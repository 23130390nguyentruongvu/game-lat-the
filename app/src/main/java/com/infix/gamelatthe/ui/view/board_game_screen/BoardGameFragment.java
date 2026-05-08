package com.infix.gamelatthe.ui.view.board_game_screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.databinding.FragmentBoardGameBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;

public class BoardGameFragment extends Fragment {
    private FragmentBoardGameBinding binding;
    private BoardGameAdapter boardGameAdapter;
    private BoardGameViewModel boardGameViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardGameBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initBoardGameViewModel();
        initRecyclerView();
    }

    private void initBoardGameViewModel() {
        boardGameViewModel = new ViewModelProvider(requireActivity()).get(BoardGameViewModel.class);
        observeStateFlipTwoCard();
        observeNotifyMessage();
    }

    private void initRecyclerView() {
        boardGameAdapter = new BoardGameAdapter(card -> {
            //2.1.2 View (Quản lí bởi Adapter) nhận sự kiện click và gửi card được chọn đến ViewModel
            boardGameViewModel.onCardClick(card);
        });
        binding.rvBoardGame.setAdapter(boardGameAdapter);
        boardGameAdapter.updateCards(boardGameViewModel.getCardsOfBoard());
    }

    private void observeStateFlipTwoCard() {

        boardGameViewModel.stateFlipTwoCard.observe(getViewLifecycleOwner(), state -> {
            switch (state.getState()) {
                //2.2.2 View nhận được thông báo và cập nhật thông báo không khớp
                case NOT_MATCH: {
                    showMessage("Không khớp");
                    break;
                }
                case MATCH:
                    break;
                case CHOOSE_DUPLICATE: {
                    break;
                }
                //2.2.5 View nhận trạng thái và thực hiện úp lại hai thẻ trên giao diện
                case FLIP_DOWN_NOW: {
                    boardGameAdapter.flipBackTwoCard(boardGameViewModel.getFirstCard(), boardGameViewModel.getSecondCard());
                    break;
                }
                //2.1.7 View quan sát thay đổi trạng thái và hiển thị trạng thái thẻ tương ứng
                case FLIP_UP_NOW: {
                    Card card = state.isFirstCard()
                            ?boardGameViewModel.getFirstCard()
                            :boardGameViewModel.getSecondCard();
                    card.setFlipped(true);
                    boardGameAdapter.updateItemCard(card);

                    break;
                }
                //2.1.10 View nhận được thông báo và update disable 2 card
                case DISABLE_TWO_CARD_NOW: {
                    boardGameAdapter.disableTwoCard(boardGameViewModel.getFirstCard(), boardGameViewModel.getSecondCard());
                    break;
                }
            }
        });
    }

    private void observeNotifyMessage() {
        //2.3.2 View quan sát nhận được trạng thái lỗi và hiển thị
        boardGameViewModel.notifyMessage.observe(getViewLifecycleOwner(), this::showMessage);
    }

    private void showMessage(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }
}