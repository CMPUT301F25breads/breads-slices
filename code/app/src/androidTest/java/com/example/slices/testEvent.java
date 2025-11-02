package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testEvent {
    private Timestamp GoodEventTime;
    private Timestamp GoodRegEndTime;
    private Timestamp BadEventTime;
    private Timestamp BadRegEndTime;

    private Timestamp PastRegEndTime;
    private DBConnector db = new DBConnector();







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



    @Test
    public void testNormalEvent() {

        CountDownLatch latch = new CountDownLatch(1);

        db.clearEvents(() -> {
            try {
                Event e = new Event("Foo", "Foo", "Foo", GoodEventTime, GoodRegEndTime, 10, new EventCallback() {
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
                assertEquals(completed, true);
                assertEquals(e.getName(), "Foo");
            }
            catch (Exception e) {
                fail("Failed to create event");
            }
        });
    }

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










}
