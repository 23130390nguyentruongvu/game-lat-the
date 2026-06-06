package com.infix.gamelatthe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.common.StateFlipTwoCard;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TestUC2 {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private BoardGameViewModel viewModel;
    private List<Card> mockCards;

    @Before
    public void setup() {
        viewModel = new BoardGameViewModel();

        // Khởi tạo dữ liệu giả (Mock Data)
        mockCards = new ArrayList<>();
        mockCards.add(new Card(1, 101, "url1", false)); // Thẻ 1 (Cặp A)
        mockCards.add(new Card(2, 101, "url1", false)); // Thẻ 2 (Cặp A)
        mockCards.add(new Card(3, 102, "url2", false)); // Thẻ 3 (Cặp B)

        BoardGame boardGame = new BoardGame(mockCards, System.currentTimeMillis());
        viewModel.setBoardGame(boardGame);
    }

    @Test
    public void testSelectFirstCard() {
        // 2.1.1: Người chơi chọn thẻ thứ nhất
        Card card1 = mockCards.get(0);
        viewModel.onCardClick(card1);

        // Kiểm tra ViewModel đã ghi nhận thẻ thứ nhất chưa
        assertEquals(card1, viewModel.getFirstCard());
        assertNull(viewModel.getSecondCard());

        // Kiểm tra State lật thẻ đã cập nhật chưa
        assertNotNull(viewModel.stateFlipTwoCard.getValue());
        assertEquals(StateFlipTwoCard.FLIP_UP_NOW, viewModel.stateFlipTwoCard.getValue().getState());
    }

    @Test
    public void testSelectSameCardTwice_ShouldShowError() {
        Card card1 = mockCards.get(0);

        // Chọn lần 1
        viewModel.onCardClick(card1);

        // Chọn lại chính thẻ đó (Rẽ nhánh 2.1.5)
        viewModel.onCardClick(card1);

        // Kiểm tra thông báo lỗi (2.3.1)
        assertEquals("Không được chọn lại thẻ đã lật", viewModel.notifyMessage.getValue());
    }

    @Test
    public void testMatchTwoCards() {
        Card card1 = mockCards.get(0); // Cặp 101
        Card card2 = mockCards.get(1); // Cặp 101

        viewModel.onCardClick(card1);
        viewModel.onCardClick(card2);

        // Kiểm tra trạng thái khớp thẻ (2.1.9)
        assertEquals(StateFlipTwoCard.DISABLE_TWO_CARD_NOW, viewModel.stateFlipTwoCard.getValue().getState());
    }

    @Test
    public void testNotMatchTwoCards() {
        Card card1 = mockCards.get(0); // Cặp 101
        Card card3 = mockCards.get(2); // Cặp 102

        viewModel.onCardClick(card1);
        viewModel.onCardClick(card3);

        // Kiểm tra trạng thái không khớp (2.2.1)
        assertEquals(StateFlipTwoCard.NOT_MATCH, viewModel.stateFlipTwoCard.getValue().getState());
    }
}