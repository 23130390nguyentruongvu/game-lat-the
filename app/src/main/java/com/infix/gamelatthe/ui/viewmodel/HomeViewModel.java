package com.infix.gamelatthe.ui.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.GameConfig;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;
import com.infix.gamelatthe.utils.NetworkUtils;

import java.util.Collections;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private GameRepository repository = new GameRepository();
    private GameConfig config;
    private GameConfig currentConfig;

    public MutableLiveData<GameConfig> gameConfigState =
            new MutableLiveData<>();
    public MutableLiveData<List<String>> levelList = new MutableLiveData<>();
    public MutableLiveData<String> errorState = new MutableLiveData<>();
    public MutableLiveData<BoardGame> boardGameState = new MutableLiveData<>();

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    // 1. START GAME CLICK

    public void onStartGameClicked() {

        // CHECK NETWORK
        if (!NetworkUtils.isNetworkAvailable(context)) {
            errorState.setValue("No Internet Connection");
            return;
        }

        // LOAD LEVELS
        repository.getLevels(new RemoteDataSource.LevelsCallback() {
            @Override
            public void onSuccess(List<String> levels) {
                if (levels == null || levels.isEmpty()) {
                    errorState.setValue("No levels available");
                } else {
                    levelList.setValue(levels);
                }
            }

            @Override
            public void onError(String error) {
                errorState.setValue(error);
            }
        });
    }

    // UC-1 STEP 1.1.7
    public void onConfigConfirmed(String name, String level) {
        DifficultyEnum difficultyEnum;

        if(name.equalsIgnoreCase(DifficultyEnum.EASY.name()))
            difficultyEnum = DifficultyEnum.EASY;
        else if(name.equalsIgnoreCase(DifficultyEnum.NORMAL.name()))
            difficultyEnum = DifficultyEnum.NORMAL;
        else if(name.equalsIgnoreCase(DifficultyEnum.HARD.name()))
            difficultyEnum = DifficultyEnum.HARD;
        else return;

        // UC-1 STEP 1.1.8
        if (name == null || name.trim().isEmpty()) {
            name = "User1";
        }
        // UC-1 STEP 1.1.9
        this.config = new GameConfig(name, difficultyEnum);
        // UC-1 STEP 1.1.10
        repository.getBoard(difficultyEnum, new RemoteDataSource.BoardCallback() {
            @Override
            public void onSuccess(List<Card> cards) {

                if (cards == null || cards.isEmpty()) {
                    errorState.setValue("No cards found");
                    return;
                }

                // SHUFFLE
                Collections.shuffle(cards);

                // CREATE BOARD GAME
                BoardGame game = new BoardGame(
                        cards,
                        System.currentTimeMillis()
                );

                // UC-1 STEP 1.1.11
                boardGameState.setValue(game);
            }

            @Override
            public void onError(String error) {
                errorState.setValue(error);
            }
        });
    }

    // UC1 START GAME
    public void startGame(String playerName,
                          DifficultyEnum difficulty) {

        GameConfig config =
                new GameConfig(
                        playerName,
                        difficulty
                );

        // VALIDATION
        if (!config.isValid()) {

            errorState.setValue(
                    "Invalid game config"
            );

            return;
        }

        currentConfig = config;

        gameConfigState.setValue(config);
    }
}