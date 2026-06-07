package com.infix.gamelatthe.data.model.multi;

import com.infix.gamelatthe.data.model.PlayHistory;

import java.util.Date; // Import Date
import java.util.Objects;

public class MatchHistoryItem  {
    public String roomId;
    public String difficulty;
    public String role; // HOST or GUEST
    public String opponentName;
    public String result; // WIN or LOSE
    public int score; // Re-added score property
    public long playTime; // in seconds
    public Date createAt; // Changed to Date

    public MatchHistoryItem(String roomId, String difficulty, String role, String opponentName, String result, int score, long playTime, Date createAt) { // Updated constructor
        this.roomId = roomId;
        this.difficulty = difficulty;
        this.role = role;
        this.opponentName = opponentName;
        this.result = result;
        this.score = score;
        this.playTime = playTime;
        this.createAt = createAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchHistoryItem that = (MatchHistoryItem) o;
        return score == that.score &&
                playTime == that.playTime &&
                Objects.equals(roomId, that.roomId) &&
                Objects.equals(difficulty, that.difficulty) &&
                Objects.equals(role, that.role) &&
                Objects.equals(opponentName, that.opponentName) &&
                Objects.equals(result, that.result) &&
                Objects.equals(createAt, that.createAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, difficulty, role, opponentName, result, score, playTime, createAt); // Include Date in hash
    }
}
