package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.repository.LeaderboardRepository;

import java.util.List;


public class LeaderboardViewModel extends ViewModel {

    private final LeaderboardRepository repository = new LeaderboardRepository();

    private final MutableLiveData<List<MatchHistoryItem>> _matchHistoryList = new MutableLiveData<>();
    public LiveData<List<MatchHistoryItem>> matchHistoryList = _matchHistoryList;

    private final MutableLiveData<String> _errorState = new MutableLiveData<>();
    public LiveData<String> errorState = _errorState;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public void fetchUserHistory(String userUUID) {
        if (userUUID == null || userUUID.isEmpty()) {
            _errorState.setValue("UUID không hợp lệ");
            return;
        }

        _isLoading.setValue(true);

        repository.getMatchesByUserUUID(userUUID).observeForever(matches -> {
            _isLoading.setValue(false);

            if (matches == null) {
                _errorState.setValue("Lỗi khi lấy dữ liệu");
                return;
            }

            if (matches.isEmpty()) {
                _matchHistoryList.setValue(matches);
                return;
            }

            _matchHistoryList.setValue(matches);
        });
    }

    public void clearError() {
        _errorState.setValue(null);
    }
}