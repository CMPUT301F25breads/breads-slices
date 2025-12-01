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
 * UI test for AdminImagesFragment
 * Tests:
 *  - fragment loads
 *  - recycler displays events with images
 *  - clicking an image opens EventDetailsFragment
 *  - back button returns to AdminHome
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        // Enter admin mode
        scenario.getScenario().onActivity(MainActivity::switchToAdmin);

        Thread.sleep(1200);

        onView(allOf(withId(R.id.adminHomeFragment),
                isDescendantOfA(withId(R.id.bottom_nav_admin))))
                .perform(click());

        onView(withId(R.id.btn_browse_images)).perform(click());

        onView(withId(R.id.adminimg_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Fragment loads and shows the image recycler.
     */
    //Testing: US 03.06.01
    @Test
    public void testImagesFragmentLoads() {
        onView(withId(R.id.adminimg_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Clicking an image item opens EventDetailsFragment.
     */
    @Test
    public void testOpenEventDetailsFromImage() throws InterruptedException {
        Thread.sleep(800);

        onView(withId(R.id.adminimg_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
    }

    /**
     * Toolbar back arrow returns to AdminHome.
     */
    @Test
    public void testToolbarBackButton() throws InterruptedException {
        onView(withId(R.id.admin_img_toolbar)).perform(click());

        Thread.sleep(600);

        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }
}