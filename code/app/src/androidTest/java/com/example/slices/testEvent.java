package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.EventCallback;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for the Event class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testEvent {
    /**
     * A valid event time
     */
    private Timestamp GoodEventTime;
    /**
     * A valid registration end time
     */
    private Timestamp GoodRegEndTime;
    /**
     * An invalid event time
     */
    private Timestamp BadEventTime;
    /**
     * An invalid registration end time
     */
    private Timestamp BadRegEndTime;
    /**
     * An invalid registration end time
     */
    private Timestamp PastRegEndTime;
    /**
     * The DBConnector instance to use for testing
     */
    private DBConnector db = new DBConnector();


    /**
     * Sets up the test by creating a new DBConnector instance and setting up valid and invalid event times
     */
    @Before
    public void setup() {

        Calendar cal = Calendar.getInstance();
        //Set good event date and time - must be in future
        cal.set(2025, 11,25, 12, 30, 0);
        Date date = cal.getTime();
        GoodEventTime = new Timestamp(date);

        //Set good registration end date and time - must be in future and before event time
        cal.set(2025, 11,24, 12, 30, 0);
        date = cal.getTime();
        GoodRegEndTime = new Timestamp(date);

        //Set bad event date and time - in past
        cal.set(2020, 11,25, 12, 30, 0);
        date = cal.getTime();
        BadEventTime = new Timestamp(date);

        //Set bad registration end date and time - after event time
        cal.set(2025, 11,26, 12, 30, 0);
        date = cal.getTime();
        BadRegEndTime = new Timestamp(date);

        //Set bad registration end date and time - in past
        cal.set(2020, 11,25, 12, 30, 0);
        date = cal.getTime();
        PastRegEndTime = new Timestamp(date);

    }


    /**
     * Tests the creation of a valid event
     */
    @Test
    public void testNormalEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();

        db.clearEvents(() -> {
            new Event("Foo", "Foo", "Foo", GoodEventTime, GoodRegEndTime, 10, new EventCallback() {
                @Override
                public void onSuccess(Event event) {
                    ref.set(event);
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    latch.countDown();
                }
            });
        });

        boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
        assertTrue("Event creation callback was not called in time", completed);
        Event e = ref.get();
        assertNotNull("Event should not be null", e);
        assertEquals("Foo", e.getName());
    }

    /**
     * Tests the creation of an event with an invalid event time
     */
    @Test
    public void testBadEventTime() {
        CountDownLatch latch = new CountDownLatch(1);
        db.clearEvents(() -> {
            try {
                Event e = new Event("Foo", "Foo", "Foo", BadEventTime, GoodRegEndTime, 10, new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                    }
                });
                Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
                assertEquals(completed, false);
            }
            catch (Exception e) {
                assertEquals(e.getMessage(), "Event time is in the past");
            }
        });
    }

    /**
     * Tests the creation of an event with an invalid registration end time
     */
    @Test
    public void testBadRegEndTime() {
        CountDownLatch latch = new CountDownLatch(1);
        db.clearEvents(() -> {
            try {
                Event e = new Event("Foo", "Foo", "Foo", GoodEventTime, BadRegEndTime, 10, new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                    }
                });
                Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
                assertEquals(completed, false);
            } catch (Exception e) {
                assertEquals(e.getMessage(), "Registration deadline is after event time");
            }
        });
    }

    /**
     * Tests the creation of an event with a registration end time in the past
     */
    @Test
    public void testPastRegEndTime() {
        CountDownLatch latch = new CountDownLatch(1);
        db.clearEvents(() -> {
            try {
                Event e = new Event("Foo", "Foo", "Foo", GoodEventTime, PastRegEndTime, 10, new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                    }
                });
                Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
                assertEquals(completed, false);
            } catch (Exception e) {
                assertEquals(e.getMessage(), "Registration deadline is in the past");
            }
        });
    }

    /**
     * Tests the adding and removing of an entrant from the waitlist
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testAddRemoveEntrantFromWaitlist() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Entrant e = new Entrant("Foo", "Bar", "1234567890", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                //Make a new event
                Event e = new Event("Foo", "Foo", "Foo", GoodEventTime, GoodRegEndTime, 10, new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        //Add the entrant to the waiting list
                        event.addEntrantToWaitlist(entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                //Verify that the entrant is in the waiting list
                                assertEquals(event.getWaitlist().getEntrants().contains(entrant), true);
                                //Check the database
                                db.getEvent(event.getId(), new EventCallback() {
                                    @Override
                                    public void onSuccess(Event event) {
                                        event.removeEntrantFromWaitlist(entrant, new DBWriteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                //Verify that the entrant is no longer in the waiting list
                                                assertEquals(event.getWaitlist().getEntrants().contains(entrant), false);
                                                latch.countDown();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                fail("Failed to remove entrant from waitlist");
                                                latch.countDown();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get event");
                                        latch.countDown();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to add entrant to waitlist");
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create event");

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create entrant");
            }
        });
        Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
        assertEquals(completed, true);

    }
    @AfterClass
    public static void tearDown() throws InterruptedException {
        DBConnector db = new DBConnector();
        CountDownLatch latch = new CountDownLatch(4);
        db.clearEntrants(() -> latch.countDown());
        db.clearEvents(() -> latch.countDown());
        db.clearNotifications(() -> latch.countDown());
        db.clearLogs(() -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }

}
