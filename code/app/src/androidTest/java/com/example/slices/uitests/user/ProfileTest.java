package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(isRoot()).perform(waitFor(2000));

        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.testingMenu)).check(matches(isDisplayed()));
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
}
