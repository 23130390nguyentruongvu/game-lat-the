package com.infix.gamelatthe.common;

import com.infix.gamelatthe.data.model.multi.RoomOnline;

public interface RoomSnapshotCallback {
    //Trả về null khi danh sách trả về là rỗng
    void onDataChanged(RoomOnline room);
    void onError(Exception e);
}