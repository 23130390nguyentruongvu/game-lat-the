package com.infix.gamelatthe.data.source.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.infix.gamelatthe.data.model.PlayHistory;

import java.util.List;

@Dao
public interface PlayHistoryDao {

    // Bước 4.1.5: Thực thi lưu entity vào LocalSource (insertRecord)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertRecord(PlayHistory record);
     @Query("SELECT * FROM play_history WHERE difficulty = :difficulty ORDER BY (endTime - initTime) ASC LIMIT 10")
    List<PlayHistory> getTop10ByDifficulty(String difficulty);
}