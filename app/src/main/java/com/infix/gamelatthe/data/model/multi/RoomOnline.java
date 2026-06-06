package com.infix.gamelatthe.data.model.multi;

import com.google.firebase.firestore.ServerTimestamp;
import com.infix.gamelatthe.data.model.BoardGame;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RoomOnline implements Serializable {
    private String roomId, roomCode, status, difficulty, currentTurn, winnerId;
    @ServerTimestamp
    private Date createAt;
    private List<PlayerOnline> players;
    private BoardGame boardGame;

    public RoomOnline() {
    }

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

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public void setPlayers(List<PlayerOnline> players) {
        this.players = players;
    }

    public void setBoardGame(BoardGame boardGame) {
        this.boardGame = boardGame;
    }
}
