package com.infix.gamelatthe.common;

import java.util.Objects;

public class TrackStateFlipTwoCard {
    private StateFlipTwoCard state;
    private Long currentTimeMillis;
    private boolean isFirstCard;

    public TrackStateFlipTwoCard(StateFlipTwoCard state, boolean isFirstCard) {
        currentTimeMillis = System.currentTimeMillis();
        this.state = state;
        this.isFirstCard = isFirstCard;
    }

    public StateFlipTwoCard getState() {
        return state;
    }

    public boolean isFirstCard() {
        return isFirstCard;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TrackStateFlipTwoCard)) return false;
        TrackStateFlipTwoCard that = (TrackStateFlipTwoCard) o;
        return state == that.state && Objects.equals(currentTimeMillis, that.currentTimeMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, currentTimeMillis);
    }
}
