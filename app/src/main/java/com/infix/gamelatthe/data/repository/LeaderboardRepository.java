package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {

    private final RemoteDataSource remoteDataSource;

    public LeaderboardRepository() {
        this.remoteDataSource = new RemoteDataSource();
    }

    public interface HistoryCallback {
        void onHistoryLoaded(List<MatchHistoryItem> history);
        void onError(String message);
    }

    public void getMatchesByUserUUID(String userUUID, HistoryCallback callback) {
        remoteDataSource.queryRoomsByUserUUID(userUUID, new RemoteDataSource.RoomQueryCallback() {
            @Override
            public void onRoomsLoaded(List<RoomOnline> rooms) {
                if (rooms == null || rooms.isEmpty()) {
                    callback.onHistoryLoaded(new ArrayList<>());
                    return;
                }

                List<MatchHistoryItem> historyItems = new ArrayList<>();
                for (RoomOnline room : rooms) {
                    // 10.1.4 Hệ thống xác định vai trò của người chơi trong từng trận đấu (HOST hoặc GUEST).
                    String role = determineRole(userUUID, room);
                    // 10.1.5 Hệ thống xác định đối thủ, kết quả, độ khó, thời gian chơi và chuyển đổi sang MatchHistoryItem.
                    MatchHistoryItem item = mapToMatchHistoryItem(userUUID, room, role);
                    if (item != null) {
                        historyItems.add(item);
                    }
                }
                callback.onHistoryLoaded(historyItems);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private String determineRole(String userUUID, RoomOnline room) {
        if (room.getPlayers() != null) {
            for (PlayerOnline player : room.getPlayers()) {
                if (player.getUuid().equals(userUUID)) {
                    return player.getRole();
                }
            }
        }
        return "UNKNOWN";
    }

    private MatchHistoryItem mapToMatchHistoryItem(String userUUID, RoomOnline room, String role) {
        if (room.getPlayers() == null || room.getPlayers().size() < 2 || room.getWinnerId() == null) {
            return null;
        }

        PlayerOnline currentPlayer = null;
        PlayerOnline opponentPlayer = null;

        for (PlayerOnline player : room.getPlayers()) {
            if (player.getUuid().equals(userUUID)) {
                currentPlayer = player;
            } else {
                opponentPlayer = player;
            }
        }

        if (currentPlayer == null || opponentPlayer == null) {
            return null;
        }

        String result;
        if (room.getWinnerId().equals(userUUID)) {
            result = "WIN";
        } else if (room.getWinnerId().equals("DRAW")) {
            result = "DRAW";
        } else {
            result = "LOSE";
        }

        long playTime = 0;
        if (room.getBoardGame() != null
                && room.getBoardGame().getTimeEnd() != null
                && room.getBoardGame().getTimeInit() != null) {
            playTime = (room.getBoardGame().getTimeEnd() - room.getBoardGame().getTimeInit()) / 1000;
        }

        return new MatchHistoryItem(
                room.getRoomId(),
                room.getDifficulty(),
                role,
                opponentPlayer.getName(),
                result,
                currentPlayer.getScore(),
                playTime,
                room.getCreateAt()
        );
    }
}