package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.UIState;
import com.infix.gamelatthe.data.model.room.PlayHistory;
import com.infix.gamelatthe.data.repository.HistoryRepository;

import java.util.List;

public class HistoryViewModel extends ViewModel {
    private HistoryRepository repository;
    private MutableLiveData<UIState> _uiState = new MutableLiveData<>();
    public LiveData<UIState> uiState = _uiState;

    private MutableLiveData<List<PlayHistory>> _historyList= new MutableLiveData<>();
    public LiveData<List<PlayHistory>> historyList = _historyList;

    private MutableLiveData<String> _errorMessage= new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;
    public void setRepository(HistoryRepository repo) {
        this.repository = repo;
    }
}
