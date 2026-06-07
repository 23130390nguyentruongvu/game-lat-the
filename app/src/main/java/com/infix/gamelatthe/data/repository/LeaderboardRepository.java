package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;
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

    // [10.1.2] Hệ thống gửi yêu cầu lấy lịch sử thi đấu của người chơi theo UUID.
    public void getMatchesByUserUUID(String userUUID, HistoryCallback callback) {
        // SỬA TẠI ĐÂY: Gọi hàm truy vấn lịch sử từ collection match_history
        remoteDataSource.queryMatchHistoryByUserUUID(userUUID, new RemoteDataSource.HistoryQueryCallback() {
            @Override
            public void onHistoryLoaded(List<MatchHistoryItem> history) {
                // [10.1.3] Firebase Firestore trả về danh sách lịch sử tương ứng
                callback.onHistoryLoaded(history);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}