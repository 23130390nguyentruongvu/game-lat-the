package com.infix.gamelatthe.data.model.multi;

import com.infix.gamelatthe.data.model.Card;

public class CardOnline extends Card {
    private boolean isMatched;

    public CardOnline(int id, int groupId, String urlImage, boolean isFlipped, boolean isMatched) {
        super(id, groupId, urlImage, isFlipped);
        this.isMatched = isMatched;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }
}
