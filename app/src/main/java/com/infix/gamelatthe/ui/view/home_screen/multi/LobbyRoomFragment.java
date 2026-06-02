package com.infix.gamelatthe.ui.view.home_screen.multi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.common.UserRole;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentLobbyRoomBinding;
import com.infix.gamelatthe.ui.viewmodel.LobbyRoomViewModel;

public class LobbyRoomFragment extends Fragment {
    private FragmentLobbyRoomBinding binding;

    private static final String ARG_USER_ROLE = "ARG_USER_ROLE";
    private static final String ARG_ROOM_CODE = "ARG_ROOM_CODE";

    private String userRole;
    private String roomCode;

    private LobbyRoomViewModel lobbyRoomViewModel;

    public static LobbyRoomFragment newInstance(String userRole, String roomCode) {
        LobbyRoomFragment fragment = new LobbyRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ROLE, userRole);
        args.putString(ARG_ROOM_CODE, roomCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userRole = getArguments().getString(ARG_USER_ROLE);
            roomCode = getArguments().getString(ARG_ROOM_CODE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLobbyRoomBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lobbyRoomViewModel = new ViewModelProvider(requireActivity()).get(LobbyRoomViewModel.class);
        lobbyRoomViewModel.notifyMsg.observe(getViewLifecycleOwner(), this::showMessage);
        initUI();
    }


    private void initUI() {
        String labelRoom = userRole.equals(UserRole.HOST.role)?"CHỜ NGƯỜI CHƠI THAM GIA":"ĐỢI CHỦ PHÒNG BẮT ĐẦU";
        binding.tvLabelLobbyRoom.setText(labelRoom);
        binding.tvShowRoomcodeLobbyRoom.setText("Mã phòng: " + roomCode);

        if ("GUEST".equals(userRole)) {
            binding.btnStartGameLobbyRoom.setVisibility(View.GONE);
        } else {
            binding.btnStartGameLobbyRoom.setEnabled(false);
            binding.btnStartGameLobbyRoom.setText("Chờ đối thủ...");
        }

        if (roomCode != null) {
            lobbyRoomViewModel.startListeningToRoomByCode(roomCode, new RoomSnapshotCallback() {
                @Override
                public void onDataChanged(RoomOnline room) {
                    lobbyRoomViewModel.setRoomOnlineState(room);
                }

                @Override
                public void onError(Exception e) {
                    showMessage(e.getMessage());
                }
            });
        }

        lobbyRoomViewModel.roomData.observe(getViewLifecycleOwner(), this::handleRoomUpdate);

        binding.btnLeaveRoomLobbyRoom.setOnClickListener(v -> {
            //back
        });

        binding.btnStartGameLobbyRoom.setOnClickListener(v -> {
            if ("HOST".equals(userRole)) {
                Toast.makeText(requireContext(), "Đang chuẩn bị trận đấu...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void handleRoomUpdate(RoomOnline room) {
        if (room == null || room.getPlayers() == null) return;

        PlayerOnline host = null;
        PlayerOnline guest = null;

        for (PlayerOnline player : room.getPlayers()) {
            if ("HOST".equals(player.getRole())) {
                host = player;
            } else if ("GUEST".equals(player.getRole())) {
                guest = player;
            }
        }

        if (host != null) {
            binding.tvLabelNameHostLobbyRoom.setText(getString(R.string.name_host, host.getName()));
        }

        if (guest != null) {
            binding.tvLabelNameGuestLoobyRoom.setText(getString(R.string.name_guest, guest.getName()));

            if ("HOST".equals(userRole)) {
                binding.btnStartGameLobbyRoom.setEnabled(true);
                binding.btnStartGameLobbyRoom.setText("Bắt đầu ván đấu");
            }
        } else {
            // Trường hợp phòng chưa có đối thủ vào, hoặc đối thủ vừa bấm nút thoát ra ngoài
            binding.tvLabelNameGuestLoobyRoom.setText("Đối thủ (Guest): Đang chờ đối thủ tham gia...");
            if ("HOST".equals(userRole)) {
                binding.btnStartGameLobbyRoom.setEnabled(false);
                binding.btnStartGameLobbyRoom.setText("Chờ đối thủ...");
            }
        }

        //chuyen man hinh
        if ("PLAYING".equals(room.getStatus())) {
            navigateToBoardGameOnline(room);
        }
    }

    private void navigateToBoardGameOnline(RoomOnline room) {

    }

    private void showMessage(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        lobbyRoomViewModel.resetAllState();
    }
}