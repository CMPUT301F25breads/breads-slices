package com.example.slices.uitests.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminHomeTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {

        scenario.getScenario().onActivity(MainActivity::switchToAdmin);

        Thread.sleep(1200);

        onView(withId(R.id.adminHomeFragment)).perform(click());

        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToEvents() {
        onView(withId(R.id.btn_browse_events)).perform(click());
        onView(withId(R.id.admin_events_frag)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToProfiles() {
        onView(withId(R.id.btn_browse_profiles)).perform(click());
        onView(withId(R.id.admin_profiles_frag)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToImages() {
        onView(withId(R.id.btn_browse_images)).perform(click());
        onView(withId(R.id.admin_images_frag)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToOrganizers() {
        onView(withId(R.id.btn_browse_organizers)).perform(click());
        onView(withId(R.id.admin_organizers_frag)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToLogs() {
        onView(withId(R.id.btn_logs)).perform(click());
        onView(withId(R.id.admin_notifications_frag)).check(matches(isDisplayed()));
    }
}
