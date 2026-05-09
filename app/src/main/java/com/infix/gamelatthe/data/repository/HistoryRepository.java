package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.data.model.PlayHistory;
import com.infix.gamelatthe.data.source.local.PlayHistoryDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {


    private final PlayHistoryDao localSource;
    private final ExecutorService executorService;


    public HistoryRepository(PlayHistoryDao localSource) {
        this.localSource = localSource;
        this.executorService = Executors.newSingleThreadExecutor();
    }


    public interface RepoCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Bước 4.1.4: Repository nhận yêu cầu saveResult từ ViewModel
    public void saveResult(PlayHistory entity, RepoCallback callback) {
        executorService.execute(() -> {
            try {
                // Bước 4.1.5: Điều hướng yêu cầu xuống DAO (insertRecord)
                long id = localSource.insertRecord(entity);

                // Bước 4.1.6: DAO trả về ID (thành công)
                if (id > 0 && callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                // Bước 4.3.1: Throw Exception khi gặp lỗi bộ nhớ (Exceptions)
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}