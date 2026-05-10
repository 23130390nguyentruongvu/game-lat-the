package com.infix.gamelatthe;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.infix.gamelatthe.data.model.PlayHistory;
import com.infix.gamelatthe.data.source.local.MyDatabase;
import com.infix.gamelatthe.data.source.local.PlayHistoryDao;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TestUC4 {

        private MyDatabase db;
        private PlayHistoryDao dao;

        @Before
        public void createDb() {
            Context context = ApplicationProvider.getApplicationContext();
            db = Room.inMemoryDatabaseBuilder(context, MyDatabase.class).build();
            dao = db.playHistoryDao();
        }

        @After
        public void closeDb() {
            db.close();
        }

    @Test
    public void insertAndVerifyData() {
        PlayHistory record = new PlayHistory("TruongVu", "Hard", 1000L, 5000L);
        long id = dao.insertRecord(record);

        List<PlayHistory> results = dao.getTop10ByDifficulty("Hard");
        assertTrue(id > 0);
        assertFalse(results.isEmpty());
        assertEquals("TruongVu", results.get(0).playerName); // Kiểm tra tên
        assertEquals("Hard", results.get(0).difficulty);    // Kiểm tra độ khó
        assertEquals(4000L, results.get(0).endTime - results.get(0).initTime); // Kiểm tra logic thời gian
    }
}
