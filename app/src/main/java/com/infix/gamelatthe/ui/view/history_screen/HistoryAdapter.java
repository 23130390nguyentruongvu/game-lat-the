package com.infix.gamelatthe.ui.view.history_screen;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.databinding.ItemHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<MatchHistoryItem> listData = new ArrayList<>();

    public void updateList(List<MatchHistoryItem> list) {
        this.listData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // ✔ FIX 1: đổi PlayHistory → MatchHistoryItem
        MatchHistoryItem item = listData.get(position);

        holder.binding.tvRank.setText(String.valueOf(position + 1));

        String opponentName = item.opponentName != null ? item.opponentName : "Unknown";
        holder.binding.tvPlayerName.setText(opponentName);

        long duration = item.playTime * 1000; // playTime đang là giây

        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;

        holder.binding.tvTime.setText(
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        );
    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemHistoryBinding binding;

        public ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}