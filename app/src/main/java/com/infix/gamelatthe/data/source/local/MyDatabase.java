package com.infix.gamelatthe.data.source.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.infix.gamelatthe.data.model.PlayHistory;


@Database(entities = {PlayHistory.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    private static volatile MyDatabase instance;

    // Bước 4.1.5: Khai báo hàm trừu tượng để lấy Dao
    public abstract PlayHistoryDao playHistoryDao();

    public static MyDatabase getInstance(Context context) {
        if(instance == null) {
            synchronized (MyDatabase.class) {
                if(instance == null)
                    instance = Room.databaseBuilder(
                            context,
                            MyDatabase.class,
                            "mydb"
                    ).fallbackToDestructiveMigration()
                            .build();
            }
        }

        return instance;
    }
//
//    public static MyDatabase getMyDatabase() {
//        if(instance == null)
//            throw new NullPointerException("Instance bị null");
//
//        return instance;
//    }
}