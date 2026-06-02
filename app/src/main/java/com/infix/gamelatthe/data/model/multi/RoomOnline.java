package com.infix.gamelatthe.data.model.multi;

import com.google.firebase.firestore.ServerTimestamp;
import com.infix.gamelatthe.data.model.BoardGame;

import java.util.Date;
import java.util.List;

public class RoomOnline {
    private String roomId, roomCode, status, difficulty, currentTurn, winnerId;
    @ServerTimestamp
    private Date createAt;
    private List<PlayerOnline> players;
    private BoardGame boardGame;

    public RoomOnline(String roomId, String roomCode, String status,
                      String difficulty, String currentTurn, String winnerId,
                      Date createAt, List<PlayerOnline> players, BoardGame boardGame) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.status = status;
        this.difficulty = difficulty;
        this.currentTurn = currentTurn;
        this.winnerId = winnerId;
        this.createAt = createAt;
        this.players = players;
        this.boardGame = boardGame;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getStatus() {
        return status;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public List<PlayerOnline> getPlayers() {
        return players;
    }

    public BoardGame getBoardGame() {
        return boardGame;
    }
}
