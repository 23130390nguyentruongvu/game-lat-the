package com.infix.gamelatthe;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class MatchHistoryItemUC9Test {
@Test
public void UC9_CreateMatchHistory_Success() {
    Date now = new Date();
    MatchHistoryItem history = new MatchHistoryItem(
            "ROOM01",
            "EASY",
            "HOST",
            "PlayerB",
            "WIN",
            100,
            60, now );
    assertNotNull(history);
    assertEquals("ROOM01", history.roomId);
    assertEquals("EASY", history.difficulty);
    assertEquals("HOST", history.role);
    assertEquals("PlayerB", history.opponentName);
    assertEquals("WIN", history.result);
    assertEquals(100, history.score);
    assertEquals(60, history.playTime);
    assertEquals(now, history.createAt);
}
}
