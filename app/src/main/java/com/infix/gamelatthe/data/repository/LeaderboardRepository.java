package com.infix.gamelatthe.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository quản lý dữ liệu lịch sử trận đấu
 * Gọi RemoteDataSource để lấy dữ liệu từ Firebase
 * Transform RoomOnline → MatchHistoryItem
 */
public class LeaderboardRepository {

    private final RemoteDataSource remoteDataSource = new RemoteDataSource();

    /**
     * Lấy danh sách lịch sử trận đấu của người chơi theo UUID
     * @param userUUID UUID của người chơi
     * @return LiveData chứa danh sách MatchHistoryItem
     */
    public LiveData<List<MatchHistoryItem>> getMatchesByUserUUID(String userUUID) {
        MutableLiveData<List<MatchHistoryItem>> result = new MutableLiveData<>();

        remoteDataSource.queryRoomsByUserUUID(userUUID, new RemoteDataSource.LeaderboardCallback() {
            @Override
            public void onSuccess(List<RoomOnline> rooms) {
                if (rooms == null || rooms.isEmpty()) {
                    result.setValue(new ArrayList<>());
                    return;
                }

                // Transform RoomOnline → MatchHistoryItem
                List<MatchHistoryItem> matchHistoryList = new ArrayList<>();
                for (RoomOnline room : rooms) {
                    MatchHistoryItem item = transformRoomToMatchHistory(room, userUUID);
                    if (item != null) {
                        matchHistoryList.add(item);
                    }
                }

                // Sort by createAt DESC (mới nhất trước)
                Collections.sort(matchHistoryList, (a, b) -> {
                    if (a.getCreateAt() == null || b.getCreateAt() == null) {
                        return 0;
                    }
                    return Long.compare(b.getCreateAt().getTime(), a.getCreateAt().getTime());
                });

                result.setValue(matchHistoryList);
            }

            @Override
            public void onFailure(String error) {
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Chuyển đổi RoomOnline thành MatchHistoryItem
     * @param room RoomOnline từ Firestore
     * @param currentUserUUID UUID của người chơi hiện tại
     * @return MatchHistoryItem hoặc null nếu dữ liệu không hợp lệ
     */
    private MatchHistoryItem transformRoomToMatchHistory(RoomOnline room, String currentUserUUID) {
        if (room.getPlayers() == null || room.getPlayers().size() < 2) {
            return null;
        }

        // Tìm người chơi hiện tại và đối thủ
        com.infix.gamelatthe.data.model.multi.PlayerOnline currentPlayer = null;
        com.infix.gamelatthe.data.model.multi.PlayerOnline opponent = null;

        for (com.infix.gamelatthe.data.model.multi.PlayerOnline player : room.getPlayers()) {
            if (player.getUuid().equals(currentUserUUID)) {
                currentPlayer = player;
            } else {
                opponent = player;
            }
        }

        // Nếu không tìm thấy người chơi hiện tại, bỏ qua
        if (currentPlayer == null || opponent == null) {
            return null;
        }

        // Xác định kết quả (WIN/LOSE)
        String result = determineResult(currentPlayer, opponent, room.getWinnerId());

        // Tính thời gian chơi (nếu có)
        Long playDuration = null;
        if (room.getBoardGame() != null && room.getBoardGame().getTimeInit() != null) {
            long timeInit = room.getBoardGame().getTimeInit();
            long timeEnd = room.getBoardGame().getTimeEnd() != null ? room.getBoardGame().getTimeEnd() : System.currentTimeMillis();
            playDuration = timeEnd - timeInit;
        }

        return new MatchHistoryItem(
                room.getRoomId(),
                room.getRoomCode(),
                room.getDifficulty(),
                currentPlayer.getRole(),
                opponent.getName(),
                opponent.getUuid(),
                result,
                currentPlayer.getScore(),
                opponent.getScore(),
                room.getCreateAt(),
                playDuration
        );
    }

    /**
     * Xác định kết quả trận đấu (WIN/LOSE)
     * @param currentPlayer Người chơi hiện tại
     * @param opponent Đối thủ
     * @param winnerId UUID người chiến thắng
     * @return "WIN" hoặc "LOSE"
     */
    private String determineResult(com.infix.gamelatthe.data.model.multi.PlayerOnline currentPlayer,
                                   com.infix.gamelatthe.data.model.multi.PlayerOnline opponent,
                                   String winnerId) {
        // Nếu có winnerId rõ ràng, so sánh
        if (winnerId != null && !winnerId.isEmpty()) {
            return winnerId.equals(currentPlayer.getUuid()) ? "WIN" : "LOSE";
        }

        // Nếu không, so sánh score
        if (currentPlayer.getScore() > opponent.getScore()) {
            return "WIN";
        } else if (currentPlayer.getScore() < opponent.getScore()) {
            return "LOSE";
        } else {
            // Cân bằng điểm - có thể coi là WIN/LOSE tùy logic
            // Ở đây coi là LOSE
            return "LOSE";
        }
    }
}