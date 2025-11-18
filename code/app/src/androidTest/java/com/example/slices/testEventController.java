package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.testing.TestUtils;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testEventController {




    private void clearAll() throws InterruptedException {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(1);
        EventController.clearEvents(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);
        assertTrue(success);
    }


    @Before
    public void setup() throws InterruptedException {
        EventController db = EventController.getInstance();
        EventController.setTesting(true);

    }
    /**
     * Tests the writeEvent and getEvent method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testWriteAndGetEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        EventController.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        assertEquals(event.getName(), result.getName());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event");
            }
        });
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue(success);
    }

    /**
     * Tests the updateEvent method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testUpdateEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        event.setName("Updated Event");
        CountDownLatch latch = new CountDownLatch(1);

        EventController.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.updateEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        EventController.getEvent(event.getId(), new EventCallback() {
                            @Override
                            public void onSuccess(Event result) {
                                assertEquals("Updated Event", result.getName());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated event");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event for update");
            }
        });
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue(success);
    }

    /**
     * Tests the deleteEvent method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testDeleteEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        EventController.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.deleteEvent(String.valueOf(event.getId()));
                EventController.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        fail("Event should have been deleted");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown(); // Expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event for delete");
            }
        });
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue(success);
    }

    /**
     * Tests the getEntrantsForEvent method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testGetEntrantsForEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 3).get(0);
        List<Entrant> entrants = TestUtils.createTestEntrants(3, 10);
        event.setEntrants(entrants);
        CountDownLatch latch = new CountDownLatch(1);

        EventController.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
                    @Override
                    public void onSuccess(List<Entrant> result) {
                        assertEquals(3, result.size());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrants for event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event with entrants");
            }
        });
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue(success);
    }

    /**
     * Tests the getAllFutureEvents method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testGetAllFutureEvents() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event eventG = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);
        //Set up event that is not in the future
        Calendar cal = Calendar.getInstance();
        cal.set(2024, 11, 12, 15, 0, 0);
        Date date = cal.getTime();
        Timestamp eventDate = new Timestamp(date);
        cal.set(2024, 11, 12, 13, 0, 0);
        Date date2 = cal.getTime();
        Timestamp regDeadline = new Timestamp(date2);

        Event x = new Event("Test Event", "Test Description", "Test Location", eventDate, regDeadline, 10, true, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                assertNotNull(event);

                EventController.getAllFutureEvents(new EventListCallback() {
                    @Override
                    public void onSuccess(List<Event> result) {
                        assertFalse(result.isEmpty());
                        assertTrue(result.contains(eventG));
                        assertFalse(result.contains(event));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get all future events");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create event");
            }
        });
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue(success);
    }
}
