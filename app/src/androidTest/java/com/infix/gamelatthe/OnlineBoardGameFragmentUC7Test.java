package com.infix.gamelatthe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import com.infix.gamelatthe.ui.view.board_game_screen.multi.OnlineBoardGameFragment;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.ui.view.board_game_screen.multi.OnlineBoardGameFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OnlineBoardGameFragmentUC7Test {

    @Test
    public void khiVaoPhongGame_BanCoRecyclerView_PhaiHienThi() {
        // Truyền dữ liệu giả lập cho Fragment y hệt sếp Vũ làm
        Bundle args = new Bundle();
        args.putString("ARG_USER_ROLE", "HOST");
        args.putString("ARG_ROOM_CODE", "ROOM_LAT_THE_999");

        // Bật Fragment lên
        FragmentScenario.launchInContainer(OnlineBoardGameFragment.class, args, R.style.Theme_GameLatThe);

        // Kiểm tra xem cái RecyclerView bàn cờ đã hiển thị lên màn hình chưa
        onView(withId(R.id.rvBoardGame)).check(matches(isDisplayed()));
    }
}