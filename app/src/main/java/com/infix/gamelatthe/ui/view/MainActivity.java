package com.infix.gamelatthe.ui.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.model.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
      //  initDataForFirestore();
    }

    /**
     * Cac thanh vien sau khi them google-service.json thi tien hanh
     * chay doan code nay de khoi tao du lieu len Firestore
     */
    private void initDataForFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch =  db.batch();

        //Collection BoardGame
        CollectionReference boardGm =  db.collection("boards");
        DocumentReference ez = boardGm.document(DifficultyEnum.EASY+"");
        CollectionReference cardEz = ez.collection("cards");
        for(Card card: getCardsForEasy()) {
            DocumentReference doc = cardEz.document(card.getId()+"");
            batch.set(doc, card);
        }

        DocumentReference normal = boardGm.document(DifficultyEnum.NORMAL+"");
        CollectionReference cardNor = normal.collection("cards");
        for(Card card: getCardsForNormal()) {
            DocumentReference doc = cardNor.document(card.getId()+"");
            batch.set(doc, card);
        }

        DocumentReference hard = boardGm.document(DifficultyEnum.HARD+"");
        CollectionReference cardHard = hard.collection("cards");
        for(Card card: getCardsForHard()) {
            DocumentReference doc = cardHard.document(card.getId()+"");
            batch.set(doc, card);
        }

        CollectionReference levelRef = db.collection("levels");
        HashMap map = new HashMap<String, String>();
        map.put(DifficultyEnum.EASY.toString(), DifficultyEnum.EASY.toString());
        batch.set(levelRef.document(DifficultyEnum.EASY+""), map);
        map.clear();
        map.put(DifficultyEnum.NORMAL.toString(), DifficultyEnum.NORMAL.toString());
        batch.set(levelRef.document(DifficultyEnum.NORMAL+""), map);
        map.clear();
        map.put(DifficultyEnum.HARD.toString(), DifficultyEnum.HARD.toString());
        batch.set(levelRef.document(DifficultyEnum.HARD+""), map);

        batch.commit();
    }

    public  List<Card> getCardsForEasy() {
        List<Card> cards = new ArrayList<>();
        String[] imgs = new String[]{
                "https://www.vhv.rs/dpng/d/181-1810085_anime-and-chibi-image-sakura-card-captor-chibi.png",
                "https://www.vhv.rs/dpng/d/431-4312466_transparent-chibi-eyes-png-kawaii-chibi-anime-cute.png",
                "https://www.vhv.rs/dpng/d/425-4255991_chibi-girl-anime-chibi-girl-png-transparent-png.png"
        };
        int cardId = 1;
        int numberOfPairs = 3;

        for (int groupId = 1; groupId <= numberOfPairs; groupId++) {
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
        }

        Collections.shuffle(cards);

        return cards;
    }

    public static List<Card> getCardsForNormal() {
        String[] imgs = new String[]{
                "https://www.vhv.rs/dpng/d/181-1810085_anime-and-chibi-image-sakura-card-captor-chibi.png",
                "https://www.vhv.rs/dpng/d/431-4312466_transparent-chibi-eyes-png-kawaii-chibi-anime-cute.png",
                "https://www.vhv.rs/dpng/d/425-4255991_chibi-girl-anime-chibi-girl-png-transparent-png.png",
                "https://www.vhv.rs/dpng/d/434-4342983_chibi-girl-in-a-cat-vest-by-sannyvampire.png",
                "https://www.vhv.rs/dpng/d/449-4494420_png-image-with-transparent-background-brown-hair-chibi.png"
        };
        List<Card> cards = new ArrayList<>();
        int cardId = 1;
        int numberOfPairs = 5;
        for (int groupId = 1; groupId <= numberOfPairs; groupId++) {
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
        }
        Collections.shuffle(cards);

        return cards;
    }

    public static List<Card> getCardsForHard() {
        String[] imgs = new String[]{
                "https://www.vhv.rs/dpng/d/181-1810085_anime-and-chibi-image-sakura-card-captor-chibi.png",
                "https://www.vhv.rs/dpng/d/431-4312466_transparent-chibi-eyes-png-kawaii-chibi-anime-cute.png",
                "https://www.vhv.rs/dpng/d/425-4255991_chibi-girl-anime-chibi-girl-png-transparent-png.png",
                "https://www.vhv.rs/dpng/d/434-4342983_chibi-girl-in-a-cat-vest-by-sannyvampire.png",
                "https://www.vhv.rs/dpng/d/449-4494420_png-image-with-transparent-background-brown-hair-chibi.png",
                "https://www.vhv.rs/dpng/d/234-2343502_transparent-anime-girl-with-brown-hair-png-cute.png",
                "https://www.vhv.rs/dpng/d/418-4186543_cartoon-character-girl-with-blue-eyes-and-brown.png"
        };
        List<Card> cards = new ArrayList<>();
        int cardId = 1;
        int numberOfPairs = 7;

        for (int groupId = 1; groupId <= numberOfPairs; groupId++) {
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
            cards.add(new Card(cardId++, groupId, imgs[groupId-1], false));
        }

        Collections.shuffle(cards);

        return cards;
    }
}