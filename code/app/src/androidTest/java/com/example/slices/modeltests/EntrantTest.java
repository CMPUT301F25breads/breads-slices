package com.example.slices.modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import com.example.slices.models.Entrant;

import org.junit.Test;

import java.util.List;

/**
 * Tests for the Entrant class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class EntrantTest {
    /**
     * Tests creating an entrant with only device ID
     */
    @Test
    public void testCreateEntrantWithDeviceId() {
        Entrant e = new Entrant("dev123");
        assertNotNull(e.getProfile());
        assertEquals("dev123", e.getDeviceId());
        assertEquals(0, e.getParent());              // default
        assertNotNull(e.getSubEntrants());
        assertEquals(0, e.getSubEntrants().size());
    }

    /**
     * Tests creating an entrant using the primary constructor
     */
    @Test
    public void testCreateEntrantPrimaryConstructor() {
        Entrant e = new Entrant("dev999", 42);
        assertEquals(42, e.getId());
        assertEquals("dev999", e.getDeviceId());
        assertNotNull(e.getSubEntrants());
        assertEquals(0, e.getSubEntrants().size());
    }

    /**
     * Tests constructor with explicit name/email/phone and ID
     */
    @Test
    public void testCreateEntrantWithProfileData() {
        Entrant e = new Entrant("John", "j@a.com", "555-2222", 10);
        assertEquals(10, e.getId());
        assertEquals("John", e.getProfile().getName());
        assertEquals("j@a.com", e.getProfile().getEmail());
        assertEquals("555-2222", e.getProfile().getPhoneNumber());
    }

    /**
     * Tests creating a secondary entrant with a parent
     */
    @Test
    public void testCreateSecondaryEntrantAddsToParentSubList() {
        Entrant parent = new Entrant("PARENT", 100);
        Entrant child = new Entrant("Kid", "k@a.com", "555", 200, parent);
        assertEquals(100, parent.getId());
        assertEquals(200, child.getId());
        assertEquals(parent.getId(), child.getParent());
        List<Integer> subs = parent.getSubEntrants();
        assertEquals(1, subs.size());
        assertTrue(subs.contains(200));
    }

    /**
     * Tests that creating a secondary entrant with a parent who already has a parent throws exception
     */
    @Test
    public void testSecondaryEntrantWithParent() {
        Entrant root = new Entrant("ROOT", 1);
        Entrant middle = new Entrant("Middle", "m@a.com", "222", 2, root);
        try {
            new Entrant("Fail", "f@a.com", "333", 3, middle);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Cant have parent with parent", e.getMessage());
        }
    }




    /**
     * Tests equals based on ID
     */
    @Test
    public void testEquals() {
        Entrant e1 = new Entrant("A", 50);
        Entrant e2 = new Entrant("B", 50);
        Entrant e3 = new Entrant("C", 51);
        assertEquals(e1, e2);      // Same ID
        assertNotEquals(e1, e3);     // Different ID
    }

    /**
     * Tests hashCode consistency
     */
    @Test
    public void testHashCode() {
        Entrant e1 = new Entrant("X", 99);
        Entrant e2 = new Entrant("Y", 99);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

}

