package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.TrackStateFlipTwoCard;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.repository.HistoryRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

public class BoardGameViewModel extends ViewModel {
    private final MutableLiveData<TrackStateFlipTwoCard> _stateFlipTwoCard = new MutableLiveData<>();
    public LiveData<TrackStateFlipTwoCard> stateFlipTwoCard = _stateFlipTwoCard;

    private final MutableLiveData<String> _notifyMessage = new MutableLiveData<>();
    public LiveData<String> notifyMessage = _notifyMessage;

    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;

    private Card firstCard, secondCard;
    private GameRuleEngine gameRuleEngine;
    private HistoryRepository repository;
}
