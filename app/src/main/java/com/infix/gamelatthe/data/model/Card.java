package com.infix.gamelatthe.data.model;

public class Card {
    private int id;
    private int groupId;
    private String urlImage;
    private boolean isFlipped;

    public Card(int id, int groupId, String urlImage, boolean isFlipped) {
        this.id = id;
        this.groupId = groupId;
        this.urlImage = urlImage;
        this.isFlipped = isFlipped;
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
}
