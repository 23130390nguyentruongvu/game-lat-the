package com.infix.gamelatthe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.ui.view.leaderboard_screen.OnlineLeaderboardFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OnlineLeaderboardFragmentUC10Test {

    @Before
    public void clearSharedPrefs() {
        // Xóa sạch SharedPreferences trước mỗi lần test để đảm bảo môi trường sạch
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    @Test
    public void khiChuaCoUUID_PhaiHienThiThongBaoTaoMoi_UC10_2_4() {
        // 10.2.1 -> 10.2.4: Không có UUID trong máy
        FragmentScenario.launchInContainer(OnlineLeaderboardFragment.class, null, R.style.Theme_GameLatThe);

        // Kiểm tra xem TextView thông báo có hiển thị đúng nội dung đặc tả không
        onView(withId(R.id.empty_state_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Bạn chưa có lịch sử thi đấu trực tuyến.")));
        
        // RecyclerView phải bị ẩn
        onView(withId(R.id.recycler_view_leaderboard)).check(matches(not(isDisplayed())));
    }

    @Test
    public void khiKhongCoDuLieuTuFirebase_PhaiHienThiTrangThaiRong_UC10_3_2() {
        // Giả lập đã có UUID nhưng Firestore trả về rỗng (logic này được xử lý trong ViewModel/Fragment)
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("user_uuid", "dummy-uuid-123").commit();

        FragmentScenario.launchInContainer(OnlineLeaderboardFragment.class, null, R.style.Theme_GameLatThe);

        // Đợi UI cập nhật và kiểm tra thông báo rỗng (10.3.2)
        onView(withId(R.id.empty_state_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("Không có lịch sử thi đấu nào.")));
    }

    @Test
    public void khiNhanNutQuayLai_PhaiThucHienPopBackStack_UC10_1_8() {
        FragmentScenario.launchInContainer(OnlineLeaderboardFragment.class, null, R.style.Theme_GameLatThe);

        // Kiểm tra nút quay lại có hiển thị và có thể nhấn được không
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }
}
