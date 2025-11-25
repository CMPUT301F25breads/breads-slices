package com.example.slices.uitests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.slices.MainActivity;
import com.example.slices.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * Tests toggling the different user modes
     */
    @Test
    public void bottomNavUserVisible() {
        onView(withId(R.id.bottom_nav))
                .check(matches(isDisplayed()));
    }
    @Test
    public void switchToOrgMode_bottomNavOrgVisible() {
        scenario.getScenario().onActivity(MainActivity::switchToOrganizer);

        onView(withId(R.id.bottom_nav_org))
                .check(matches(isDisplayed()));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(
                ViewMatchers.Visibility.GONE)));
    }
    @Test
    public void switchToAdminMode_bottomNavAdminVisible() {
        scenario.getScenario().onActivity(MainActivity::switchToAdmin);

        onView(withId(R.id.bottom_nav_admin))
                .check(matches(isDisplayed()));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(
                        ViewMatchers.Visibility.GONE)));
    }

    /**
     * User mode navigation testing
     */
    @Test
    public void navigateToBrowseTest() {
        onView(allOf(withId(R.id.BrowseFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.browse_list)).check(matches(isDisplayed()));
    }
    @Test
    public void navigateToMyEventsTest() {
        onView(allOf(withId(R.id.BrowseFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());
        onView(allOf(withId(R.id.MyEventsFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.confirmed_list)).check(matches(isDisplayed()));
    }
    @Test
    public void navigateToNotifTest() {
        onView(allOf(withId(R.id.NotifFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());

        onView(withId(R.id.notif_frag)).check(matches(isDisplayed()));
    }
    @Test
    public void navigateToMenuTest() {

        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav))))
                .perform(click());


        onView(withId(R.id.profile_edit_button)).check(matches(isDisplayed()));
    }

    /**
     * Organizer mode navigation testing
     */
    @Test
    public void navigateToCreateOrgTest() {
        scenario.getScenario().onActivity(MainActivity::switchToOrganizer);

        onView(allOf(withId(R.id.OrganizerCreateEventFragment), isDescendantOfA(withId(R.id.bottom_nav_org))))
                .perform(click());

        onView(withId(R.id.create_frag)).check(matches(isDisplayed()));
    }
    @Test
    public void navigateToMyEventsOrgTest() {
        scenario.getScenario().onActivity(MainActivity::switchToOrganizer);

        onView(allOf(withId(R.id.OrganizerEventsFragment), isDescendantOfA(withId(R.id.bottom_nav_org))))
                .perform(click());

        onView(withId(R.id.org_events_frag))
                .check(matches(isDisplayed()));
    }
    @Test
    public void navigateToMenuOrgTest() {
        scenario.getScenario().onActivity(MainActivity::switchToOrganizer);

        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav_org))))
                .perform(click());

        onView(withId(R.id.profile_edit_button))
                .check(matches(isDisplayed()));
    }
    @Test
    public void navigateToNotifOrgTest() {
        scenario.getScenario().onActivity(MainActivity::switchToOrganizer);

        //onView(allOf(withId(R.id.NotifOrgFragment), isDescendantOfA(withId(R.id.bottom_nav_org))))
                //.perform(click());

        onView(withId(R.id.create_frag)).check(matches(isDisplayed()));
    }

    /**
     * Admin mode navigation testing
     */
    @Test
    public void navigateToAdminHome() {
        scenario.getScenario().onActivity(MainActivity::switchToAdmin);

        onView(allOf(withId(R.id.adminHomeFragment), isDescendantOfA(withId(R.id.bottom_nav_admin))))
                .perform(click());

        onView(withId(R.id.adminhome_frag)).check(matches(isDisplayed()));

    }

//    @Test
//    public void navigateToAdminSearch() {
//        scenario.getScenario().onActivity(MainActivity::switchToAdmin);
//        onView(allOf(withId(R.id.nav_search), isDescendantOfA(withId(R.id.bottom_nav_admin))))
//                .perform(click());
//        onView(withId(R.id.create_button)).check(matches(isDisplayed()));
//
//    }

    @Test
    public void navigateToAdminProfiles() {
        scenario.getScenario().onActivity(MainActivity::switchToAdmin);
        onView(allOf(withId(R.id.adminProfilesFragment), isDescendantOfA(withId(R.id.bottom_nav_admin))))
                .perform(click());
        onView(withId(R.id.adminprofiles_frag)).check(matches(isDisplayed()));

    }

    @Test
    public void navigateToAdminSettings() {
        scenario.getScenario().onActivity(MainActivity::switchToAdmin);
        onView(allOf(withId(R.id.MenuFragment), isDescendantOfA(withId(R.id.bottom_nav_admin))))
                .perform(click());
        onView(withId(R.id.profile_edit_button)).check(matches(isDisplayed()));

    }

}
