package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

public class GameRepository {

    private final RemoteDataSource remoteDataSource = new RemoteDataSource();

    public void getLevels(RemoteDataSource.LevelsCallback callback) {
        remoteDataSource.getLevels(callback);
    }

    public void getBoard(DifficultyEnum level,
                         RemoteDataSource.BoardCallback callback) {
        remoteDataSource.getBoard(level, callback);
    }

    public void createRoomOnline(PlayerOnline playerOnline, String difficulty, RoomOnlineListener roomOnlineListener) {
        remoteDataSource.createRoomOnline(playerOnline, difficulty, roomOnlineListener);
    }

    public void enterRoomOnline(PlayerOnline playerOnline, String roomCode, RoomOnlineListener roomOnlineListener) {
        //6.2.4 Hệ thống thực hiện truy vấn Firestore:
        //Ghi tên và ID của Guest vào mục danh sách người chơi trong phòng.
        //Đăng ký hàm lắng nghe sự kiện (addSnapshotListener) để đồng bộ thời gian thực với phòng này.
        remoteDataSource.enterRoomOnline(playerOnline, roomCode, roomOnlineListener);
    }
}