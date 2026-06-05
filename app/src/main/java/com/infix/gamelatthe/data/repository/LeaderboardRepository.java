package com.infix.gamelatthe.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LeaderboardRepository {

    private final RemoteDataSource remoteDataSource;

    public LeaderboardRepository() {
        this.remoteDataSource = new RemoteDataSource(FirebaseFirestore.getInstance());
    }

    public interface HistoryCallback {
        void onHistoryLoaded(List<MatchHistoryItem> history);
        void onError(String message);
    }

    public void getMatchesByUserUUID(String userUUID, HistoryCallback callback) {
        remoteDataSource.queryRoomsByUserUUID(userUUID, new RemoteDataSource.FirestoreCallback<List<RoomOnline>>() {
            @Override
            public void onSuccess(List<RoomOnline> roomOnlineList) {
                if (roomOnlineList == null || roomOnlineList.isEmpty()) {
                    callback.onHistoryLoaded(new ArrayList<>());
                    return;
                }
                // 10.1.5 Chuyển đổi dữ liệu sang danh sách MatchHistoryItem
                List<MatchHistoryItem> matchHistoryItems = mapToMatchHistoryItems(roomOnlineList, userUUID);
                callback.onHistoryLoaded(matchHistoryItems);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private List<MatchHistoryItem> mapToMatchHistoryItems(List<RoomOnline> roomOnlineList, String currentUserUUID) {
        List<MatchHistoryItem> matchHistoryItems = new ArrayList<>();
        for (RoomOnline room : roomOnlineList) {
            MatchHistoryItem item = new MatchHistoryItem();
            item.setRoomId(room.getRoomId());
            item.setDifficulty(room.getDifficulty());
            item.setCreateAt(room.getCreateAt());

            // 10.1.4 Xác định vai trò của người chơi
            String role = "";
            String opponentName = "";
            int userScore = 0;
            int opponentScore = 0;

            for (PlayerOnline player : room.getPlayers()) {
                if (player.getUuid().equals(currentUserUUID)) {
                    role = player.getRole();
                    userScore = player.getScore();
                } else {
                    opponentName = player.getName();
                    opponentScore = player.getScore();
                }
            }
            item.setRole(role);
            item.setOpponentName(opponentName);

            // 10.1.5 Xác định kết quả
            String result = "DRAW";
            if (Objects.equals(room.getWinnerId(), currentUserUUID)) {
                result = "WIN";
            } else if (room.getWinnerId() != null && !room.getWinnerId().isEmpty()) {
                result = "LOSE";
            }
            item.setResult(result);
            item.setScore(userScore - opponentScore);

            matchHistoryItems.add(item);
        }
        return matchHistoryItems;
    }
}
