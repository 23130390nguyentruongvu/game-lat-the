package com.infix.gamelatthe.data.model;

import com.google.firebase.firestore.Exclude;

import java.util.Arrays;
import java.util.List;

public class BoardGame {
    private List<Card> cards;
    private Long timeInit, timeEnd;


    public BoardGame(List<Card> cards, Long timeInit) {
        this.cards = cards;
        this.timeInit = timeInit;
    }
    // UC1.7.1 - init BoardGame
    public BoardGame() {}
    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return "BoardGame{" +
                "cards=" + Arrays.toString(cards.toArray()) +
                ", timeInit=" + timeInit +
                ", timeEnd=" + timeEnd +
                '}';
    }

    // 3.1.3 Kiểm tra trạng thái tất cả các thẻ
    public boolean checkAllCardFlipped() {
        for (Card card : cards) {
            if (!card.isFlipped()) {
                return false;
            }
        }
        return true;
    }
    // 3.1.5 Tính thời gian hoàn thành
    public long calcTimeFinish() {
        return timeEnd - timeInit;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }
    public void setTimeInit(Long timeInit) { this.timeInit = timeInit;}

    @Exclude
    public Long getTimeInit() {
        return timeInit;
    }

    @Exclude
    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
