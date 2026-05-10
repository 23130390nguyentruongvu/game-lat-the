package com.infix.gamelatthe.data.model;

import com.infix.gamelatthe.common.DifficultyEnum;

public class GameConfig {

    private String playerName;
    private DifficultyEnum difficulty;

    // REQUIRED for Firestore
    public GameConfig(String name, String string) {}

    public GameConfig(String playerName, DifficultyEnum difficulty) {
        this.playerName = playerName;
        this.difficulty = difficulty;
    }

    public String getPlayerName() {
        return playerName;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    // UC1 validation rule
    public boolean isValid() {
        return playerName != null && !playerName.trim().isEmpty()
                && difficulty != null;
    }
}