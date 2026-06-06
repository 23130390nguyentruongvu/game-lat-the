package com.infix.gamelatthe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TestUnitUC7 {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private OnlineBoardGameViewModel viewModel;
    private RoomOnline mockRoom;
    private List<Card> mockCards;
    private String myUserId = "sinh_123";
    private String opponentId = "vu_456";

    @Before
    public void setup() {
        viewModel = new OnlineBoardGameViewModel();

        // 1. Tạo danh sách thẻ giả
        mockCards = new ArrayList<>();
        CardOnline card1 = new CardOnline(1, 101, "url1", false);
        CardOnline card2 = new CardOnline(2, 101, "url1", false);
        CardOnline card3 = new CardOnline(3, 102, "url2", false);

        mockCards.add(card1);
        mockCards.add(card2);
        mockCards.add(card3);

        BoardGame boardGame = new BoardGame(mockCards, System.currentTimeMillis());

        // 2. Tạo phòng chơi giả
        mockRoom = new RoomOnline();
        mockRoom.setBoardGame(boardGame);

        // 3. Tạo 2 người chơi bằng Constructor có tham số (Đã fix lỗi setUuid)
        List<PlayerOnline> players = new ArrayList<>();
        PlayerOnline me = new PlayerOnline(myUserId, "Người chơi 1", 0, true, "host");
        PlayerOnline opponent = new PlayerOnline(opponentId, "Người chơi 2", 0, true, "member");

        players.add(me);
        players.add(opponent);
        mockRoom.setPlayers(players);

        // Mặc định cho tới lượt của mình
        mockRoom.setCurrentTurn(myUserId);
    }

    @Test
    public void testWrongTurn_ShouldNotFlipCard() {
        // Cố tình setup đang là lượt của đối thủ
        mockRoom.setCurrentTurn(opponentId);
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0);

        // Mình cố tình click
        viewModel.onCardClick(card1, mockRoom, myUserId);

        // 7.3: Kiểm tra thẻ KHÔNG bị lật vì sai lượt
        assertFalse(card1.isFlipped());
    }

    @Test
    public void testSelectFirstCard_RightTurn() {
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0);

        // Chọn thẻ thứ nhất đúng lượt
        viewModel.onCardClick(card1, mockRoom, myUserId);

        // 7.1.2: Kiểm tra thẻ đã được cập nhật trạng thái lật ngửa
        assertTrue(card1.isFlipped());
    }

    @Test
    public void testMatchTwoCards_ShouldIncreaseScoreAndKeepTurn() {
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0); // Cặp 101
        CardOnline card2 = (CardOnline) mockRoom.getBoardGame().getCards().get(1); // Cặp 101

        viewModel.onCardClick(card1, mockRoom, myUserId);
        viewModel.onCardClick(card2, mockRoom, myUserId);

        // 7.1.6: Kiểm tra 2 thẻ đã được đánh dấu là matched (trùng khớp)
        assertTrue(card1.isMatched());
        assertTrue(card2.isMatched());

        // 7.1.6: Kiểm tra điểm số người chơi tăng lên 1
        PlayerOnline me = mockRoom.getPlayers().get(0);
        assertEquals(1, me.getScore());

        // 7.1.7: Kiểm tra lượt chơi VẪN GIỮ NGUYÊN là của mình
        assertEquals(myUserId, mockRoom.getCurrentTurn());
    }

    @Test
    public void testNotMatchTwoCards_ShouldSwitchTurn() throws InterruptedException {
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0); // Cặp 101
        CardOnline card3 = (CardOnline) mockRoom.getBoardGame().getCards().get(2); // Cặp 102

        viewModel.onCardClick(card1, mockRoom, myUserId);
        viewModel.onCardClick(card3, mockRoom, myUserId);

        // Vì hàm sai thẻ có delay 1.5s (Handler), ta phải bắt Test chờ 1.6s để logic chạy xong
        Thread.sleep(1600);

        // 7.2.3: Kiểm tra 2 thẻ đã bị úp lại
        assertFalse(card1.isFlipped());
        assertFalse(card3.isFlipped());

        // 7.2.4: Kiểm tra lượt chơi đã CHUYỂN SANG đối thủ
        assertEquals(opponentId, mockRoom.getCurrentTurn());
    }
}