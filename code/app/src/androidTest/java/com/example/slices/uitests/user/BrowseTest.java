package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import android.view.View;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrowseTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * setup()
     * sets up each test by waiting for the user to be initialized with DB calls
     * and then navigates to the browse screen
     */
    @Before
    public void setup() {
        onView(isRoot()).perform(waitFor(2000));

        onView(allOf(withId(R.id.BrowseFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.browse_list)).check(matches(isDisplayed()));
    }

    /**
     * waitFor
     * simple helper function that makes Espresso wait for a set amount of milliseconds.
     * Used in letting firestore DB finish setting up before the test runs
     * @param millis
     *   millis for how long to wait in milliseconds
     * @return
     *   returns a timer message that can be logged
     */
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

    /**
     * clickChildViewWithId
     * helper that clicks a child view inside the RecyclerView
     * @param id
     *    resource id of the child view that needs to be clicked
     * @return
     *  returns a ViewAction that find the child and calls performClick() on it
     */
    private static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with id: " + id;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }

    /**
     * testJoinLeaveWaitlistButtonToggles
     *   Tests that the join/leave button in the Browse list toggles correctly
     *   First click should siwtch the label to "leave" and a second click should switch it back
     */
    // Testing: US 01.01.01, 01.01.02, 01.01.03
    // - Raj
    @Test
    public void testJoinLeaveWaitlistButtonToggles() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.browse_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btn_event_action)
                ));

        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.browse_list))
                .check(matches(hasDescendant(withText("Leave"))));


        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.browse_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btn_event_action)
                ));


        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.browse_list))
                .check(matches(hasDescendant(withText("Join"))));
    }

    /**
     * testFilterEvents
     *  tests the search bar on the Browse screen. Typing a keyword ("swim") and pressing search
     *  should filter the list so that the event with "swim" in its name appears in the results
     */
    // Testing US 01.01.04
    // -Raj
    @Test
    public void testFilterEvents() {
        //give fragment a moment to load events
        onView(isRoot()).perform(waitFor(2000));

        // type the search keyword into the search bar
        onView(withId(R.id.search_edit_text))
                .perform(click(), androidx.test.espresso.action.ViewActions
                                .replaceText("swim"),
                        androidx.test.espresso.action.ViewActions.closeSoftKeyboard());

        // click the search button
        onView(withId(R.id.search_button)).perform(click());

        // wait for adapter
        onView(isRoot()).perform(waitFor(1500));

        // check that the results contain the tested swimmer event
        onView(withId(R.id.browse_list)).check(matches
                (hasDescendant(withText(containsString("swimmers")))));
    }
}
