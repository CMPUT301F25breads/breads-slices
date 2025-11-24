package com.example.slices.modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.slices.models.Profile;

import org.junit.Test;

public class ProfileTest {
    /**
     * Tests default constructor
     * Pass if fields initialize to null/default values
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        Profile p = new Profile();

        assertNull(p.getName());
        assertNull(p.getEmail());
        assertNull(p.getPhoneNumber());
        assertEquals(0, p.getId());
        assertFalse(p.getSendNotifications()); // default = false
    }

    /**
     * Tests constructor with all fields
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        Profile p = new Profile("John", "j@a.com", "555", true, 10);

        assertEquals("John", p.getName());
        assertEquals("j@a.com", p.getEmail());
        assertEquals("555", p.getPhoneNumber());
        assertTrue(p.getSendNotifications());
        assertEquals(10, p.getId());
    }

    /**
     * Tests constructor with ID only
     * sendNotifications should default to true
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testIdOnlyConstructor() {
        Profile p = new Profile(7);

        assertEquals(7, p.getId());
        assertTrue(p.getSendNotifications());
        assertNull(p.getName());
        assertNull(p.getEmail());
        assertNull(p.getPhoneNumber());
    }

    /**
     * Tests constructor with name/email/phone/id
     * sendNotifications should default to true
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testConstructorWithContactInfo() {
        Profile p = new Profile("Alice", "a@b.com", "123", 99);

        assertEquals("Alice", p.getName());
        assertEquals("a@b.com", p.getEmail());
        assertEquals("123", p.getPhoneNumber());
        assertEquals(99, p.getId());
        assertTrue(p.getSendNotifications());
    }



}
