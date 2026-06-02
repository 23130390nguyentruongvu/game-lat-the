package com.infix.gamelatthe.data.model;

import java.util.Objects;

public class Card {
    protected int id;
    protected int groupId;
    protected String urlImage;
    protected boolean isFlipped;
    protected boolean isEnable;

    public Card() {}

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
        return getId() == card.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGroupId(), getUrlImage(), isFlipped());
    }
}
