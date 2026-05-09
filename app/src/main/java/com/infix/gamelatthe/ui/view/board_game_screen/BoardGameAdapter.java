package com.infix.gamelatthe.ui.view.board_game_screen;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.OnCardClick;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.databinding.ItemCardBinding;

import java.util.ArrayList;
import java.util.List;

public class BoardGameAdapter extends RecyclerView.Adapter<BoardGameAdapter.ViewHolder> {
    private OnCardClick onCardClick;
    private List<Card> cards = new ArrayList<>();

    public boolean areAllCardsDisabled() {
        for (Card card : cards) {
            if (card.isEnable()) {
                return false;
            }
        }
        return true;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardBinding binding;

        public ViewHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(Card card) {
            String url = card.isFlipped()?card.getUrlImage():"https://www.emojiall.com/images/60/skype/2753.png";

            Glide.with(binding.getRoot())
                    .load(url)
                    .error(R.drawable.error_network)
                    .centerCrop()
                    .into(binding.imgCardItem);

            //2.1.1 Người chơi chọn một thẻ trên giao diện
            binding.getRoot().setOnClickListener(v->onCardClick.onCardClick(card));
        }
    }

    public BoardGameAdapter(OnCardClick onCardClick) {
        this.onCardClick = onCardClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(cards.get(position));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCards(List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
        notifyDataSetChanged();
    }

    public void updateItemCard(Card card) {
        int index = cards.indexOf(card);
        if(index != -1)
            notifyItemChanged(index);
    }

    public void disableTwoCard(Card card1, Card card2) {
        card1.setEnable(false);
        card2.setEnable(false);

        int index1 = cards.indexOf(card1);
        int index2 = cards.indexOf(card2);

        if(index1 != -1)
            notifyItemChanged(index1);
        if(index2 != -1)
            notifyItemChanged(index2);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void flipBackTwoCard(Card card1, Card card2) {
        card1.setFlipped(false);
        card2.setFlipped(false);

        int index1 = cards.indexOf(card1);
        int index2 = cards.indexOf(card2);

        if(index1 != -1)
            notifyItemChanged(index1);
        if(index2 != -1)
            notifyItemChanged(index2);
    }
}
