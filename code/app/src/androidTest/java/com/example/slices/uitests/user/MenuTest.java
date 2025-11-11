package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

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
public class MenuTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.profile_edit_button)).check(matches(isDisplayed()));
    }

    @Test
    public void editButtonTest(){
        onView(withId(R.id.profile_edit_button))
                .perform(click())
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.name_textfield))
                .check(matches(isEnabled()));
        onView(withId(R.id.email_textfield))
                .check(matches(isEnabled()));
        onView(withId(R.id.phone_number_textfield))
                .check(matches(isEnabled()));
        onView(withId(R.id.send_notifications_switch))
                .check(matches(isEnabled()));
        onView(withId(R.id.profile_cancel_button))
                .check(matches(isDisplayed()));
        onView(withId(R.id.profile_save_button))
                .check(matches(isDisplayed()));
    }

    @Test
    public void cancelButtonTest(){
        onView(withId(R.id.profile_edit_button))
                .perform(click());
        onView(withId(R.id.profile_cancel_button))
                .perform(click())
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.profile_edit_button))
                .check(matches(isDisplayed()));
        onView(withId(R.id.profile_save_button))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.name_textfield))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.email_textfield))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.phone_number_textfield))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.send_notifications_switch))
                .check(matches(not(isEnabled())));
    }
}
