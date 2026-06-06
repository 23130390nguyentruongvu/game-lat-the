package com.infix.gamelatthe.ui.view.home_screen.multi;

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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.auth.User;
import com.infix.gamelatthe.MyApplication;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.common.UserRole;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentLobbyRoomBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.view.board_game_screen.multi.OnlineBoardGameFragment;
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
        registerObserver();
        initUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        unregisterObserver();
        lobbyRoomViewModel.resetAllState();
    }

    private void registerObserver() {
        try {
            MyApplication myApplication = (MyApplication) requireActivity().getApplication();
            myApplication.registerObserver(lobbyRoomViewModel);
        } catch (Exception e) {
            Log.e("SVU", e.getMessage());
        }
    }

    private void unregisterObserver() {
        try {
            MyApplication myApplication = (MyApplication) requireActivity().getApplication();
            myApplication.removeObserver(lobbyRoomViewModel);
        } catch (Exception e) {
            Log.e("SVU", e.getMessage());
        }
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
            //6.1.7 Đối thủ thực hiện nhập mã để kết nối vào phòng chơi.
            // Hệ thống Firestore Real-time Listener nhận biết phòng đã đủ
            // 2/2 người (trường players.guest đã được điền dữ liệu), lập tức
            // cập nhật giao diện hiển thị thông tin đối thủ trên cả hai máy và
            // kích hoạt trạng thái sẵn sàng (isEnabled = true) cho nút "Bắt đầu ván đấu" trên máy Host.

            //6.2.6 Quay trở lại bước 6.1.7 của Luồng chính.
            lobbyRoomViewModel.startListeningToRoomByCode(roomCode, new RoomSnapshotCallback() {
                @Override
                public void onDataChanged(RoomOnline room) {
                    Log.d("SVU", room.toString());
                    lobbyRoomViewModel.setRoomOnlineState(room);
                }

                @Override
                public void onError(Exception e) {
                    showMessage(e.getMessage());
                }
            });
        }

        lobbyRoomViewModel.roomData.observe(getViewLifecycleOwner(), this::handleRoomUpdate);

        //6.3.1 Người chơi nhấn nút "Hủy" hoặc nút "Rời phòng" trên giao diện phòng chờ.
        binding.btnLeaveRoomLobbyRoom.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(
                    MainActivity.FILE_INFO_USER,
                    Context.MODE_PRIVATE
            );
            String uuid = sharedPreferences.getString(MainActivity.KEY_UUID_USER, null);
            if(uuid == null) {
                showMessage("UUID của người dùng không tồn tại");
                return;
            }

            lobbyRoomViewModel.leaveRoomOnline(uuid, roomCode, new RoomOnlineListener() {
                @Override
                public void onSuccess(String message) {
                    //6.3.3 Hệ thống đưa người chơi vừa thoát quay lại màn hình chính ở bước 6.1.0.
                    FragmentManager fm = requireActivity().getSupportFragmentManager();
                    fm.popBackStack();
                    fm.popBackStack();
                }

                @Override
                public void onFailure() {
                    showMessage("Rời phòng thất bại");
                }
            });
        });

        //6.1.8 Host nhấn nút "Bắt đầu ván đấu".
        binding.btnStartGameLobbyRoom.setOnClickListener(v -> {
            if ("HOST".equals(userRole)) {
                Toast.makeText(requireContext(), "Đang chuẩn bị trận đấu...", Toast.LENGTH_SHORT).show();
                lobbyRoomViewModel.startGameOnline(roomCode, new RoomOnlineListener() {
                    @Override
                    public void onSuccess(String message) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
    }

    private void handleRoomUpdate(RoomOnline room) {
        if(room == null && userRole.equals(UserRole.GUEST.role)) {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            fm.popBackStack();
            fm.popBackStack();
            return;
        }
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
            binding.tvLabelNameGuestLoobyRoom.setText("Đối thủ (Guest): Đang chờ đối thủ tham gia...");
            if ("HOST".equals(userRole)) {
                binding.btnStartGameLobbyRoom.setEnabled(false);
                binding.btnStartGameLobbyRoom.setText("Chờ đối thủ...");
            }
        }

        //6.1.10 Hệ thống lắng nghe dữ liệu trạng thái bàn chơi trên
        // Firestore và chuyển sang màn hình lật thẻ khi trạng thái là “PLAYING”.
        if ("PLAYING".equals(room.getStatus())) {
            navigateToBoardGameOnline(room);
        }
    }

    private void navigateToBoardGameOnline(RoomOnline room) {
        Log.d("LIL", room.toString());
        showMessage("Bắt đầu game");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcv_main, OnlineBoardGameFragment.newInstance(room))
                .addToBackStack(null)
                .commit();
    }

    private void showMessage(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }
}