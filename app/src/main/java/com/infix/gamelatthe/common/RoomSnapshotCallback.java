package com.infix.gamelatthe.common;

import com.infix.gamelatthe.data.model.multi.RoomOnline;

public interface RoomSnapshotCallback {
    void onDataChanged(RoomOnline room);
    void onError(Exception e);
}