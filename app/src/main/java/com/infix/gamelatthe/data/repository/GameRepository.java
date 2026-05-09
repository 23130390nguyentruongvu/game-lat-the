package com.infix.gamelatthe.data.repository;

import com.infix.gamelatthe.data.source.remote.RemoteDataSource;

public class GameRepository {
    private RemoteDataSource remoteDataSource;

    public GameRepository(RemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }
}
