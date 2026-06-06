package com.infix.gamelatthe.data.model;

import com.infix.gamelatthe.common.DifficultyEnum;

public class GameConfig {

    private String playerName;
    private DifficultyEnum difficulty;

    // REQUIRED for Firestore
    public GameConfig() {
    }

    public GameConfig(String playerName,
                      DifficultyEnum difficulty) {

        this.playerName = playerName;
        this.difficulty = difficulty;
    }

    // GETTERS

    public String getPlayerName() {
        return playerName;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    // SETTERS

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setDifficulty(DifficultyEnum difficulty) {
        this.difficulty = difficulty;
    }

    // UC1 validation
    public boolean isValid() {

        return playerName != null
                && !playerName.trim().isEmpty()
                && difficulty != null;
    }
}