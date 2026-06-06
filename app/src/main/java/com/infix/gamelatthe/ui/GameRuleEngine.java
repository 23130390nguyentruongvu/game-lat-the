package com.infix.gamelatthe.ui;

import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
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
    /* * 8.1.2: ViewModel gọi sang lớp GameRuleEngine thực hiện hàm checkAllCardMatched().
     * GameRuleEngine thực hiện quét danh sách CardOnline và xác nhận tất cả các thẻ bài
     * đã được ghép trúng (isMatched == true), trả về kết quả true.
     */    public boolean checkOnlineEndGame(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getBoardGame() == null || roomOnline.getBoardGame().getCards() == null) {
            return false;
        }
        List<Card> cards = roomOnline.getBoardGame().getCards();
        for (Card c : cards) {
            if (c instanceof CardOnline) {
                if (!((CardOnline) c).isMatched()) {
                    return false;
                }
            } else {
                if (!c.isFlipped()) return false;
            }
        }
        return true;
    }
    /* * 8.1.4: GameRuleEngine thực hiện so sánh thuộc tính score (điểm số) hiện tại của
     * hai đối tượng PlayerOnline (Host và Guest), tìm ra người cao điểm hơn và trả về mã winnerId tương ứng.
     */    public String calculateOnlineWinner(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getPlayers() == null || roomOnline.getPlayers().isEmpty()) {
            return "DRAW";
        }

        List<PlayerOnline> players = roomOnline.getPlayers();
        if (players.size() == 1) {
            return players.get(0).getUuid();
        }

        PlayerOnline p1 = players.get(0);
        PlayerOnline p2 = players.get(1);

        if (p1.getScore() > p2.getScore()) {
            return p1.getUuid();
        } else if (p2.getScore() > p1.getScore()) {
            return p2.getUuid();
        } else {
            return "DRAW";
        }
    }
}
