package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.models.Entrant;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the Entrant class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testsEntrant {
    private Entrant primaryEntrant;


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
     * Setup executed before each test.
     * Initializes the DBConnector and a primary test entrant.
     */

    @Before
    public void setup() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        primaryEntrant = new Entrant("Primary", "primary@test.com", "780-000-0000", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Failed to create primary entrant");
            }
        });

        latch.await(5000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testEntrant() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);
        assertTrue(e.getName().equals("Foo"));
        assertTrue(e.getEmail().equals("Foo@Foo.Foo"));
        assertTrue(e.getPhoneNumber().equals("780-678-1211"));
        assertTrue(e.getId() == 1);
    }

    @Test
    public void testEntrantWithParent() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);
        Entrant p;
        try {
            p = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 2, e);
            assertTrue(e.getSubEntrants().contains(2));
            assertTrue(p.getParent() == 1);
        }
        catch (Exception r) {
            fail("Failed to create parent");
        }
    }

    @Test
    public void testEntrantWithParentFail() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);

        try {
            Entrant p = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 2, e);
            try {
                Entrant p2 = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 3, p);
                fail("Should not be able to create parent with parent");
            }
            catch (Exception r) {
                assertTrue(true);
            }
        }
        catch (Exception r) {
            fail("Failed to create parent");
        }
    }
    /**
     * Tests setters and getters for name, email, phone number, and device ID.
     */
    @Test
    public void testSettersAndGetters() {
        primaryEntrant.setName("NewName");
        primaryEntrant.setEmail("newemail@test.com");
        primaryEntrant.setPhoneNumber("111-222-3333");
        primaryEntrant.setDeviceId("DEVICE123");

        assertEquals("NewName", primaryEntrant.getName());
        assertEquals("newemail@test.com", primaryEntrant.getEmail());
        assertEquals("111-222-3333", primaryEntrant.getPhoneNumber());
        assertEquals("DEVICE123", primaryEntrant.getDeviceId());
    }

    /**
     * Tests adding and retrieving organized events.
     */
    @Test
    public void testOrganizedEvents() {
        primaryEntrant.addOrganizedEvent(1);
        primaryEntrant.addOrganizedEvent(2);

        List<Integer> events = new ArrayList<>();
        events.add(1);
        events.add(2);
        assertEquals(events, primaryEntrant.getOrganizedEvents());

        List<Integer> newEvents = new ArrayList<>();
        newEvents.add(3);
        primaryEntrant.setOrganizedEvents(newEvents);
        assertEquals(newEvents, primaryEntrant.getOrganizedEvents());
    }

    /**
     * Tests adding sub-entrants manually and retrieving them.
     */
    @Test
    public void testSubEntrants() {
        Entrant sub = new Entrant("Child", "child@test.com", "000-111-2222", 500, primaryEntrant);
        primaryEntrant.addSubEntrant(sub);

        assertTrue(primaryEntrant.getSubEntrants().contains(sub.getId()));
    }

    /**
     * Tests notification preference getter/setter.
     */
    @Test
    public void testSendNotifications() {
        primaryEntrant.setSendNotifications(true);
        assertTrue(primaryEntrant.getSendNotifications());

        primaryEntrant.setSendNotifications(false);
        assertFalse(primaryEntrant.getSendNotifications());
    }

    /**
     * Tests equality and hash code based on ID.
     */
    @Test
    public void testEqualityAndHashCode() {
        Entrant e1 = new Entrant("A", "a@test.com", "111", 123);
        Entrant e2 = new Entrant("B", "b@test.com", "222", 123); // same ID
        Entrant e3 = new Entrant("C", "c@test.com", "333", 124);

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1.hashCode(), e3.hashCode());
    }

    /**
     * Tests parent retrieval via callback.
     */
    @Test
    public void testGetParentCallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Entrant child = new Entrant("Child2", "child2@test.com", "555-666-7777", primaryEntrant, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                entrant.getParent(new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant parent) {
                        assertEquals(primaryEntrant.getId(), parent.getId());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get parent");
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create child entrant");
                latch.countDown();
            }
        });

        latch.await(5000, TimeUnit.MILLISECONDS);
    }


}

