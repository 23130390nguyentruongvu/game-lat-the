package com.infix.gamelatthe.ui.view.history_screen;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infix.gamelatthe.data.model.PlayHistory;
import com.infix.gamelatthe.databinding.ItemHistoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<PlayHistory> listData = new ArrayList<>();

    public void updateList(List<PlayHistory> list) {
        this.listData = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlayHistory item = listData.get(position);

        holder.binding.tvRank.setText(String.valueOf(position + 1));

        String playerName = item.playerName != null ? item.playerName : "Unknown";
        holder.binding.tvPlayerName.setText(playerName);

        long duration = item.endTime - item.initTime;

        if (duration < 0) duration = 0;

        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;

        // Định dạng hiển thị chuỗi "02:05"
        holder.binding.tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
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