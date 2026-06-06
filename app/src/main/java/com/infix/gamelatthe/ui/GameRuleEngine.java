package com.infix.gamelatthe.ui;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import java.util.Collections;
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
        boolean isFinished = checkAllCardFlipped();
        
        if (isFinished) {
            // 3.1.4 GameRuleEngine trả về kết quả (kết thúc)
            return true;
        } else {
            // (Rẽ nhánh từ 3.1.4)
            // 3.2.1 GameRuleEngine xác định ván chơi chưa kết thúc do còn thẻ chưa lật
            // 3.2.2 GameRuleEngine trả về trạng thái chưa kết thúc
            return false;
        }
    }
    //3.1.3 - Kiểm tra tất cả thẻ và xác định trạng thái ván chơi
    private boolean checkAllCardFlipped() {
        return boardGame.checkAllCardFlipped();
    }
    // 3.1.5 ViewModel gọi GameRuleEngine để tính toán thời gian hoàn thành. + 3.1.6 GameRuleEngine trả về thời gian hoàn thành
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
  // 8.1.2 Kiểm tra trạng thái lật hết bài của phòng chơi trực tuyến.
    public boolean checkOnlineEndGame(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getBoardGame() == null) {
            return false;
        }
        return roomOnline.getBoardGame().checkAllCardFlipped();
    }
   // 8.1.3 & 8.1.4 Quét danh sách PlayerOnline, so sánh score và trả về mã uuid của người chiến thắng.
    public String calculateOnlineWinner(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getPlayers() == null || roomOnline.getPlayers().isEmpty()) {
            return "";
        }

        List<PlayerOnline> players = roomOnline.getPlayers();
        if (players.size() == 1) {
            return players.get(0).getUuid();
        }

        PlayerOnline player1 = players.get(0);
        PlayerOnline player2 = players.get(1);

        if (player1.getScore() > player2.getScore()) {
            return player1.getUuid();
        } else if (player2.getScore() > player1.getScore()) {
            return player2.getUuid();
        } else {
            return "DRAW";
        }
    }
}
