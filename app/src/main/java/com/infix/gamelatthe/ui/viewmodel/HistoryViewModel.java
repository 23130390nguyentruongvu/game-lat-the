package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.infix.gamelatthe.common.UIState;
import com.infix.gamelatthe.data.model.PlayHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.source.local.PlayHistoryDao;

public class HistoryViewModel extends ViewModel {

    private PlayHistoryDao playHistoryDao;

    public final MutableLiveData<UIState> _uiState = new MutableLiveData<>();
    public final MutableLiveData<List<PlayHistory>> _historyList = new MutableLiveData<>();
    public final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public final MutableLiveData<List<MatchHistoryItem>> matchHistory = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void setDao(PlayHistoryDao dao) {
        this.playHistoryDao = dao;
    }

    public void getTop10(String difficulty) {
        if (playHistoryDao == null) {
            _errorMessage.setValue("Lỗi: Chưa khởi tạo Database DAO");
            _uiState.setValue(UIState.ERROR);
            return;
        }

        _uiState.setValue(UIState.LOADING);

        executorService.execute(() -> {
            try {
                List<PlayHistory> list = playHistoryDao.getTop10ByDifficulty(difficulty);

                if (list == null || list.isEmpty()) {
                    _uiState.postValue(UIState.EMPTY);
                } else {
                    _historyList.postValue(list);
                    _uiState.postValue(UIState.SUCCESS);
                }
            } catch (Exception e) {
                _errorMessage.postValue(e.getMessage());
                _uiState.postValue(UIState.ERROR);
            }
        });
    }

    /**
     * [10.1.2] Gửi yêu cầu lấy lịch sử thi đấu của người chơi theo userUUID.
     */
    public void loadMatchHistory(String userUUID) {
        if (userUUID == null || userUUID.isEmpty()) return;

        db.collection("match_history")
                .whereEqualTo("userUUID", userUUID) // Lọc đúng trận đấu của người dùng hiện tại
                .orderBy("createAt", Query.Direction.DESCENDING) // Sắp xếp trận mới nhất lên đầu
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<MatchHistoryItem> list = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot) {
                        // [10.1.3] Firebase Firestore trả về danh sách
                        // Cập nhật Constructor 9 tham số (thêm userUUID ở đầu)
                        MatchHistoryItem item = new MatchHistoryItem(
                                doc.getString("userUUID"),
                                doc.getString("roomId"),
                                doc.getString("difficulty"),
                                doc.getString("role"),
                                doc.getString("opponentName"),
                                doc.getString("result"),
                                doc.get("score") != null ? ((Number) doc.get("score")).intValue() : 0,
                                doc.get("playTime") != null ? ((Number) doc.get("playTime")).longValue() : 0L,
                                doc.getDate("createAt")
                        );

                        list.add(item);
                    }
                    // [10.1.6] Hệ thống cập nhật danh sách hiển thị
                    matchHistory.setValue(list);
                })
                .addOnFailureListener(e -> {
                    _errorMessage.setValue("Lỗi tải lịch sử: " + e.getMessage());
                });
    }
}