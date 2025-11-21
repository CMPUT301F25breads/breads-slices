package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Notification;
import com.example.slices.testing.TestUtils;
import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class testEventController {

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        NotificationManager.setTesting(true);
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);
    }


    @AfterClass
    public static void tearDown() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);
        //Revert out of testing mode
        EntrantController.setTesting(false);
        EventController.setTesting(false);
        Logger.setTesting(false);
        NotificationManager.setTesting(false);
    }




    private void clearAll() throws InterruptedException {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(3);
        EventController.clearEvents(latch::countDown);
        EntrantController.clearEntrants(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);

        assertTrue(success);
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
                        assertEquals(event.getEventInfo().getName(), result.getEventInfo().getName());
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
        CountDownLatch latch = new CountDownLatch(1);
        event.getEventInfo().updateName("Updated Event" , new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.updateEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        EventController.getEvent(event.getId(), new EventCallback() {
                            @Override
                            public void onSuccess(Event result) {
                                assertEquals("Updated Event", result.getEventInfo().getName());
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
                EventController.deleteEvent(String.valueOf(event.getId()), new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
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

                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to delete event");
                    }

                });

            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event for delete");
            }
        });
        boolean success = latch.await(20, TimeUnit.SECONDS);
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
        wait(1000);
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

        Event x = new Event("Test Event", "Test Description", "Test Location", eventDate, regDeadline, 10, new EventCallback() {
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


    @Test
    public void testDeleteEventWithEntrants() throws InterruptedException {
        // Clear all collections before the test
        clearAll();

        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);

        // Create entrants
        List<Entrant> entrants = TestUtils.createTestEntrants(3, 10);
        Entrant e = entrants.get(0); // regular entrant
        Entrant w = entrants.get(1); // waitlist entrant

        // Add entrants and waitlist to Firestore
        CountDownLatch addLatch = new CountDownLatch(2);

        event.addEntrant(e, new DBWriteCallback() {
            @Override
            public void onSuccess() { addLatch.countDown(); }
            @Override
            public void onFailure(Exception ex) { fail("Failed to add entrant e"); }
        });

        event.addEntrantToWaitlist(w, new DBWriteCallback() {
            @Override
            public void onSuccess() { addLatch.countDown(); }
            @Override
            public void onFailure(Exception ex) { fail("Failed to add waitlist entrant w"); }
        });

        // Wait for all additions to complete
        boolean added = addLatch.await(10, TimeUnit.SECONDS);
        assertTrue("Entrants failed to be added in time", added);

        // Now delete the event
        CountDownLatch deleteLatch = new CountDownLatch(1);
        EventController.deleteEvent(String.valueOf(event.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                deleteLatch.countDown();
            }

            @Override
            public void onFailure(Exception ex) {
                fail("Failed to delete event: " + ex.getMessage());
            }
        });

        // Wait for event deletion + notifications
        boolean deleted = deleteLatch.await(20, TimeUnit.SECONDS);
        assertTrue("Event deletion test timed out", deleted);

        // Verify notifications
        assertTrue("Regular entrant did not receive notification",
                waitForNotification(e.getId(), true, 5000));

        assertTrue("Waitlist entrant should not receive notification",
                waitForNotification(w.getId(), false, 5000));
    }

    // Utility to wait for a notification for a recipient
    private boolean waitForNotification(int recipientId, boolean expectNotification, long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] result = {false};

            NotificationManager.getNotificationByRecipientId(recipientId, new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    result[0] = expectNotification ? !notifications.isEmpty() : notifications.isEmpty();
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception ex) {
                    latch.countDown();
                }
            });

            latch.await(500, TimeUnit.MILLISECONDS);

            if (result[0]) return true;
            Thread.sleep(200);
        }
        return false;
    }







}
