package com.example.slices.uitests.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test for AdminNotificationsFragment.
 * Tests loading, toolbar navigation, and recycler visibility.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminNotificationsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {

        scenario.getScenario().onActivity(MainActivity::switchToAdmin);

        Thread.sleep(1200);

        onView(allOf(withId(R.id.adminHomeFragment),
                isDescendantOfA(withId(R.id.bottom_nav_admin))))
                .perform(click());

        onView(withId(R.id.btn_logs)).perform(click());

        onView(withId(R.id.adminnotification_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Test that the list loads properly.
     */
    //Testing: US 03.08.01
    @Test
    public void testNotificationsFragmentLoads() {
        onView(withId(R.id.adminnotification_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Test toolbar back arrow returns to AdminHome.
     */
    @Test
    public void testToolbarBackButton() throws InterruptedException {
        onView(withId(R.id.adminnotification_toolbar)).perform(click());

        Thread.sleep(600);

        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }
}