package com.infix.gamelatthe.data.model;

import java.util.Objects;

public class Card {
    private int id;
    private int groupId;
    private String urlImage;
    private boolean isFlipped;
    private boolean isEnable;

    public Card(int id, int groupId, String urlImage, boolean isFlipped) {
        this.id = id;
        this.groupId = groupId;
        this.urlImage = urlImage;
        this.isFlipped = isFlipped;
        isEnable = true;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return getId() == card.getId() && getGroupId() == card.getGroupId() && isFlipped() == card.isFlipped() && Objects.equals(getUrlImage(), card.getUrlImage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGroupId(), getUrlImage(), isFlipped());
    }
}
