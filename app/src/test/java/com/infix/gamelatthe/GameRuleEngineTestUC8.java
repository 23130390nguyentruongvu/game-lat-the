package com.infix.gamelatthe;


import static org.junit.Assert.*;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.ui.GameRuleEngine;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
public class GameRuleEngineTestUC8 {
    private GameRuleEngine gameRuleEngine;
    private BoardGame mockBoardGame;
    private RoomOnline mockRoomOnline;

    @Before
    public void setUp() {
        mockBoardGame = new BoardGame();
        gameRuleEngine = new GameRuleEngine(mockBoardGame);
        mockRoomOnline = new RoomOnline();
    }

    // ==========================================
    // KIỂM THỬ HÀM: checkOnlineEndGame()
    // ==========================================

    @Test
    public void testCheckOnlineEndGame_KhiTatCaCardDaLat_TraVeTrue() {
        // [Kịch bản thông thường] Giả lập tất cả thẻ bài trong phòng đều đã lật (isFlipped = true)
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 101, "url1", true));
        cards.add(new Card(2, 101, "url1", true));

        mockBoardGame.setCards(cards);
        mockRoomOnline.setBoardGame(mockBoardGame);

        boolean result = gameRuleEngine.checkOnlineEndGame(mockRoomOnline);

        // Khẳng định kết quả phải là TRUE (Trận đấu kết thúc)
        assertTrue(result);
    }

    @Test
    public void testCheckOnlineEndGame_KhiConCardChuaLat_TraVeFalse() {
        // [Kịch bản biên/lỗi] Giả lập vẫn còn thẻ chưa lật (một thẻ true, một thẻ false)
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(1, 101, "url1", true));
        cards.add(new Card(2, 101, "url1", false)); // Thẻ này chưa lật

        mockBoardGame.setCards(cards);
        mockRoomOnline.setBoardGame(mockBoardGame);

        boolean result = gameRuleEngine.checkOnlineEndGame(mockRoomOnline);

        // Khẳng định kết quả phải là FALSE (Trận đấu tiếp tục)
        assertFalse(result);
    }

    // ==========================================
    // KIỂM THỬ HÀM: calculateOnlineWinner()
    // ==========================================

    @Test
    public void testCalculateOnlineWinner_NguoiChoi1DiemCaoHon_P1Thang() {
        // Giả lập Người chơi 1 (UUID: user_01) có 5 điểm, Người chơi 2 (UUID: user_02) có 3 điểm
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("user_01", "Player 1", 5, true, "HOST"));
        players.add(new PlayerOnline("user_02", "Player 2", 3, true, "GUEST"));
        mockRoomOnline.setPlayers(players);

        String winnerId = gameRuleEngine.calculateOnlineWinner(mockRoomOnline);

        // Người chơi 1 phải là người thắng cuộc
        assertEquals("user_01", winnerId);
    }

    @Test
    public void testCalculateOnlineWinner_HaiNguoiBangDiem_TraVeDraw() {
        // Giả lập kịch bản 2 bên hòa nhau (cùng được 4 điểm)
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("user_01", "Player 1", 4, true, "HOST"));
        players.add(new PlayerOnline("user_02", "Player 2", 4, true, "GUEST"));
        mockRoomOnline.setPlayers(players);

        String winnerId = gameRuleEngine.calculateOnlineWinner(mockRoomOnline);

        // Hệ thống phải trả về mã "DRAW" công nhận kết quả Hòa
        assertEquals("DRAW", winnerId);
    }
}
