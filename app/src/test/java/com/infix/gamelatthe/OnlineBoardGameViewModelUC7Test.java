package com.infix.gamelatthe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class OnlineBoardGameViewModelUC7Test {

    @Rule
    public TestRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GameRepository mockRepository;

    private OnlineBoardGameViewModel viewModel;
    private RoomOnline mockRoom;
    private String myUserId = "sinh_123";
    private String opponentId = "vu_456";

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Truyền mockRepository vào để chặn gọi Firebase
        viewModel = new OnlineBoardGameViewModel(mockRepository);

        List<Card> mockCards = new ArrayList<>();
        mockCards.add(new CardOnline(1, 101, "url1", false));
        mockCards.add(new CardOnline(2, 101, "url1", false));
        mockCards.add(new CardOnline(3, 102, "url2", false));

        BoardGame boardGame = new BoardGame(mockCards, System.currentTimeMillis());

        mockRoom = new RoomOnline();
        mockRoom.setBoardGame(boardGame);

        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline(myUserId, "Người chơi 1", 0, true, "host"));
        players.add(new PlayerOnline(opponentId, "Người chơi 2", 0, true, "member"));

        mockRoom.setPlayers(players);
        mockRoom.setCurrentTurn(myUserId);
    }

    @Test
    public void testWrongTurn_ShouldNotFlipCard() {
        mockRoom.setCurrentTurn(opponentId);
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0);

        viewModel.onCardClick(card1, mockRoom, myUserId);

        assertFalse(card1.isFlipped());
    }

    @Test
    public void testMatchTwoCards_ShouldIncreaseScoreAndKeepTurn() {
        CardOnline card1 = (CardOnline) mockRoom.getBoardGame().getCards().get(0);
        CardOnline card2 = (CardOnline) mockRoom.getBoardGame().getCards().get(1);

        viewModel.onCardClick(card1, mockRoom, myUserId);
        viewModel.onCardClick(card2, mockRoom, myUserId);

        assertTrue(card1.isMatched());
        PlayerOnline me = mockRoom.getPlayers().get(0);
        assertEquals(1, me.getScore());
        assertEquals(myUserId, mockRoom.getCurrentTurn());
    }
}