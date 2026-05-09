package com.infix.gamelatthe.data.source.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.infix.gamelatthe.data.model.PlayHistory;


@Database(entities = {PlayHistory.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

    // Bước 4.1.5: Khai báo hàm trừu tượng để lấy Dao

    public abstract PlayHistoryDao playHistoryDao();

}