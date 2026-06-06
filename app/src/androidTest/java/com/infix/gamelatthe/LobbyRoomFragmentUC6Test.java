package com.infix.gamelatthe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.ui.view.home_screen.multi.LobbyRoomFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LobbyRoomFragmentUC6Test {

    @Test
    public void khiUserLaGuest_NutBatDauGame_PhaiBiAn() {
        Bundle args = new Bundle();
        args.putString("ARG_USER_ROLE", "GUEST");
        args.putString("ARG_ROOM_CODE", "123456");

        FragmentScenario.launchInContainer(LobbyRoomFragment.class, args, R.style.Theme_GameLatThe);

        onView(withId(R.id.btn_start_game_lobby_room)).check(matches(not(isDisplayed())));
    }

    @Test
    public void khiUserLaHost_NutBatDauGame_PhaiHienThi() {
        Bundle args = new Bundle();
        args.putString("ARG_USER_ROLE", "HOST");
        args.putString("ARG_ROOM_CODE", "123456");

        FragmentScenario.launchInContainer(LobbyRoomFragment.class, args, R.style.Theme_GameLatThe);

        onView(withId(R.id.btn_start_game_lobby_room)).check(matches(isDisplayed()));
    }
}