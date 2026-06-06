package com.infix.gamelatthe.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "play_history")
public class PlayHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String playerName;
    public String difficulty;
    public long initTime;
    public long endTime;

    public PlayHistory(String playerName, String difficulty, long initTime, long endTime) {
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.initTime = initTime;
        this.endTime = endTime;
    }
}