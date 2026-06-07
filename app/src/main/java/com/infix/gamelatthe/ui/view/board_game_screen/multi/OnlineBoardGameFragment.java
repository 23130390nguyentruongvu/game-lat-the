package com.infix.gamelatthe.ui.view.board_game_screen.multi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentOnlineBoardGameBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.view.board_game_screen.BoardGameAdapter;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;

public class OnlineBoardGameFragment extends Fragment {
    private FragmentOnlineBoardGameBinding binding;
    private static final String ARG_ROOM_ONLINE = "ARG_ROOM_ONLINE";

    private OnlineBoardGameViewModel onlineBoardGameViewModel;
    private BoardGameAdapter boardGameAdapter;
    private String currentUserId;

    private RoomOnline roomOnline;
    private boolean isGameOver = false;

    public static OnlineBoardGameFragment newInstance(RoomOnline roomOnline) {
        OnlineBoardGameFragment fragment = new OnlineBoardGameFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROOM_ONLINE, roomOnline);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                roomOnline = (RoomOnline) getArguments().getSerializable(ARG_ROOM_ONLINE);
            } catch (Exception e) {
                Log.e("OnlineBoardGameFragment", "Error getting args: " + e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnlineBoardGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(MainActivity.FILE_INFO_USER, Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString(MainActivity.KEY_UUID_USER, "");

        onlineBoardGameViewModel = new ViewModelProvider(this).get(OnlineBoardGameViewModel.class);

        setupRecyclerView();
        observeRoomData();

        binding.ivBack.setOnClickListener(v -> showConfirmAbandonDialog());
    }

    private void setupRecyclerView() {
        boardGameAdapter = new BoardGameAdapter(card -> {
            // [8.1.9] Khóa tương tác nếu game đã kết thúc
            if (isGameOver) return;
            if (roomOnline == null) return;

            // [7.1.1] Kiểm tra lượt đi
            if (roomOnline.getCurrentTurn() != null && !roomOnline.getCurrentTurn().equals(currentUserId)) {
                // [7.3.3] Thông báo sai lượt
                Toast.makeText(requireContext(), "Chưa tới lượt của bạn!", Toast.LENGTH_SHORT).show();
                return;
            }

            // [7.1.0] Người chơi chạm vào thẻ
            if (card instanceof CardOnline) {
                // [8.1.0] Kích hoạt logic lật thẻ
                onlineBoardGameViewModel.onCardClick((CardOnline) card, roomOnline, currentUserId);
            } else {
                Log.e("OnlineBoardGameFragment", "Lỗi dữ liệu: Thẻ không phải CardOnline");
            }
        });

        binding.rvBoardGame.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvBoardGame.setAdapter(boardGameAdapter);

        if (roomOnline != null && roomOnline.getBoardGame() != null) {
            boardGameAdapter.updateCards(roomOnline.getBoardGame().getCards());
        }
    }

    private void observeRoomData() {
        if (roomOnline != null) {
            onlineBoardGameViewModel.startListeningToRoomByCode(roomOnline.getRoomCode());
        }

        onlineBoardGameViewModel.roomOnline.observe(getViewLifecycleOwner(), room -> {
            if (room != null && room.getBoardGame() != null) {
                this.roomOnline = room;

                // [7.1.3] Cập nhật giao diện đồng bộ theo Firestore
                boardGameAdapter.updateCards(room.getBoardGame().getCards());

                // [8.1.9] Kiểm tra kết thúc từ Snapshot
                if (!isGameOver && room.getStatus() != null &&
                        (room.getStatus().equals("FINISHED") || room.getStatus().equals("ABANDONED"))) {
                    isGameOver = true;
                    showGameOverDialog(room.getWinnerId());
                }
            }
        });

        // [8.1.9] Tín hiệu kết thúc từ ViewModel
        onlineBoardGameViewModel.gameOverEvent.observe(getViewLifecycleOwner(), winnerId -> {
            if (winnerId != null && !isGameOver) {
                isGameOver = true;
                showGameOverDialog(winnerId);
            }
        });

        // [8.3.4] Thông báo lỗi mạng
        onlineBoardGameViewModel.networkError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null && isError) showNetworkErrorDialog();
        });
    }

    private void showConfirmAbandonDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận rời trận")
                .setMessage("Bạn có chắc chắn muốn bỏ cuộc? Đối thủ sẽ lập tức giành chiến thắng.")
                .setPositiveButton("Bỏ cuộc", (dialog, which) -> {
                    // [8.2.2] Kích hoạt abandonGame
                    onlineBoardGameViewModel.abandonGame(currentUserId, roomOnline);
                })
                .setNegativeButton("Ở lại", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showGameOverDialog(String winnerId) {
        if (getContext() == null) return;

        String title, msg;
        if ("DRAW".equals(winnerId)) {
            title = "KẾT QUẢ HÒA!";
            msg = "Cả hai đều có số điểm bằng nhau!";
        } else if (currentUserId.equals(winnerId)) {
            title = "CHIẾN THẮNG! 🎉";
            msg = "Bạn đã giành chiến thắng!";
        } else {
            title = "THUA CUỘC 😢";
            msg = "Đối thủ đã giành chiến thắng!";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title).setMessage(msg)
                .setPositiveButton("Quay về Lobby", (dialog, which) -> {
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton("Màn hình chính", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(requireActivity(), MainActivity.class);
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lỗi kết nối mạng")
                .setMessage("Lỗi kết nối đồng bộ kết quả. Hệ thống đã lưu trữ tạm thời và sẽ tự động cập nhật lại khi mạng ổn định.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
