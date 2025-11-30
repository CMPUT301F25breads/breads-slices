package com.example.slices.uitests.user;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyEventsTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(isRoot()).perform(waitFor(1500));

        onView(withId(R.id.confirmed_list)).check(matches(isDisplayed()));
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return isRoot();
            }
            @Override public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    /** Tests opening details from the MyEventsFragment and confirmed list
     *
     * Covers stories US 01.05.04
     */
    @Test
    public void openDetailsTestFromConfirm() {

        onView(withId(R.id.confirmed_list))
                .perform(actionOnItemAtPosition(0, click()));
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.event_counts))
                .check(matches(isDisplayed()));

    }

    /** Tests opening details from the MyEventsFragment and waitlist list
     *
     * Covers stories US 01.05.04
     */
    @Test
    public void openDetailsTestFromWaitlist() {

        onView(withId(R.id.waitlist_list))
                .perform(actionOnItemAtPosition(0, click()));
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.event_counts))
                .check(matches(isDisplayed()));

    }

    /** Tests opening details from the MyEventsFragment and past list
     *
     * Covers stories US 01.05.04
     */
    @Test
    public void openDetailsTestFromPast() {

        onView(withId(R.id.past_list))
                .perform(actionOnItemAtPosition(0, click()));
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.event_counts))
                .check(matches(isDisplayed()));

    }

    /** Tests opening details from the MyEventsFragment and waitlist list
     *
     * Covers stories US 01.05.05
     */
    @Test
    public void openGuidelinesFromWaitlist() {

        onView(withId(R.id.waitlist_list))
                .perform(actionOnItemAtPosition(0, click()));
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.btn_guidelines)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        onView(withText("OK"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

}
