package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

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

/**
 * UI tests for the Profile menu tab
 * These tests check that a user can edit/save profile details and whether
 * they can opt in/out of notifications from the profile page
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * setup()
     * sets up each test by waiting for the user to be initialized with DB calls
     * and then navigates to the profile screen (menu screen)
     */
    @Before
    public void setup() {
        onView(isRoot()).perform(waitFor(2000));

        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.testingMenu)).check(matches(isDisplayed()));
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
     * testEditAndSaveProfileInfo
     * Name, email, phone number from the profile screen is edited and saved.
     * Checks after saving to see if the new values are correct and the fields are not editable
     */
    // Testing US 01.02.01, 01.02.02
    @Test
    public void testEditAndSaveProfileInfo() {
        // click edit profile then wait
        onView(withId(R.id.profile_edit_button)).check(matches(isDisplayed())).perform(click());
        onView(isRoot()).perform(waitFor(300));

        // check to see if all fields are available to edit
        onView(withId(R.id.name_textfield)).check(matches(isEnabled()));
        onView(withId(R.id.email_textfield)).check(matches(isEnabled()));
        onView(withId(R.id.phone_number_textfield)).check(matches(isEnabled()));

        // actually input values into fields
        onView(withId(R.id.name_textfield)).perform(replaceText("Tester"),
                closeSoftKeyboard());
        onView(withId(R.id.email_textfield)).perform(replaceText("test@testing.com"),
                closeSoftKeyboard());
        onView(withId(R.id.phone_number_textfield)).perform(replaceText("3434"),
                closeSoftKeyboard());

        // hit save
        onView(withId(R.id.profile_save_button)).check(matches(isDisplayed())).perform(click());
        onView(isRoot()).perform(waitFor(300));

        // verify all is well
        onView(withId(R.id.name_textfield))
                .check(matches(withText("Tester")))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.email_textfield))
                .check(matches(withText("test@testing.com")))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.phone_number_textfield))
                .check(matches(withText("3434")))
                .check(matches(not(isEnabled())));
    }

    /**
     * Helper that reads the checked state of a  button/switch in the UI and returns it as a boolean
     * @param viewId
     *    viewId resource ID of the switch or other button/switch
     * @return
     *    true if the view is checked, false otherwise
     */
    private static boolean getSwitchCheckedState(int viewId) {
        final boolean[] checked = new boolean[1];

        onView(withId(viewId)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(android.widget.CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "Get checked state from CompoundButton";
            }

            @Override
            public void perform(UiController uiController, View view) {
                checked[0] = ((android.widget.CompoundButton) view).isChecked();
            }
        });

        return checked[0];
    }

    /**
     * testNotificationOpt()
     *   tests notification switch on the profile screen can be toggled on or off and that the new
     *   state is saved. Test does not assume initial state. Record the initial state, toggles it,
     *   then check that the state changed after tapping the switch and then after saving the switch
     *   keeps the new state and is not editable
     */
    @Test
    public void testNotificationOpt() {
        onView(withId(R.id.profile_edit_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(isRoot()).perform(waitFor(300));

        // anchor the initial state of the notification switch to a bool
        boolean initialState = getSwitchCheckedState(R.id.send_notifications_switch);

        // toggle notifications switch
        onView(withId(R.id.send_notifications_switch)).perform(click());
        onView(isRoot()).perform(waitFor(200));

        if (initialState) {
            onView(withId(R.id.send_notifications_switch)).check(matches(isNotChecked()));
        } else {
            onView(withId(R.id.send_notifications_switch)).check(matches(isChecked()));
        }

        // hit save
        onView(withId(R.id.profile_save_button)).check(matches(isDisplayed())).perform(click());
        onView(isRoot()).perform(waitFor(300));

        // do an if-else to check the state of the switch
        if (initialState) {
            onView(withId(R.id.send_notifications_switch)).check(matches(isNotChecked()))
                    .check(matches(not(isEnabled())));
        } else {
            onView(withId(R.id.send_notifications_switch)).check(matches(isChecked()))
                    .check(matches(not(isEnabled())));
        }
    }
}
