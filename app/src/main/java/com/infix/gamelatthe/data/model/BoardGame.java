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

    public Long getTimeInit() {
        return timeInit;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }
}
