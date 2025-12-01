package com.example.slices.uitests.organizer;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static org.hamcrest.Matchers.anything;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.slices.R;
import com.example.slices.adapters.OrganizerEventAdapter;
import com.example.slices.fragments.OrganizerEventsFragment;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.google.firebase.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventsFragmentTest {

    /**
     * Tests that the fragment loads and displays the 3 RecyclerViews.
     */
    @Test
    public void testSectionsAreVisible() {

        FragmentScenario.launchInContainer(
                OrganizerEventsFragment.class,
                null,
                R.style.Base_Theme_Slices
        );

        onView(withId(R.id.rvUpcoming)).check(matches(isDisplayed()));
        onView(withId(R.id.rvInProgress)).check(matches(isDisplayed()));
        onView(withId(R.id.rvPast)).check(matches(isDisplayed()));
    }

    /**
     * Launch fragment with fake events injected into its lists
     * and verify RecyclerView contents render.
     */
    @Test
    public void testRecyclerViewsDisplayItems() {

        FragmentScenario<OrganizerEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrganizerEventsFragment.class,
                        null,
                        R.style.Base_Theme_Slices
                );

        scenario.onFragment(fragment -> {

            // Create 3 mock events
            List<Event> mockUpcoming = new ArrayList<>();
            List<Event> mockInProgress = new ArrayList<>();
            List<Event> mockPast = new ArrayList<>();

            Timestamp now = new Timestamp(new Date());

            // EVENT 1: Upcoming
            EventInfo info1 = new EventInfo(
                    "Upcoming Event",     // name
                    "Description",        // description
                    "123 Street",         // address
                    "",                   // guidelines
                    "",                   // imgUrl
                    now,                  // eventDate
                    now,                  // regStart
                    now,                  // regEnd
                    10,                   // maxEntrants
                    5,                    // maxWaiting
                    false,                // entrantLoc
                    "",                   // entrantDist
                    1,                    // id
                    100,                  // organizerID
                    null                  // image
            );
            Event event1 = new Event(info1);
            mockUpcoming.add(event1);

            // EVENT 2: In Progress
            EventInfo info2 = new EventInfo(
                    "In Progress Event",
                    "Description",
                    "321 Ave",
                    "",
                    "",
                    now,
                    now,
                    now,
                    10,
                    5,
                    false,
                    "",
                    2,
                    100,
                    null
            );
            Event event2 = new Event(info2);
            mockInProgress.add(event2);

            // EVENT 3: Past
            EventInfo info3 = new EventInfo(
                    "Past Event",
                    "Description",
                    "654 Road",
                    "",
                    "",
                    now,
                    now,
                    now,
                    10,
                    5,
                    false,
                    "",
                    3,
                    100,
                    null
            );
            Event event3 = new Event(info3);
            mockPast.add(event3);

            fragment.getView().post(() -> {

                RecyclerView rvUpcoming = fragment.getView().findViewById(R.id.rvUpcoming);
                RecyclerView rvInProgress = fragment.getView().findViewById(R.id.rvInProgress);
                RecyclerView rvPast = fragment.getView().findViewById(R.id.rvPast);

                rvUpcoming.setAdapter(
                        new OrganizerEventAdapter(fragment.requireContext(), mockUpcoming, fragment)
                );
                rvInProgress.setAdapter(
                        new OrganizerEventAdapter(fragment.requireContext(), mockInProgress, fragment)
                );
                rvPast.setAdapter(
                        new OrganizerEventAdapter(fragment.requireContext(), mockPast, fragment)
                );
            });
        });

        onView(withText("Upcoming Event")).check(matches(isDisplayed()));
        onView(withText("In Progress Event")).check(matches(isDisplayed()));
        onView(withText("Past Event")).check(matches(isDisplayed()));
    }

    /**
     * Tests clicking an event card triggers navigation
     * to OrganizerEditEventFragment with correct bundle.
     */
    @Test
    public void testNavigationOnEventClick() {

        NavController mockNavController = Mockito.mock(NavController.class);

        FragmentScenario<OrganizerEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrganizerEventsFragment.class,
                        null,
                        R.style.Base_Theme_Slices
                );

        scenario.onFragment(fragment -> {

            Navigation.setViewNavController(
                    fragment.requireView(),
                    mockNavController
            );

            // Create mock event for adapter
            List<Event> mockList = new ArrayList<>();

            // Create Firebase timestamp
            Timestamp now = Timestamp.now();

            EventInfo info = new EventInfo(
                    "Test Click Event",        // name
                    "Description here",        // description
                    "Test Street",             // address
                    "Test guidelines",         // guidelines
                    "",                        // imgUrl
                    now,                       // eventDate
                    now,                       // regStart
                    now,                       // regEnd
                    10,                        // maxEntrants
                    2,                         // maxWaiting
                    false,                     // entrantLoc
                    "5km",                     // entrantDist
                    123,                       // id
                    1,                         // organizerID
                    null                       // image (you can use null for tests)
            );

            Event event = new Event(info);
            mockList.add(event);

            fragment.getView().post(() -> {
                fragment.getBinding().rvUpcoming.setAdapter(
                        new OrganizerEventAdapter(
                                fragment.requireContext(),
                                mockList,
                                fragment
                        )
                );
            });
        });

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.rvUpcoming))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        androidx.test.espresso.action.ViewActions.click()
                ));

        Mockito.verify(mockNavController).navigate(
                Mockito.eq(R.id.action_OrganizerEventsFragment_to_OrganizerEditEventFragment),
                Mockito.argThat(bundle -> bundle.getString("eventID").equals("123")),
                Mockito.any()
        );
    }
}
