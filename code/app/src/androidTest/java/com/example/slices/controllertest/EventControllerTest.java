package com.example.slices.controllertest;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.example.slices.models.Notification;
import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EventControllerTest {

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        // Chuck it in testing mode
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        NotificationManager.setTesting(true);


        //Clean it out
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", completed);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", completed);
        EntrantController.setTesting(false);
        EventController.setTesting(false);
        Logger.setTesting(false);
        NotificationManager.setTesting(false);
    }


    /**
     * Await a latch to complete
     * @param latch
     *      Latch to wait for
     */
    private void await(CountDownLatch latch) {
        try {
            boolean ok = latch.await(20, TimeUnit.SECONDS);
            assertTrue("Timed out waiting for async operation", ok);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }

    /**
     * Clear all collections
     */
    private void clearAll()  {
        CountDownLatch latch = new CountDownLatch(3);
        EventController.clearEvents(latch::countDown);
        EntrantController.clearEntrants(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        await(latch);
    }

    /**
     * Create an entrant
     * @param name
     *      Entrant name
     * @return
     *      Entrant

     */
    private Entrant createEntrant(String name)  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Entrant> ref = new AtomicReference<>();
        //Create a new entrant
        EntrantController.createEntrant(name, name + "@mail.com", "123", new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        ref.set(entrant);
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create entrant: " + e.getMessage());
                    }
                });

        await(latch);
        return ref.get();
    }

    /**
     * Create a valid event - requires valid times
     * @return
     *      Event
     */
    private Event createValidEvent() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();
        //Get some valid times
        List<Timestamp> times = EventController.getTestEventTimes();
        Timestamp regStart = times.get(0);
        Timestamp regEnd = times.get(1);
        Timestamp eventDate = times.get(2);
        //Create the event
        EventController.createEvent("Name", "Desc", "Loc", "Guidelines", "ImgUrl",
                eventDate, regStart, regEnd, 10, 5, false, "none", 123,
                new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        ref.set(event);
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create event: " + e.getMessage());
                    }
                });
        await(latch);
        return ref.get();
    }

    /**
     * Create an event without checking times
     * @param info
     *      Event info to use for creating the event
     */
    private void createEventNoCheck(EventInfo info) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();
        //Create the event without checking times
        EventController.createEventNoCheck(info, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                ref.set(event);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create event (no check): " + e.getMessage());
            }
        });
        await(latch);
    }

    /**
     * Helper for getting all notifications for an entrant
     * @param entrantId
     *      Entrant ID who's notifications to get
     * @return
     *      List of notifications

     */
    private List<Notification> getNotifications(int entrantId)  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Notification>> ref = new AtomicReference<>(new ArrayList<>());
        //Get all notifications for the entrant
        NotificationManager.getNotificationsByRecipientId(entrantId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                ref.set(notifications);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        await(latch);
        return ref.get();
    }

    /**
     * Small helper to assert the number of notifications for an entrant
     * @param entrantId
     *      Entrant ID
     * @param expected
     *      Expected number of notifications
     */
    private void assertNotificationCount(int entrantId, int expected) {
        assertEquals(expected, getNotifications(entrantId).size());
    }

    /**
     * Tests getting all past events
     * Pass if the list is the same size as the collection
     * Fail otherwise
     * @return
     *      EventInfo for a past event
     */
    private static EventInfo getPastInfo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        Date pastEventDate = cal.getTime();
        Timestamp pastEventTs = new Timestamp(pastEventDate);

        cal.add(Calendar.HOUR, -4);
        Timestamp pastRegStart = new Timestamp(cal.getTime());
        cal.add(Calendar.HOUR, -2);
        Timestamp pastRegEnd = new Timestamp(cal.getTime());

        return new EventInfo("Past", "Desc", "Loc", "Guide", "Img",
                pastEventTs, pastRegStart, pastRegEnd, 10, 5, false, "none", 0, 123);
    }


    /**
     * Tests the write and read of an event
     * Pass if the event can be read back
     * Fail otherwise
     */
    @Test
    public void testWriteAndGetEvent() {
        clearAll();
        Event event = createValidEvent();
        CountDownLatch latch = new CountDownLatch(1);
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
        await(latch);
    }

    /**
     * Tests getting an event that does not exist
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testGetEventNotFound() {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getEvent(9999, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                fail("Should not find non-existent event");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EventNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests updating an event
     * Pass if the event can be updated
     * Fail otherwise

     */
    @Test
    public void testUpdateEvent() {
        clearAll();
        Event event = createValidEvent();
        CountDownLatch latch = new CountDownLatch(1);
        EventInfo info = event.getEventInfo();
        info.setName("Updated");
        EventController.updateEventInfo(event, info, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        assertEquals("Updated", result.getEventInfo().getName());
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed after update");
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to update");
            }
        });
        await(latch);
    }

    /**
     * Tests updating an eventInfo
     * Pass if the event can be updated
     * Fail otherwise
     */
    @Test
    public void testUpdateEventInfo()  {
        clearAll();
        Event event = createValidEvent();
        EventInfo info = event.getEventInfo();
        info.setName("HI");

        CountDownLatch latch = new CountDownLatch(1);
        EventController.updateEventInfo(event, info, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                EventController.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        assertEquals("HI", result.getEventInfo().getName());
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
                fail("Failed to update event info");
            }
        });
        await(latch);
    }

    /**
     * Tests getting a new event ID from empty collection
     * Pass if the ID is 1
     * Fail otherwise
     */
    @Test
    public void testGetNewEventIdOnEmptyCollection()  {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        EventController.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                ref.set(id);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new event ID");
            }
        });
        await(latch);
        assertEquals(1, (int) ref.get());
    }

    /**
     * Tests getting a new event ID from non-empty collection
     * Pass if the ID is greater than 1
     * Fail otherwise
     */
    @Test
    public void testGetNewEventIdAfterEventExists() {
        clearAll();
        Event e1 = createValidEvent();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        EventController.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                ref.set(id);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new event ID");
            }
        });
        await(latch);
        assertTrue(ref.get() > e1.getId());
    }
    /**
     * Tests getting all events from empty collection
     * Pass if the list is empty
     * Fail otherwise
     */
    @Test
    public void testGetAllEventsEmpty() {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getAllEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                assertTrue(events.isEmpty());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get events");
            }
        });
        await(latch);
    }
    /**
     * Tests getting all events from non-empty collection
     * Pass if the list is the same size as the collection
     * Fail otherwise
     */
    @Test
    public void testGetAllEventsMultiple() {
        clearAll();
        //Make 2 events
        createValidEvent();
        createValidEvent();
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getAllEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                assertEquals(2, events.size());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get events");
            }
        });
        await(latch);
    }
    /**
     * Tests adding and removing an entrant from an event
     * Pass if the entrant is removed
     * Fail otherwise
     */
    @Test
    public void testAddEntrantAndRemoveEntrant()  {
        clearAll();
        Event event = createValidEvent();
        Entrant entrant = createEntrant("User");
        CountDownLatch latch1 = new CountDownLatch(1);
        //Add entrant to event
        EventController.addEntrantToEvent(event, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add failed");
            }
        });
        await(latch1);
        CountDownLatch latch2 = new CountDownLatch(1);
        //Remove entrant from event
        EventController.removeEntrantFromEvent(event, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Remove failed");
            }
        });
        await(latch2);
    }
    /**
     * Tests getting all entrants for an event
     * Pass if the list is the same size as the collection
     * Fail otherwise

     */
    @Test
    public void testGetEntrantsForEvent()  {
        clearAll();
        Event event = createValidEvent();
        Entrant e1 = createEntrant("A");
        Entrant e2 = createEntrant("B");
        CountDownLatch latch = new CountDownLatch(2);
        EventController.addEntrantToEvent(event, e1, new DBWriteCallback(){
            public void onSuccess(){
                latch.countDown();
            }
            public void onFailure(Exception e){
                fail("Failed to add entrant");
            }
        });
        EventController.addEntrantToEvent(event, e2, new DBWriteCallback(){
            public void onSuccess(){
                latch.countDown();
            }
            public void onFailure(Exception e){
                fail("Failed to add entrant");
            }
        });
        await(latch);
        CountDownLatch check = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> result) {
                assertEquals(2, result.size());
                check.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get entrants");
            }
        });
        await(check);
    }
    /**
     * Tests getting all entrants for an event that does not exist
     * Pass if the exception is thrown
     * Fail otherwise

     */
    @Test
    public void testGetEntrantsForEventNotFound()  {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getEntrantsForEvent(9999, new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                fail("Should not succeed for non-existent event");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EventNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }
    /**
     * Tests getting all waitlist for an event
     * Pass if the list is the same size as the collection
     * Fail otherwise
     */
    @Test
    public void testGetWaitlistForEvent() {
        clearAll();

        Event event = createValidEvent();
        Entrant e1 = createEntrant("WL1");
        Entrant e2 = createEntrant("WL2");

        CountDownLatch addLatch = new CountDownLatch(2);
        EventController.addEntrantToWaitlist(event, e1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add to waitlist");
            }
        });
        EventController.addEntrantToWaitlist(event, e2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add to waitlist");
            }
        });
        await(addLatch);

        CountDownLatch latch = new CountDownLatch(1);
        EventController.getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertEquals(2, entrants.size());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get waitlist");
            }
        });
        await(latch);
    }
    /**
     * Tests getting all events for an entrant that is not in an event
     * Pass if the list is empty
     * Fail otherwise
     */
    @Test
    public void testGetEventsForEntrantNone() {
        clearAll();
        Entrant e = createEntrant("NoEvents");
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getAllEventsForEntrant(e, new EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                assertTrue(events.isEmpty());
                assertTrue(waitEvents.isEmpty());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Should not fail when there are no events");
            }
        });
        await(latch);
    }

    /**
     * Tests getting all events for an entrant that is in an event and waitlist
     * Pass if the list is not empty
     */
    @Test
    public void testGetEventsForEntrantInEventAndWaitlist(){
        clearAll();
        //Create entrant and events
        Entrant entrant = createEntrant("Mixed");
        Event eventMain = createValidEvent();
        Event eventWait = createValidEvent();

        CountDownLatch addLatch = new CountDownLatch(2);
        //Add entrant to event and waitlist
        EventController.addEntrantToEvent(eventMain, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add to event");
            }
        });
        EventController.addEntrantToWaitlist(eventWait, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                addLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add to waitlist");
            }
        });
        await(addLatch);

        //Get all events for entrant
        CountDownLatch latch = new CountDownLatch(1);
        EventController.getAllEventsForEntrant(entrant, new EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                assertEquals(1, events.size());
                assertEquals(1, waitEvents.size());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get events for entrant");
            }
        });
        await(latch);
    }

    /**
     * Tests deleting an event with no entrants
     * Pass if the event is deleted
     * Fail otherwise
     */
    @Test
    public void testDeleteEventNoEntrants() {
        clearAll();
        Event event = createValidEvent();
        CountDownLatch del = new CountDownLatch(1);
        EventController.deleteEvent(String.valueOf(event.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Delete failed: " + e.getMessage());
            }
        });
        await(del);

        CountDownLatch check = new CountDownLatch(1);
        EventController.getEvent(event.getId(), new EventCallback() {
            @Override
            public void onSuccess(Event e) {
                fail("Event should have been deleted");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EventNotFound);
                check.countDown();
            }
        });
        await(check);
    }

    /**
     * Tests deleting an event with mixed entrants and notifications
     * Pass if the event is deleted
     * Fail otherwise
     */
    @Test
    public void testDeleteEventWithMixedEntrantsAndNotifications() {
        clearAll();

        Event event = createValidEvent();
        Entrant in = createEntrant("In");
        Entrant out = createEntrant("Out");

        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToEvent(event, in, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to add to event");
            }
        });
        await(add);

        CountDownLatch del = new CountDownLatch(1);
        EventController.deleteEvent(String.valueOf(event.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Delete failed: " + e.getMessage());
            }
        });
        await(del);
        //Entrant in event should have 1 notification
        assertNotificationCount(in.getId(), 1);
        //Entrant not in event should have none
        assertNotificationCount(out.getId(), 0);
    }

    /**
     * Tests deleting an event that does not exist
     * Pass if the exception is thrown
     */
    @Test
    public void testDeleteEventNotFound() {
        clearAll();

        CountDownLatch latch = new CountDownLatch(1);
        EventController.deleteEvent("9999", new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed deleting non-existent event");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EventNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests deleting an event with mixed entrants
     * Pass if the event is deleted
     * Fail otherwise
     */
    @Test
    public void testDeleteEventWithMixedEntrants()  {
        clearAll();

        Event event = createValidEvent();
        Entrant in = createEntrant("In");
        Entrant out = createEntrant("Out");

        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToEvent(event, in, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail();
            }
        });
        await(add);

        CountDownLatch del = new CountDownLatch(1);
        EventController.deleteEvent(String.valueOf(event.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Delete failed: " + e.getMessage());
            }
        });
        await(del);

        assertNotificationCount(in.getId(), 1);
        assertNotificationCount(out.getId(), 0);
    }
    /**
     * Tests creating an event with valid times
     * Pass if the event is created
     * Fail otherwise
     */
    @Test
    public void testCreateEventValidTimes() {
        clearAll();
        Event event = createValidEvent();
        assertNotNull(event);
        assertTrue(event.getId() > 0);
    }

    /**
     * Tests creating an event with invalid times
     * Pass if the event is not created
     * Fail otherwise
     */
    @Test
    public void testCreateEventInvalidTimes() {
        clearAll();
        //Make regStart in the past to guarantee failure
        long nowMs = System.currentTimeMillis();
        Timestamp regStart = new Timestamp((nowMs - 60_000) / 1000, 0);
        Timestamp regEnd   = new Timestamp((nowMs + 60_000) / 1000, 0);
        Timestamp event    = new Timestamp((nowMs + 120_000) / 1000, 0);

        CountDownLatch latch = new CountDownLatch(1);

        EventController.createEvent("Bad", "Desc", "Loc", "Guide", "Img", event, regStart, regEnd,
                10, 5, false, "none", 123, new EventCallback() {
                    @Override
                    public void onSuccess(Event e) {
                        fail("Should not create event with invalid times");
                    }
                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                    }
                });
        await(latch);
    }

    /**
     * Tests creating an event from an event info
     * Pass if the event is created
     * Fail otherwise
     */
    @Test
    public void testCreateEventFromEventInfo() {
        clearAll();
        List<Timestamp> times = EventController.getTestEventTimes();
        //Create event info
        EventInfo info = new EventInfo("Info Event", "Desc", "Loc",
                "Guide", "Img", times.get(2), times.get(0), times.get(1),
                20, 10, true, "dist", 0, 123);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();
        //Create event from event info
        EventController.createEvent(info, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                ref.set(event);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create from EventInfo: " + e.getMessage());
            }
        });

        await(latch);
        Event created = ref.get();
        assertNotNull(created);
        assertEquals("Info Event", created.getEventInfo().getName());
    }

    /**
     * Tests adding and removing an entrant from an event
     * Pass if the entrant is removed
     * Fail otherwise
     */
    @Test
    public void testAddAndRemoveEntrantFromEvent() {
        clearAll();
        Event event = createValidEvent();
        Entrant e = createEntrant("AddRemove");
        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToEvent(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Add failed");
            }
        });
        await(add);

        //Now remove
        CountDownLatch rem = new CountDownLatch(1);
        EventController.removeEntrantFromEvent(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                rem.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Remove failed");
            }
        });
        await(rem);

        //Check entrants list from DB is empty
        CountDownLatch check = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertTrue(entrants.isEmpty());
                check.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to load entrants after removal");
            }
        });
        await(check);
    }

    /**
     * Tests adding and removing an entrant from a waitlist
     * Pass if the entrant is removed
     * Fail otherwise
     */
    @Test
    public void testAddAndRemoveEntrantFromWaitlist() {
        clearAll();

        Event event = createValidEvent();
        Entrant e = createEntrant("Waitlist");

        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToWaitlist(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Add WL failed");
            }
        });
        await(add);

        CountDownLatch rem = new CountDownLatch(1);
        EventController.removeEntrantFromWaitlist(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                rem.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Remove WL failed");
            }
        });
        await(rem);
        CountDownLatch check = new CountDownLatch(1);
        EventController.getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertTrue(entrants.isEmpty());
                check.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to load waitlist after removal");
            }
        });
        await(check);
    }

    /**
     * Tests adding and removing an entrant from an event
     * Pass if the entrant is removed
     * Fail otherwise
     */
    @Test
    public void testAddAndRemoveEntrantsFromEvent() {
        clearAll();

        Event event = createValidEvent();
        Entrant e1 = createEntrant("R1");
        Entrant e2 = createEntrant("R2");

        CountDownLatch add = new CountDownLatch(2);
        EventController.addEntrantToEvent(event, e1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Add failed");
            }
        });
        EventController.addEntrantToEvent(event, e2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Add failed");
            }
        });
        await(add);

        List<Entrant> entrants = new ArrayList<>();
        entrants.add(e1);
        entrants.add(e2);

        CountDownLatch rem = new CountDownLatch(1);
        EventController.removeEntrantsFromEvent(event, entrants, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                rem.countDown();
            }
            @Override
            public void onFailure(Exception ex) {
                fail("Batch remove failed");
            }
        });
        await(rem);

        CountDownLatch check = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> result) {
                assertTrue(result.isEmpty());
                check.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to load entrants after batch removal");
            }
        });
        await(check);
    }

    /**
     * Tests getting all future events
     * Pass if the list is the same size as the collection
     * Fail otherwise
     */
    @Test
    public void testGetAllFutureEvents() {
        clearAll();

        //Create a future event
        Event futureEvent = createValidEvent();
        createEventNoCheck(getPastInfo());

        CountDownLatch latch = new CountDownLatch(1);
        EventController.getAllFutureEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                //Only the future event should appear
                assertEquals(1, events.size());
                assertEquals(futureEvent.getId(), events.get(0).getId());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get future events");
            }
        });
        await(latch);
    }



    /**
     *  Tests doing the lottery on an event that is completely full
     *  Pass if the lottery fails
     *  Fail otherwise
     */
    @Test
    public void testDoLotteryFullEvent()  {
        clearAll();

        Event event = createValidEvent();
        //Fill event to capacity
        for (int i = 0; i < event.getEventInfo().getMaxEntrants(); i++) {
            Entrant e = createEntrant("Full" + i);

            CountDownLatch add = new CountDownLatch(1);
            EventController.addEntrantToEvent(event, e, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }
                @Override
                public void onFailure(Exception ex) {
                    fail("Add failed");
                }
            });
            await(add);
        }
        //Now run lottery with no more entrants, should expect failure
        CountDownLatch latch = new CountDownLatch(1);
        EventController.doLottery(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Lottery should fail when no spots available");
            }
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     *  Tests doing the lottery on an event with no entrants in waitlist
     *  Pass if the lottery fails
     *  Fail otherwise
     */
    @Test
    public void testDoLotteryNoWaiting()  {
        clearAll();
        Event event = createValidEvent();
        //Make sure event has empty waitlist
        CountDownLatch latch = new CountDownLatch(1);
        EventController.doLottery(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Lottery should fail with empty waitlist");
            }
            @Override
            public void onFailure(Exception e) {
                //Expect failure
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     *  Tests doing the lottery on an event with enough space for all entrants in waitlist
     *  Pass if the lottery succeeds
     *  Fail otherwise
     */
    @Test
    public void testDoLotteryEnoughSpace() {
        clearAll();

        Event event = createValidEvent();

        //Only 2 waitlist entrants
        Entrant e1 = createEntrant("W1");
        Entrant e2 = createEntrant("W2");

        CountDownLatch addWL = new CountDownLatch(2);
        EventController.addEntrantToWaitlist(event, e1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addWL.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add to waitlist failed");
            }
        });
        EventController.addEntrantToWaitlist(event, e2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addWL.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add to waitlist failed");
            }
        });
        await(addWL);

        CountDownLatch latch = new CountDownLatch(1);
        //Event has capacity 10, but only 2 waitlist entrants
        EventController.doLottery(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Should succeed when spots > waitlist entrants");
            }
        });
        await(latch);
        //Validate both entrants are now in event
        CountDownLatch check = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertEquals(2, entrants.size());
                //Also validate waitlist is empty
                EventController.getWaitlistForEvent(event.getId(), new EntrantListCallback() {
                    @Override
                    public void onSuccess(List<Entrant> waitlist) {
                        assertTrue(waitlist.isEmpty());
                        check.countDown();
                        }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get waitlist");
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get entrants");
            }
        });
        await(check);
    }

    /**
     * Tests doing a lottery on a partially full event
     * Pass if the lottery succeeds
     * Fail otherwise
     */
    @Test
    public void testDoLotterySomeAddedPartFull() {
        clearAll();
        Event event = createValidEvent();

        //Fill event with 7 entrants 3 spots left
        for (int i = 0; i < 7; i++) {
            Entrant e = createEntrant("Fill" + i);
            CountDownLatch add = new CountDownLatch(1);
            EventController.addEntrantToEvent(event, e, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }
                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            await(add);
        }

        //Add 4 entrants to waitlist only 3 should win
        List<Entrant> waiters = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            waiters.add(createEntrant("WL" + i));
        }

        CountDownLatch addWL = new CountDownLatch(waiters.size());
        //Add 4 entrants to waitlist
        for (Entrant w : waiters) {
            EventController.addEntrantToWaitlist(event, w, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    addWL.countDown();
                }
                @Override
                public void onFailure(Exception e) {
                    fail("Add to waitlist failed");
                }
            });
        }
        await(addWL);

        //Run lottery
        CountDownLatch lottery = new CountDownLatch(1);
        EventController.doLottery(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                lottery.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Lottery failed");
            }
        });
        await(lottery);

        //Verify exactly 10 total entrants
        CountDownLatch check = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertEquals(10, entrants.size());
                check.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get entrants");
            }
        });

        await(check);
    }

    /**
     * Tests that adding an entrant to an event removes them from the waitlist
     * Pass if the entrants are removed
     * Fail otherwise
     */
    @Test
    public void testAddEntrantsToEventAddsAndRemovesFromWaitlist() {
        clearAll();

        Event event = createValidEvent();

        Entrant e1 = createEntrant("A1");
        Entrant e2 = createEntrant("A2");

        //Put both in waitlist
        CountDownLatch addWL = new CountDownLatch(2);
        EventController.addEntrantToWaitlist(event, e1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addWL.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add to waitlist failed");
            }
        });
        EventController.addEntrantToWaitlist(event, e2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                addWL.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add to waitlist failed");
            }
        });
        await(addWL);

        //Call method under test
        CountDownLatch latch = new CountDownLatch(1);
        List<Entrant> list = Arrays.asList(e1, e2);

        EventController.addEntrantsToEvent(event, list, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add failed");
            }
        });
        await(latch);

        //Now both should be in event
        CountDownLatch checkEvent = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertEquals(2, entrants.size());
                checkEvent.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get entrants");
            }
        });
        await(checkEvent);

        //And removed from waitlist
        CountDownLatch checkWL = new CountDownLatch(1);
        EventController.getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertTrue(entrants.isEmpty());
                checkWL.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get waitlist");
            }
        });
        await(checkWL);
    }

    /**
     * Tests adding an entrant to an event that is in an event
     * Pass if the entrant is not added
     * Fail otherwise
     */
    @Test
    public void testAddToWaitlistWhenInEvent() {
        clearAll();
        Event event = createValidEvent();

        Entrant e = createEntrant("InEvent");

        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToEvent(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                add.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Add failed");
            }
        });
        await(add);
        //Now try to add to waitlist
        CountDownLatch addWL = new CountDownLatch(1);
        EventController.addEntrantToWaitlist(event, e, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Should not add to waitlist when in event");
            }
            @Override
            public void onFailure(Exception e) {
                //Expect failure
                addWL.countDown();
            }
        });
        await(addWL);
    }

    /**
     * Tests getting all events for an organizer
     * Pass if the list is not empty
     * Fail otherwise
     */
    @Test
    public void testGetEventsForOrganizer() {
        clearAll();
        //Create an organizer
        try {
            Entrant organizer = createEntrant("Organizer");
            //Now create an event for the organizer
            Event event = createValidEvent();
            event.getEventInfo().setOrganizerID(organizer.getId());
            //Save event
            EventController.writeEvent(event, new DBWriteCallback()
            {
                @Override
                public void onSuccess() {
                    //Now get events for organizer
                    EventController.getEventsForOrganizer(organizer.getId(), new EventListCallback() {
                        @Override
                        public void onSuccess(List<Event> events) {
                            assertEquals(1, events.size());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            fail("Failed to get events for organizer");
                        }
                    });
                }
                @Override
                public void onFailure(Exception e) {
                    fail("Failed to save event");
                }
            });
        }
        catch (Exception e) {
            fail("Failed to create organizer");
        }

        //Create an event for the organizer
    }

    /**
     * Tests that the lottery sends invites to all entrants that win
     * Pass if the invites are sent
     * Fail otherwise
     */
    @Test
    public void testLotterySendsInvites() {
        clearAll();
        Entrant organizer = null;
        Entrant e1 = null;
        Entrant e2 = null;
        Entrant e3 = null;

        //Create an organizer and 3 entrants
        try {
            organizer = createEntrant("Organizer");
        }
        catch (Exception e) {
            fail("Failed to create organizer");
        }
        try {
            e1 = createEntrant("E1");
        }
        catch (Exception e) {
            fail("Failed to create entrant 1");
        }
        try {
            e2 = createEntrant("E2");
        }
        catch (Exception e) {
            fail("Failed to create entrant 2");
        }
        try {
            e3 = createEntrant("E3");
        }
        catch (Exception e) {
            fail("Failed to create entrant 3");
        }

        try {
            //Now create an event for the organizer
            Event event = createValidEvent();
            event.getEventInfo().setOrganizerID(organizer.getId());
            //Set event cap to 2
            event.getEventInfo().setMaxEntrants(2);
            //Save event
            CountDownLatch save = new CountDownLatch(1);
            EventController.writeEvent(event, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    save.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to save event");
                }
            });
            await(save);

            CountDownLatch add = new CountDownLatch(3);
            EventController.addEntrantToWaitlist(event, e1, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            EventController.addEntrantToWaitlist(event, e2, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            EventController.addEntrantToWaitlist(event, e3, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            await(add);


            //Now run lottery
            CountDownLatch lottery = new CountDownLatch(1);
            EventController.doLottery(event, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    lottery.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Lottery failed");
                }
            });
            await(lottery);

            AtomicInteger sent = new AtomicInteger(0);
            CountDownLatch check = new CountDownLatch(3);
            NotificationManager.getInvitationByRecipientId(e1.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            NotificationManager.getInvitationByRecipientId(e2.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            NotificationManager.getInvitationByRecipientId(e3.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            await(check);
            assertEquals(2, sent.get());
        }
        catch (Exception e) {
            fail("Something else went wrong " + e.getMessage());
        }

    }

    /**
     * Tests that the lottery sends notifications to all entrants that lose
     * Pass if the notifications are sent
     * Fail otherwise
     */
    @Test
    public void testLotterySendsNotifications() {
        clearAll();
        Entrant organizer = null;
        Entrant e1 = null;
        Entrant e2 = null;
        Entrant e3 = null;

        //Create an organizer and 3 entrants
        try {
            organizer = createEntrant("Organizer");
        } catch (Exception e) {
            fail("Failed to create organizer");
        }
        try {
            e1 = createEntrant("E1");
        } catch (Exception e) {
            fail("Failed to create entrant 1");
        }
        try {
            e2 = createEntrant("E2");
        } catch (Exception e) {
            fail("Failed to create entrant 2");
        }
        try {
            e3 = createEntrant("E3");
        } catch (Exception e) {
            fail("Failed to create entrant 3");
        }
        try {
            //Now create an event for the organizer
            Event event = createValidEvent();
            event.getEventInfo().setOrganizerID(organizer.getId());
            //Set event cap to 2
            event.getEventInfo().setMaxEntrants(2);
            //Save event
            CountDownLatch save = new CountDownLatch(1);
            EventController.writeEvent(event, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    save.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to save event");
                }
            });
            await(save);
            CountDownLatch add = new CountDownLatch(3);
            EventController.addEntrantToWaitlist(event, e1, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            EventController.addEntrantToWaitlist(event, e2, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            EventController.addEntrantToWaitlist(event, e3, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    add.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Add failed");
                }
            });
            await(add);
            //Now run lottery
            CountDownLatch lottery = new CountDownLatch(1);
            EventController.doLottery(event, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    lottery.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Lottery failed");
                }
            });
            await(lottery);
            //Check notifications
            AtomicInteger sent = new AtomicInteger(0);
            CountDownLatch check = new CountDownLatch(3);
            NotificationManager.getNotificationsByRecipientId(e1.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            NotificationManager.getNotificationsByRecipientId(e2.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            NotificationManager.getNotificationsByRecipientId(e3.getId(), new NotificationListCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (!notifications.isEmpty()) {
                        sent.incrementAndGet();
                    }
                    check.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get notifications");
                }
            });
            await(check);
            assertEquals(1, sent.get());

        } catch (Exception e) {
            fail("Something else went wrong " + e.getMessage());
        }
    }


}


