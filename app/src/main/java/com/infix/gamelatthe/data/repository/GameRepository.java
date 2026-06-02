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
}