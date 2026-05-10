package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

public class GameRepository {

    private final RemoteDataSource remoteDataSource = new RemoteDataSource();

    // UC-1: lấy danh sách level
    public void getLevels(RemoteDataSource.LevelsCallback callback) {
        remoteDataSource.getLevels(callback);
    }

    // UC-1: lấy board/cards theo level đã chọn
    public void getBoard(DifficultyEnum level,
                         RemoteDataSource.BoardCallback callback) {
        remoteDataSource.getBoard(level, callback);
    }
}