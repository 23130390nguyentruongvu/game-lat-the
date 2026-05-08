package com.infix.gamelatthe.data.model;

import java.util.List;

public class BoardGame {
    private List<Card> cards;
    private Long timeInit, timeEnd;

    public BoardGame(List<Card> cards, Long timeInit) {
        this.cards = cards;
        this.timeInit = timeInit;
    }

    public List<Card> getCards() {
        return cards;
    }
}
