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

import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentOnlineBoardGameBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.view.board_game_screen.BoardGameAdapter;
import com.infix.gamelatthe.ui.viewmodel.LobbyRoomViewModel;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;


public class OnlineBoardGameFragment extends Fragment {
    private FragmentOnlineBoardGameBinding binding;
    private static final String ARG_ROOM_ONLINE = "ARG_ROOM_ONLINE";

    private LobbyRoomViewModel lobbyRoomViewModel;
    private OnlineBoardGameViewModel onlineBoardGameViewModel;
    private BoardGameAdapter boardGameAdapter;
    private String currentUserId;

    private RoomOnline roomOnline;

    // Thêm cờ để khóa bàn cờ theo bước 8.1.9
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
                Log.e("OnlineBoardGameFragment", e.getMessage());
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

        lobbyRoomViewModel = new ViewModelProvider(requireActivity()).get(LobbyRoomViewModel.class);
        onlineBoardGameViewModel = new ViewModelProvider(this).get(OnlineBoardGameViewModel.class);

        setupRecyclerView();
        observeRoomData();
        // 8.2.1: Trong khi trận đấu đang diễn ra, người chơi nhấn chọn nút "Thoát trận" trên giao diện
        binding.ivBack.setOnClickListener(v -> showConfirmAbandonDialog());
    }

    private void setupRecyclerView() {
        boardGameAdapter = new BoardGameAdapter(card -> {
            // Nếu game đã kết thúc, khóa tương tác không cho lật bài nữa (Bước 8.1.9)
            if (isGameOver) return;
            if (card instanceof CardOnline && roomOnline != null) {
                if (roomOnline.getCurrentTurn() != null && !roomOnline.getCurrentTurn().equals(currentUserId)) {
                    Log.d("SKU", roomOnline.getCurrentTurn() + " " + currentUserId);
                    // 7.3.3 Hiển thị Toast cảnh báo "Chưa tới lượt của bạn!".
                    Toast.makeText(requireContext(), "Chưa tới lượt của bạn!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 8.1.0: Người chơi thực hiện click và lật thành công cặp bài
                onlineBoardGameViewModel.onCardClick((CardOnline) card, roomOnline, currentUserId);
            }
        });

        binding.rvBoardGame.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvBoardGame.setAdapter(boardGameAdapter);

        if (roomOnline != null && roomOnline.getBoardGame() != null) {
            boardGameAdapter.updateCards(roomOnline.getBoardGame().getCards());
        }
    }

    private void observeRoomData() {
        onlineBoardGameViewModel.startListeningToRoomByCode(roomOnline.getRoomCode());
        onlineBoardGameViewModel.roomOnline.observe(getViewLifecycleOwner(), room -> {
            if (room != null && room.getBoardGame() != null) {
                this.roomOnline = room;

                // 7.1.3 Trình lắng nghe nhận sự kiện, đồng bộ hoạt họa lật ngửa thẻ.
                boardGameAdapter.updateCards(room.getBoardGame().getCards());
                if (!isGameOver && room.getStatus() != null &&
                        (room.getStatus().equals("FINISHED") || room.getStatus().equals("ABANDONED"))) {

                    String winnerId = room.getWinnerId();
                    if (winnerId != null && !winnerId.isEmpty()) {
                        isGameOver = true; // Đóng băng bàn cờ ngay lập tức
                        showGameOverDialog(winnerId); // Hiện Dialog kết quả cho máy người thua
                    }
                }
            }
        });
        // 8.1.9 & 8.2.7: Giao diện nhận tín hiệu đồng bộ thay đổi trạng thái, thực hiện khóa bàn và hiện Dialog
        onlineBoardGameViewModel.gameOverEvent.observe(getViewLifecycleOwner(), winnerId -> {
            // Thêm check !isGameOver để tránh việc Dialog bị hiện 2 lần trùng nhau
            if (winnerId != null && !isGameOver) {
                isGameOver = true;
                showGameOverDialog(winnerId);
            }
        });

        // 8.3.4: Lỗi kết nối đồng bộ kết quả. Hệ thống hiển thị thông báo
        onlineBoardGameViewModel.networkError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null && isError) showNetworkErrorDialog();
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showConfirmAbandonDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận rời trận")
                .setMessage("Bạn có chắc chắn muốn bỏ cuộc? Đối thủ sẽ lập tức giành chiến thắng.")
                .setPositiveButton("Bỏ cuộc", (dialog, which) -> {
                    // 8.2.2: Giao diện cục bộ bắt được sự kiện và kích hoạt khẩn cấp hàm abandonGame
                    onlineBoardGameViewModel.abandonGame(currentUserId, roomOnline);
                })
                .setNegativeButton("Ở lại", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showGameOverDialog(String winnerId) {
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
                .setCancelable(false).show();
    }

    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lỗi kết nối mạng")
                .setMessage("Lỗi kết nối đồng bộ kết quả. Hệ thống đã lưu trữ tạm thời và sẽ tự động cập nhật lại khi mạng ổn định.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .show();
    }
}