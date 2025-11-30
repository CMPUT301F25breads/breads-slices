package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
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

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDetailsTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(isRoot()).perform(waitFor(2000));

        onView(allOf(withId(R.id.BrowseFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.browse_list)).check(matches(isDisplayed()));
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    private static String getTextFromView(final int viewId) {
        final AtomicReference<String> textRef = new AtomicReference<>();

        onView(withId(viewId)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "Get text from view with id: " + viewId;
            }

            @Override
            public void perform(UiController uiController, View view) {
                textRef.set(((TextView) view).getText().toString());
            }
        });

        return textRef.get();
    }

    @Test
    public void testJoinLeaveWaitlistEventDetailsToggle() {
        // give browsefrag a sec to finish loading events
        onView(isRoot()).perform(waitFor(1000));

        // open the EventDetailsFragment by clicking the first event in the browsefrag
        onView(withId(R.id.browse_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0, click()));

        // wait for navigation + verify we're on eventdetails proper
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));

        // get the initial button text (could be "Join" or "Leave")
        String initialText = getTextFromView(R.id.btn_join_waitlist);

        // click once, expect the text to change to either join or leave
        onView(withId(R.id.btn_join_waitlist))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(isRoot()).perform(waitFor(800));
        // check to ensure that the text has actually changed
        onView(withId(R.id.btn_join_waitlist))
                .check(matches(not(withText(initialText))));

        // click again, expect text to go back to the original
        onView(withId(R.id.btn_join_waitlist))
                .perform(click());
        onView(isRoot()).perform(waitFor(800));
        onView(withId(R.id.btn_join_waitlist))
                .check(matches(withText(initialText)));
    }
}

