package com.infix.gamelatthe.ui;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;

import java.util.List;

public class GameRuleEngine {
    private BoardGame boardGame;

    public GameRuleEngine(BoardGame boardGame) {
        this.boardGame = boardGame;
    }

    public boolean matchTwoCard(Card card1, Card card2){
        return  card1.getGroupId() == card2.getGroupId();
    }
    public boolean checkEndGame() {
        return false;
    }
    public Long calcTimeFinish() {
        return 0L;
    }
    public void trackEndTime() {

    }

    public List<Card> getCards() {
        return boardGame.getCards();
    }
}
