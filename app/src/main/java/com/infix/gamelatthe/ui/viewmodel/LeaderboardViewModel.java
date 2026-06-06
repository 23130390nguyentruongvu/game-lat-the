package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.repository.LeaderboardRepository;

import java.util.List;

public class LeaderboardViewModel extends ViewModel {

    private final LeaderboardRepository repository;
    private final MutableLiveData<List<MatchHistoryItem>> _matchHistory = new MutableLiveData<>();
    public LiveData<List<MatchHistoryItem>> getMatchHistory() {
        return _matchHistory;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public LeaderboardViewModel() {
        this(new LeaderboardRepository());
    }

    // Constructor hỗ trợ Dependency Injection cho Testing
    public LeaderboardViewModel(LeaderboardRepository repository) {
        this.repository = repository;
    }

    // 10.1.2 Gửi yêu cầu truy vấn lịch sử thi đấu
    public void fetchUserHistory(String userUUID) {
        repository.getMatchesByUserUUID(userUUID, new LeaderboardRepository.HistoryCallback() {
            @Override
            public void onHistoryLoaded(List<MatchHistoryItem> history) {
                if (history.isEmpty()) {
                    // 10.3.1 Firestore trả về danh sách rỗng
                    _errorMessage.postValue("Không có lịch sử thi đấu.");
                } else {
                    _matchHistory.postValue(history);
                }
            }

            @Override
            public void onError(String message) {
                _errorMessage.postValue(message);
            }
        });
    }
}
