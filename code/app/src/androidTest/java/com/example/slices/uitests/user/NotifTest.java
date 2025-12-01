package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotifTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(allOf(withId(R.id.NotifFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.notif_frag)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the Recycler View is visible and contains items.
     * Assumes the user has at least one notification/invitation.
     */
    @Test
    public void testNotificationListIsVisible() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.notification_recycler))
                .check(matches(isDisplayed()));
        onView(withId(R.id.notification_recycler))
                .check(matches(hasDescendant(withId(R.id.notification_card_title))));
    }

    /**
     * Tests the "Accept" / "Dismiss" action on the first item in the list.
     */
    @Test
    public void testAcceptOrDismissAction() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.notification_recycler))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.notification_card_accept_button)));
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.notification_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Tests the "Decline" action on the first item in the list.
     * Only valid if the first item is an Invitation or NotSelected type.
     */
    @Test
    public void testDeclineAction() {
        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.notification_recycler))
                .perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.notification_card_decline_button)));

        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.notification_recycler)).check(matches(isDisplayed()));
    }

    /**
     * Tests the "Clear Notifications" button functionality.
     */
    @Test
    public void testClearNotificationsButton() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.clear_notifications_button))
                .perform(click());
        onView(isRoot()).perform(waitFor(1500));
        onView(withId(R.id.no_notif_text))
                .check(matches(isDisplayed()));
    }

    /**
     * Helper method from MyEventsTest to handle Async wait times
     */
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

    /**
     * Custom ViewAction to click a button within a RecyclerView item.
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null; // No constraints, allows finding child inside RecyclerView item
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

}
