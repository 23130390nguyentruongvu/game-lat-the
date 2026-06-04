package com.infix.gamelatthe.ui.view.leaderboard_screen;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.databinding.ItemMatchHistoryBinding;
import com.infix.gamelatthe.common.OnCardClick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.MatchHistoryViewHolder> {

    private List<MatchHistoryItem> matchHistoryList = new ArrayList<>();
    private final OnCardClick onItemClick;

    public MatchHistoryAdapter(OnCardClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchHistoryBinding binding = ItemMatchHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MatchHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder holder, int position) {
        MatchHistoryItem item = matchHistoryList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return matchHistoryList.size();
    }

    /**
     * Cập nhật danh sách lịch sử trận đấu
     * @param newList Danh sách mới
     */
    public void updateMatchHistory(List<MatchHistoryItem> newList) {
        this.matchHistoryList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder cho mỗi item trong danh sách
     */
    static class MatchHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemMatchHistoryBinding binding;

        public MatchHistoryViewHolder(ItemMatchHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Bind dữ liệu vào UI
         */
        public void bind(MatchHistoryItem item) {
            // Tên đối thủ
            binding.tvOpponentName.setText("Vs: " + item.getOpponentName());

            // Kết quả (WIN/LOSE) và màu sắc
            if ("WIN".equals(item.getResult())) {
                binding.tvResult.setText("Thắng");
                binding.tvResult.setTextColor(binding.getRoot().getContext().getColor(android.R.color.holo_green_dark));
            } else {
                binding.tvResult.setText("Thua");
                binding.tvResult.setTextColor(binding.getRoot().getContext().getColor(android.R.color.holo_red_dark));
            }

            // Độ khó
            binding.tvDifficulty.setText("Độ khó: " + item.getDifficulty());

            // Vai trò (HOST/GUEST)
            binding.tvRole.setText("Vai trò: " + item.getRole());

            // So sánh điểm
            String scoreText = "Điểm: " + item.getScore() + " vs " + item.getOpponentScore();
            binding.tvScore.setText(scoreText);

            // Thời gian chơi
            if (item.getPlayDuration() != null && item.getPlayDuration() > 0) {
                long seconds = item.getPlayDuration() / 1000;
                long minutes = seconds / 60;
                long secs = seconds % 60;
                binding.tvPlayDuration.setText(String.format("Thời gian: %d:%02d", minutes, secs));
            } else {
                binding.tvPlayDuration.setText("Thời gian: --:--");
            }

            // Thời gian tạo phòng
            if (item.getCreateAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String dateStr = sdf.format(item.getCreateAt());
                binding.tvCreateAt.setText(dateStr);
            } else {
                binding.tvCreateAt.setText("--/--/---- --:--");
            }

            // Mã phòng
            binding.tvRoomCode.setText("Mã phòng: " + item.getRoomCode());
        }
    }
}