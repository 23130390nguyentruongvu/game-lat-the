package com.infix.gamelatthe.data.model;

public class GameConfig {
    private String playerName;
    private String difficulty;

    public GameConfig(String playerName, String difficulty) {
        this.playerName = playerName;
        this.difficulty = difficulty;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
