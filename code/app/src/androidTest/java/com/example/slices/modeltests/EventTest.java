package com.example.slices.modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import com.example.slices.exceptions.DuplicateEntry;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.exceptions.EventFull;
import com.example.slices.exceptions.WaitlistFull;

import com.example.slices.models.Entrant;
import com.example.slices.models.Event;


import com.example.slices.models.EventInfo;
import com.example.slices.models.Image;
import com.google.firebase.Timestamp;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Tests for the Event class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class EventTest {
    /**
     * Utility method to make timestamps easily
     * @param millis
     *      Milliseconds since epoch
     * @return
     *      Timestamp object
     */
    private Timestamp ts(long millis) {
        return new Timestamp(
                millis / 1000,                     // seconds
                (int) ((millis % 1000) * 1_000_000) // nanos
        );
    }

    /**
     * Creates a simple EventInfo for testing
     * @param id
     *      ID of the event
     * @param maxEntrants
     *      Maximum number of entrants
     * @param maxWait
     *      Maximum number of waitlist entrants
     * @return
     *      EventInfo object
     */
    private EventInfo makeInfo(int id, int maxEntrants, int maxWait) {
        long now = System.currentTimeMillis();
        return new EventInfo(
                "Name", "Desc", "Loc", "Guide", "Img",
                ts(now + 10_000), ts(now), ts(now + 5_000),
                maxEntrants, maxWait, false, "none", id, 123, new Image()
        );
    }

    /**
     * Tests constructor with full parameters
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        long now = System.currentTimeMillis();
        Event e = new Event(
                "Name", "Desc", "Loc", "Guide", "Img",
                ts(now + 10000), ts(now), ts(now + 5000),
                5, 3, false, "none", 77, 999, new Image());

        assertEquals(77, e.getId());
        assertNotNull(e.getEntrants());
        assertNotNull(e.getWaitlist());
        assertEquals(0, e.getEntrants().size());
        assertEquals(3, e.getWaitlist().getMaxCapacity());
    }

    /**
     * Tests constructor taking an EventInfo
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testConstructorEventInfo() {
        EventInfo info = makeInfo(55, 10, 5);
        Event e = new Event(info);
        assertEquals(55, e.getId());
        assertNotNull(e.getEntrants());
        assertEquals(0, e.getEntrants().size());
        assertEquals(5, e.getWaitlist().getMaxCapacity());
    }

    /**
     * Tests addEntrant success
     * Pass if the entrant is added to the list
     * Fail otherwise
     */
    @Test
    public void testAddEntrantSuccess() {
        EventInfo info = makeInfo(1, 2, 0);
        Event e = new Event(info);

        Entrant a = new Entrant("A", 10);
        assertTrue(e.addEntrant(a));
        assertEquals(1, e.getEntrants().size());
        assertEquals(1, e.getEventInfo().getCurrentEntrants());
    }

    /**
     * Tests adding duplicate entrant throws DuplicateEntry
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testAddEntrantDuplicate() {
        EventInfo info = makeInfo(1, 3, 0);
        Event e = new Event(info);

        Entrant a = new Entrant("A", 10);

        e.addEntrant(a);

        try {
            e.addEntrant(a);
            fail("Should throw DuplicateEntry");
        }
        catch(DuplicateEntry duplicateEntry) {
            // Expected
        }
    }

    /**
     * Tests adding entrant to a full event throws EventFull
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testAddEntrantEventFull() {
        EventInfo info = makeInfo(1, 1, 0);
        Event e = new Event(info);
        e.addEntrant(new Entrant("A", 10));
        try {
            e.addEntrant(new Entrant("B", 20));
            fail("Should throw EventFull");
        } catch (EventFull ignored) {
            // Expected
        }
    }

    /**
     * Tests removing entrant successfully
     * Pass if the entrant is removed from the list
     * Fail otherwise
     */
    @Test
    public void testRemoveEntrantSuccess() {
        EventInfo info = makeInfo(1, 3, 0);
        Event e = new Event(info);
        Entrant a = new Entrant("A", 10);
        e.addEntrant(a);
        assertTrue(e.removeEntrant(a));
        assertEquals(0, e.getEntrants().size());
        assertEquals(0, e.getEventInfo().getCurrentEntrants());
    }

    /**
     * Tests removing entrant not in event throws EntrantNotFound
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testRemoveEntrantNotFound() {
        EventInfo info = makeInfo(1, 3, 0);
        Event e = new Event(info);
        Entrant a = new Entrant("A", 10);
        try {
            e.removeEntrant(a);
            fail("Should throw EntrantNotFound");
        }
        catch (EntrantNotFound ignored) {

        }
    }

    /**
     * Tests adding entrant to waitlist successfully
     * Pass if the entrant is added to the list
     * Fail otherwise
     */
    @Test
    public void testAddToWaitlistSuccess() {
        EventInfo info = makeInfo(1, 3, 2);
        Event e = new Event(info);
        Entrant a = new Entrant("A", 10);
        assertTrue(e.addEntrantToWaitlist(a));
        assertEquals(1, e.getWaitlist().getEntrants().size());
    }

    /**
     * Tests adding duplicate to waitlist throws DuplicateEntry
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testAddToWaitlistDuplicate() {
        EventInfo info = makeInfo(1, 3, 2);
        Event e = new Event(info);

        Entrant a = new Entrant("A", 10);
        e.addEntrantToWaitlist(a);
        try {
            e.addEntrantToWaitlist(a);
            fail("Should throw DuplicateEntry");
        }
        catch (DuplicateEntry ignored) {

        }
    }

    /**
     * Tests adding to full waitlist throws WaitlistFull
     * Pass if the exception is thrown
     * Fail otherwise
     */
    @Test
    public void testWaitlistFull() {
        EventInfo info = makeInfo(1, 3, 1);
        Event e = new Event(info);
        e.addEntrantToWaitlist(new Entrant("A", 10));
        try {
            e.addEntrantToWaitlist(new Entrant("B", 20));
            fail("Should throw WaitlistFull");
        }
        catch (WaitlistFull ignored) {

        }
    }

    /**
     * Tests removing entrant from waitlist
     * Pass if the entrant is removed from the list
     * Fail otherwise
     */
    @Test
    public void testRemoveFromWaitlist() {
        EventInfo info = makeInfo(1, 3, 2);
        Event e = new Event(info);
        Entrant a = new Entrant("A", 10);
        e.addEntrantToWaitlist(a);
        assertTrue(e.removeEntrantFromWaitlist(a));
        assertEquals(0, e.getWaitlist().getEntrants().size());
    }

    /**
     * Removing someone not on waitlist should return false
     * Pass if false is returned
     * Fail otherwise
     */
    @Test
    public void testRemoveFromWaitlistNotFound() {
        EventInfo info = makeInfo(1, 3, 1);
        Event e = new Event(info);
        Entrant a = new Entrant("A", 10);
        assertFalse(e.removeEntrantFromWaitlist(a));
    }

    /**
     * Tests compareTo sorts by event date
     * Pass if the events are sorted correctly
     * Fail otherwise
     */
    @Test
    public void testCompareTo() {
        long now = System.currentTimeMillis();
        EventInfo early = new EventInfo("E", "D", "L", "G", "I",
                ts(now + 1000), ts(now), ts(now + 500),
                5, 5, false, "none", 1, 111, new Image());
        EventInfo late = new EventInfo("E", "D", "L", "G", "I",
                ts(now + 5000), ts(now), ts(now + 500),
                5, 5, false, "none", 2, 111, new Image());
        Event e1 = new Event(early);
        Event e2 = new Event(late);
        assertTrue(e1.compareTo(e2) < 0);
        assertTrue(e2.compareTo(e1) > 0);
    }

    /**
     * Tests equals by ID
     * Pass if the events are equal
     * Fail otherwise
     */
    @Test
    public void testEquals() {
        Event e1 = new Event(makeInfo(1, 3, 1));
        Event e2 = new Event(makeInfo(1, 3, 1));
        Event e3 = new Event(makeInfo(2, 3, 1));
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
    }

    /**
     * Tests hashCode matches equals logic
     * Pass if the hash codes are the same
     * Fail otherwise
     */
    @Test
    public void testHashCode() {
        Event e1 = new Event(makeInfo(10, 3, 1));
        Event e2 = new Event(makeInfo(10, 3, 1));

        assertEquals(e1.hashCode(), e2.hashCode());
    }

    /**
     * Tests setEntrants
     * Pass if the list is set correctly
     * Fail otherwise
     */
    @Test
    public void testSetEntrants() {
        EventInfo info = makeInfo(1, 3, 1);
        Event e = new Event(info);
        List<Entrant> list = new ArrayList<>();
        list.add(new Entrant("A", 10));
        e.setEntrants(list);
        assertEquals(1, e.getEntrants().size());
    }

    /**
     * Tests setEventInfo
     * Pass if the event info is set correctly
     * Fail otherwise
     */
    @Test
    public void testSetEventInfo() {
        EventInfo info1 = makeInfo(1, 3, 1);
        EventInfo info2 = makeInfo(2, 5, 2);
        Event e = new Event(info1);
        e.setEventInfo(info2);
        assertEquals(2, e.getEventInfo().getId());
    }
}



