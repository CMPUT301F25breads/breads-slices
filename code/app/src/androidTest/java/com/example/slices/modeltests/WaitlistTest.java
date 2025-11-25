package com.example.slices.modeltests;

import org.junit.Before;
import org.junit.Test;
import java.util.List;


import static org.junit.Assert.*;

import com.example.slices.models.Entrant;
import com.example.slices.models.Waitlist;

/**
 * Tester for the Waitlist class
 */
public class WaitlistTest {

    private Waitlist waitlist;
    private Entrant e1;
    private Entrant e2;
    private Entrant e3;

    @Before
    public void setup() {
        waitlist = new Waitlist(); // default capacity 32768

        e1 = new Entrant("Name1", "e1@test.com", "111", 1);
        e2 = new Entrant("Name2", "e2@test.com", "222", 2);
        e3 = new Entrant("Name3", "e3@test.com", "333", 3);
    }

    /**
     * Tests the default constructor
     * Pass if the waitlist is empty, the current number of entrants is 0, the maximum capacity is 32768
     * and the entrant IDs list is empty
     * Fail if any of these conditions are not met
     */
    @Test
    public void testDefaultConst() {
        assertTrue(waitlist.isEmpty());
        assertEquals(0, waitlist.getCurrentEntrants());
        assertEquals(32768, waitlist.getMaxCapacity());
        assertTrue(waitlist.getEntrantIds().isEmpty());
    }

    /**
     * Tests the constructor with a custom maximum capacity
     * Pass if the waitlist capacity is 5 and the waitlist is empty
     * Fail otherwise
     */
    @Test
    public void testSetConst() {
        Waitlist wl = new Waitlist(5);
        assertEquals(5, wl.getMaxCapacity());
        assertTrue(wl.isEmpty());
    }

    /**
     * Tests adding an entrant to the waitlist
     * Pass if the current number of entrants is 1, the waitlist contains the entrant, and the entrant ID is tracked
     * Fail otherwise
     */
    @Test
    public void testAdd() {
        waitlist.addEntrant(e1);
        assertEquals(1, waitlist.getCurrentEntrants());
        assertEquals(1, waitlist.getEntrants().size());
        assertTrue(waitlist.getEntrantIds().contains(e1.getId()));
    }

    /**
     * Tests adding multiple entrants to the waitlist
     * Pass if the current number of entrants is 2, the waitlist contains both entrants, and both entrant IDs are tracked
     * Fail otherwise
     */
    @Test
    public void testAddMul() {
        waitlist.addEntrant(e1);
        waitlist.addEntrant(e2);
        assertEquals(2, waitlist.getCurrentEntrants());
        assertTrue(waitlist.getEntrants().contains(e2));
        assertTrue(waitlist.getEntrantIds().contains(2));
    }


    /**
     * Tests removing an entrant from the waitlist
     * Pass if the current number of entrants is 1, the waitlist does not contain the entrant, and the entrant ID is no longer tracked
     * Fail otherwise
     */
    @Test
    public void testRemoveEntrant() {
        waitlist.addEntrant(e1);
        waitlist.addEntrant(e2);
        waitlist.removeEntrant(e1);
        assertEquals(1, waitlist.getCurrentEntrants());
        assertFalse(waitlist.getEntrants().contains(e1));
        assertFalse(waitlist.getEntrantIds().contains(1));
    }

    /**
     * Tests if waitlist is empty after adding an entrant
     * Pass if the waitlist is not empty
     * Fail otherwise
     */
    @Test
    public void testIsEmptyFalseAfterAdd() {
        waitlist.addEntrant(e1);
        assertFalse(waitlist.isEmpty());
    }

    /**
     * Tests if entrantIds are properly tracked
     * Pass if the waitlist contains all three entrant IDs
     * Fail otherwise
     */
    @Test
    public void testEntrantIdsTracking() {
        waitlist.addEntrant(e1);
        waitlist.addEntrant(e2);
        waitlist.addEntrant(e3);
        List<Integer> ids = waitlist.getEntrantIds();
        assertEquals(3, ids.size());
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
        assertTrue(ids.contains(3));
    }
}



