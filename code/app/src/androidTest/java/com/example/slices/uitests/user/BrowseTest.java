package com.example.slices.uitests.user;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrowseTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() {
        onView(allOf(withId(R.id.BrowseFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.browse_event_list)).check(matches(isDisplayed()));
    }

}
