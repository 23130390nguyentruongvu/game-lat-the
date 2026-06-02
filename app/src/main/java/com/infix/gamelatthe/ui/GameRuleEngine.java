package com.infix.gamelatthe.ui;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;

import com.infix.gamelatthe.data.model.PlayerOnline;

import java.util.List;

public class GameRuleEngine {
    private BoardGame boardGame;

    public GameRuleEngine(BoardGame boardGame) {
        this.boardGame = boardGame;
    }

    public boolean matchTwoCard(Card card1, Card card2){
        return  card1.getGroupId() == card2.getGroupId();
    }
    //3.1.2 ViewModel gọi để kiểm tra trạng thái ván chơi
    public boolean checkEndGame() {
        return checkAllCardFlipped();
    }
    //3.1.3 - Kiểm tra tất cả thẻ và xác định trạng thái ván chơi
    private boolean checkAllCardFlipped() {
        return boardGame.checkAllCardFlipped();
    }
    // 3.1.5 + 3.1.6 tính toán và trả về thời gian hoàn thành
    public Long calcTimeFinish() {
        return boardGame.calcTimeFinish();
    }
    public void trackEndTime(Long currentTimeMillis) {
        boardGame.setTimeEnd(currentTimeMillis);
    }

    public List<Card> getCards() {
        return boardGame.getCards();
    }

    public BoardGame getBoardGame() {
        return boardGame;
    }
    // 8.1.2 Thực hiện quét danh sách và xác nhận tất cả các thẻ bài đã được ghép trúng.
    public boolean checkAllCardMatched(List<Card> cards) {
        for (Card card : cards) {
            if (card.isEnable()) {
                return false;
            }
        }
        return true;
    }
}
