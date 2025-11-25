package com.example.slices.modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.models.Entrant;
import com.example.slices.models.Waitlist;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Waitlist class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testWaitlist {
    private Waitlist w;
    private Entrant e;
    private Entrant e2;

    /**
     * Sets up the test by creating a Waitlist object and two Entrant objects
     */
    @Before
    public void setup() {
        w = new Waitlist(2);
        e = new Entrant("John", "Doe", "2", 1);
        e2 = new Entrant("Jane", "Doe", "2", 2);
    }

    /**
     * Test the default constructor of the Waitlist class
     */
    @Test
    public void testDefaultConstructor() {
        Waitlist w = new Waitlist();
        assertEquals(32768, w.getMaxCapacity());
        assertEquals(0, w.getCurrentEntrants());
        assertEquals(0, w.getEntrants().size());
    }

    /**
     * Test the constructor of the Waitlist class with a custom maximum capacity
     */
    @Test
    public void testConstructor() {
        assertEquals(2, w.getMaxCapacity());
        assertEquals(0, w.getCurrentEntrants());
        assertTrue(w.isEmpty());
    }

    /**
     * Test the addEntrant method of the Waitlist class
     */
    @Test
    public void testAddEntrant() {
        w.addEntrant(e);
        assertEquals(1, w.getCurrentEntrants());
        assertEquals(e, w.getEntrants().get(0));
        assertFalse(w.isEmpty());
    }

    /**
     * Test the addEntrant method of the Waitlist class when the waitlist is full
     */
    @Test
    public void testAddEntrantFull() {
        w.addEntrant(e);
        w.addEntrant(e2);
        try {
            w.addEntrant(new Entrant("John", "Doe", "2", 3));
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Waitlist is full", e.getMessage());
        }
    }

    /**
     * Test the removeEntrant method of the Waitlist class
     */
    @Test
    public void testRemoveEntrant() {
        w.addEntrant(e);
        w.addEntrant(e2);
        w.removeEntrant(e);
        assertEquals(1, w.getCurrentEntrants());
        assertEquals(e2, w.getEntrants().get(0));
        assertFalse(w.isEmpty());

    }

    /**
     * Test the removeEntrant method of the Waitlist class when the waitlist is empty
     */
    @Test
    public void testRemoveEntrantEmpty() {
        try {
            w.removeEntrant(e);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Waitlist is empty", e.getMessage());
        }

    }

    /**
     * Test the removeEntrant method of the Waitlist class when the entrant is not on the waitlist
     */
    @Test
    public void testRemoveEntrantNotFound() {
        w.addEntrant(e);
        try {
            w.removeEntrant(e2);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Entrant not on waitlist", e.getMessage());
        }
    }

    /**
     * Test the clearWaitlist method of the Waitlist class
     */
    @Test
    public void testClearWaitlist() {
        w.addEntrant(e);
        w.addEntrant(e2);
        w.clearWaitlist();
        assertEquals(0, w.getCurrentEntrants());
        assertTrue(w.isEmpty());
    }
    /**
     * Test the getter methods of the Waitlist class
     */
    @Test
    public void testGetters() {
        w.addEntrant(e);
        w.addEntrant(e2);
        assertEquals(2, w.getMaxCapacity());
        Entrant er = w.getEntrant(e);
        assertEquals(e, er);
        er = w.getEntrant(e.getId());
        assertEquals(e, er);
        assertNull(w.getEntrant(new Entrant("John", "Doe", "2", 3)));
        assertNull(w.getEntrant(3));
    }

    /**
     * Test the setter methods of the Waitlist class
     */
    @Test
    public void testSetters() {
        w.setMaxCapacity(4);
        assertEquals(4, w.getMaxCapacity());
        w.setCurrentEntrants(2);
        assertEquals(2, w.getCurrentEntrants());

    }

    /**
     * Test the isEmpty method of the Waitlist class
     */
    @Test
    public void testIsEmpty() {
        assertTrue(w.isEmpty());
        w.addEntrant(e);
        assertFalse(w.isEmpty());
        w.removeEntrant(e);
        assertTrue(w.isEmpty());
    }















}

