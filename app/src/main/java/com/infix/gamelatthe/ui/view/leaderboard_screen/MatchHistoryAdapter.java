package com.infix.gamelatthe.ui.view.leaderboard_screen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;

public class MatchHistoryAdapter extends ListAdapter<MatchHistoryItem, MatchHistoryAdapter.MatchHistoryViewHolder> {

    public MatchHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_history, parent, false);
        return new MatchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder holder, int position) {
        MatchHistoryItem item = getItem(position);
        holder.bind(item);
    }

    static class MatchHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOpponentName;
        private final TextView tvResult;
        private final TextView tvDifficulty;
        private final TextView tvPlayTime;
        private final TextView tvRole;
        private final TextView tvScore; // Added TextView for score

        public MatchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOpponentName = itemView.findViewById(R.id.tv_opponent_name);
            tvResult = itemView.findViewById(R.id.tv_result);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvPlayTime = itemView.findViewById(R.id.tv_play_time);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvScore = itemView.findViewById(R.id.tv_score); // Initialize TextView for score
        }

        public void bind(MatchHistoryItem item) {
            tvOpponentName.setText("Đối thủ: " + item.opponentName);
            tvResult.setText("Kết quả: " + item.result);
            tvDifficulty.setText("Độ khó: " + item.difficulty);
            tvPlayTime.setText("Thời gian: " + item.playTime + "s");
            tvRole.setText("Vai trò: " + item.role);
            tvScore.setText("Điểm: " + item.score); // Display the score
        }
    }

    private static final DiffUtil.ItemCallback<MatchHistoryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MatchHistoryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MatchHistoryItem oldItem, @NonNull MatchHistoryItem newItem) {
            return oldItem.roomId.equals(newItem.roomId); // Assuming roomId is unique
        }


        @Override
        public boolean areContentsTheSame(@NonNull MatchHistoryItem oldItem, @NonNull MatchHistoryItem newItem) {
            return oldItem.equals(newItem); // Requires MatchHistoryItem to implement equals()
        }
    };
}
