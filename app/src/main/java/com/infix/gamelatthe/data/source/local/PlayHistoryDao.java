package com.infix.gamelatthe.data.source.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import com.infix.gamelatthe.data.model.PlayHistory;

@Dao
public interface PlayHistoryDao {

    // Bước 4.1.5: Thực thi lưu entity vào LocalSource (insertRecord)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertRecord(PlayHistory record);
}