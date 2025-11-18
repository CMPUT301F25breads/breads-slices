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
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the Invitation class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testInvitation {

    private Event testEvent;
    private Entrant recipient;
    private Entrant sender;

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



    /**
     * Sets up the test environment before each test.
     * Creates a test event, recipient, and sender.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Before
    public void setup() throws InterruptedException {


        CountDownLatch latch = new CountDownLatch(2);

        //Create test entrants
        recipient = new Entrant("Recipient", "recipient@test.com", "780-000-0001", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Failed to create recipient");
            }
        });


        sender = new Entrant("Sender", "sender@test.com", "780-000-0002", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Failed to create sender");
            }
        });

        latch.await(5000, TimeUnit.MILLISECONDS);
        assertNotNull(sender);
        assertNotNull(recipient);


        //Create test event
        CountDownLatch eventLatch = new CountDownLatch(1);
        //Set up times
        Calendar cal = Calendar.getInstance();
        //Set good event date and time - must be in future
        cal.set(2025, 11,25, 12, 30, 0);
        Date date = cal.getTime();
        Timestamp GoodEventTime = new Timestamp(date);

        //Set good registration end date and time - must be in future and before event time
        cal.set(2025, 11,24, 12, 30, 0);
        date = cal.getTime();
        Timestamp GoodRegEndTime = new Timestamp(date);
        new Event("TestEvent", "TestDescription", "TestLocation",
                GoodEventTime, GoodRegEndTime, 10, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                testInvitation.this.testEvent = event;
                eventLatch.countDown();

            }

            @Override
            public void onFailure(Exception e) {
                eventLatch.countDown();
                fail("Failed to create test event");
            }
        });

        eventLatch.await(5000, TimeUnit.MILLISECONDS);
        assertNotNull(testEvent);



        //Add recipient to event waitlist
        CountDownLatch waitlistLatch = new CountDownLatch(1);
        testEvent.addEntrantToWaitlist(recipient, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                waitlistLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                waitlistLatch.countDown();
                fail("Failed to add recipient to waitlist");
            }
        });

        waitlistLatch.await(5000, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests the Invitation constructor and default accepted/declined status.
     */
    @Test
    public void testConstructor() {
        Invitation invitation = new Invitation("InviteTitle", "InviteBody", 1, recipient.getId(), sender.getId(), testEvent.getId());
        assertEquals("InviteTitle", invitation.getTitle());
        assertEquals("InviteBody", invitation.getBody());
        assertEquals(recipient.getId(), invitation.getRecipientId());
        assertEquals(sender.getId(), invitation.getSenderId());
        assertEquals(testEvent.getId(), invitation.getEventId());
        assertFalse(invitation.isAccepted());
        assertFalse(invitation.isDeclined());
    }

    /**
     * Tests getters and setters for accepted and declined status.
     */
    @Test
    public void testAcceptedDeclinedGettersSetters() {

        Invitation invitation = new Invitation();
        invitation.setAccepted(true);
        invitation.setDeclined(true);

        assertTrue(invitation.isAccepted());
        assertTrue(invitation.isDeclined());
    }

    /**
     * Tests onAccept removes the entrant from the waitlist and adds them to the event.
     */
    @Test
    public void testOnAccept() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Invitation invitation = new Invitation("Invite", "Body", 2, recipient.getId(), sender.getId(), testEvent.getId());
        invitation.onAccept(new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                assertFalse(event.getWaitlist().getEntrants().contains(recipient));
                assertTrue(event.getEntrants().contains(recipient));
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add entrant to event");
                latch.countDown();
            }
        });


    boolean completed = latch.await(5000, TimeUnit.MILLISECONDS);
    assertTrue(completed);


    }

    /**
     * Tests onDecline removes the entrant from the waitlist without adding them to the event.
     */
    @Test
    public void testOnDecline() throws InterruptedException {
        //add recipient again to waitlist
        CountDownLatch latch = new CountDownLatch(1);
        testEvent.addEntrantToWaitlist(recipient, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Invitation invitation = new Invitation("Invite", "Body", 3, recipient.getId(), sender.getId(), testEvent.getId());

                invitation.onDecline(new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        boolean inWaitlist = event.getWaitlist().getEntrants()
                                .stream()
                                .anyMatch(e -> e.getId() == (recipient.getId()));
                        assertFalse(inWaitlist);

                        boolean inEvent = event.getEntrants()
                                .stream()
                                .anyMatch(e -> e.getId() == (recipient.getId()));
                        assertFalse(inEvent);
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
                fail("Failed to add entrant to waitlist");
                latch.countDown();

            }
        });
        boolean completed = latch.await(5000, TimeUnit.MILLISECONDS);
        assertTrue(completed);

    }



}





