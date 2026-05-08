package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.TrackStateFlipTwoCard;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.GameConfig;
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
    private GameConfig gameConfig;
    private GameRuleEngine gameRuleEngine;
    private HistoryRepository repository = new HistoryRepository();

    public void setGameConfig(GameConfig gameConfig) {
        if(gameConfig == null) return;
        this.gameConfig = gameConfig;
    }

    //Thiết lập đối tượng GameRuleEngine mới
    public void setBoardGame(BoardGame boardGame) {
        if(boardGame == null) return;
        this.gameRuleEngine = new GameRuleEngine(boardGame);
    }
}
