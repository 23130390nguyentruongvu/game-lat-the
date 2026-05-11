package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.model.Card;

import java.util.ArrayList;
import java.util.List;

public class RemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LevelsCallback {
        void onSuccess(List<String> levels);
        void onError(String error);
    }

    public interface BoardCallback {
        void onSuccess(List<Card> cards);
        void onError(String error);
    }

    // LOAD LEVELS
    public void getLevels(LevelsCallback callback) {
        db.collection("levels")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> levels = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        levels.add(doc.getId());
                    }
                    callback.onSuccess(levels);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // LOAD CARDS
    public void getBoard(DifficultyEnum level, BoardCallback callback) {
        db.collection("boards")
                .document(level.name())
                .collection("cards")
                .get()
                .addOnSuccessListener(query -> {

                    List<Card> cards = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        Card c = doc.toObject(Card.class);
                        cards.add(c);
                    }

                    callback.onSuccess(cards);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}