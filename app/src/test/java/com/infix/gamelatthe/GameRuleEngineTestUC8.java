package com.infix.gamelatthe; // Bác nhớ check lại dòng package trên cùng máy bác nhé

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.ui.GameRuleEngine;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GameRuleEngineTestUC8 {

    private RoomOnline room;

    @Before
    public void setUp() {
        // Chỉ khởi tạo phòng ảo trước mỗi hàm test
        room = new RoomOnline();
    }

    // ==========================================
    // TEST HÀM: checkOnlineEndGame (Kiểm tra hết bài chưa)
    // ==========================================

    @Test
    public void testCheckOnlineEndGame_KhiTatCaTheDaGhepTrung_ThiTraVeTrue() {
        // 1. Giả lập danh sách thẻ: Tất cả đều đã isMatched = true
        List<Card> cards = new ArrayList<>();
        CardOnline card1 = new CardOnline();
        card1.setMatched(true);
        CardOnline card2 = new CardOnline();
        card2.setMatched(true);

        cards.add(card1);
        cards.add(card2);

        BoardGame boardGame = new BoardGame();
        boardGame.setCards(cards);
        room.setBoardGame(boardGame);

        // FIX LỖI ĐỎ: Truyền boardGame vào trong ngoặc
        GameRuleEngine gameRuleEngine = new GameRuleEngine(boardGame);

        // 2. Gọi hàm và Khẳng định (Assert) kết quả phải là True
        assertTrue(gameRuleEngine.checkOnlineEndGame(room));
    }

    @Test
    public void testCheckOnlineEndGame_KhiCoTheChuaGhepTrung_ThiTraVeFalse() {
        // 1. Giả lập danh sách thẻ: Có 1 thẻ chưa lật trúng (isMatched = false)
        List<Card> cards = new ArrayList<>();
        CardOnline card1 = new CardOnline();
        card1.setMatched(true);
        CardOnline card2 = new CardOnline();
        card2.setMatched(false); // Thẻ này chưa ăn điểm

        cards.add(card1);
        cards.add(card2);

        BoardGame boardGame = new BoardGame();
        boardGame.setCards(cards);
        room.setBoardGame(boardGame);

        // FIX LỖI ĐỎ: Truyền boardGame vào trong ngoặc
        GameRuleEngine gameRuleEngine = new GameRuleEngine(boardGame);

        // 2. Gọi hàm và Khẳng định kết quả phải là False (Game chưa kết thúc)
        assertFalse(gameRuleEngine.checkOnlineEndGame(room));
    }

    // ==========================================
    // TEST HÀM: calculateOnlineWinner (Phân định thắng thua)
    // ==========================================

    @Test
    public void testCalculateOnlineWinner_KhiHostDiemCaoHon_ThiTraVeHostId() {
        // 1. Giả lập danh sách người chơi (Host 5 điểm, Guest 3 điểm)
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("host_123", "Host", 5, true, "HOST"));
        players.add(new PlayerOnline("guest_456", "Guest", 3, true, "GUEST"));
        room.setPlayers(players);

        BoardGame boardGame = new BoardGame();
        room.setBoardGame(boardGame);

        // FIX LỖI ĐỎ: Truyền boardGame vào trong ngoặc
        GameRuleEngine gameRuleEngine = new GameRuleEngine(boardGame);

        // 2. Gọi hàm tính toán Winner
        String winnerId = gameRuleEngine.calculateOnlineWinner(room);

        // 3. Khẳng định Winner phải là Host
        assertEquals("host_123", winnerId);
    }

    @Test
    public void testCalculateOnlineWinner_KhiHaiNguoiBangDiem_ThiTraVeDRAW() {
        // 1. Giả lập danh sách người chơi (Host 4 điểm, Guest 4 điểm)
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("host_123", "Host", 4, true, "HOST"));
        players.add(new PlayerOnline("guest_456", "Guest", 4, true, "GUEST"));
        room.setPlayers(players);

        BoardGame boardGame = new BoardGame();
        room.setBoardGame(boardGame);

        // FIX LỖI ĐỎ: Truyền boardGame vào trong ngoặc
        GameRuleEngine gameRuleEngine = new GameRuleEngine(boardGame);

        // 2. Gọi hàm tính toán Winner
        String winnerId = gameRuleEngine.calculateOnlineWinner(room);

        // 3. Khẳng định phải trả về chữ "DRAW" (Hòa)
        assertEquals("DRAW", winnerId);
    }
}