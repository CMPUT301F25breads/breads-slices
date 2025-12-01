package com.example.slices.modeltests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.slices.models.Invitation;
import com.example.slices.models.NotSelected;
import com.example.slices.models.NotificationType;

public class NotSelectedTest {

    /**
     * Tests default constructor initializes meaningful defaults
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        NotSelected notSelected = new NotSelected();
        assertNull(notSelected.getId());
        assertNull(notSelected.getTitle());
        assertNull(notSelected.getBody());
        assertEquals(0, notSelected.getSenderId());
        assertEquals(0, notSelected.getRecipientId());
        assertEquals(0, notSelected.getEventId());
        assertFalse(notSelected.getRead());
        assertFalse(notSelected.isStayed());
        assertFalse(notSelected.isDeclined());
    }

    /**
     * Tests full constructor initializes all fields correctly
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        NotSelected notSelected = new NotSelected("You were not Selected", "You can still stay in the waitlist", "abc123", 10, 20, 33);

        assertEquals("abc123", notSelected.getId());
        assertEquals("You were not Selected", notSelected.getTitle());
        assertEquals("You can still stay in the waitlist", notSelected.getBody());
        assertEquals(10, notSelected.getRecipientId());
        assertEquals(20, notSelected.getSenderId());
        assertEquals(33, notSelected.getEventId());
        assertFalse(notSelected.getRead());
        assertNotNull(notSelected.getTimestamp());
        assertEquals(NotificationType.NOT_SELECTED, notSelected.getType());
        assertFalse(notSelected.isStayed());
        assertFalse(notSelected.isDeclined());
    }

    /**
     * Tests setting the stayed flag
     * Pass if the flag is set correctly
     * Fail otherwise
     */
    @Test
    public void testSetStayed() {
        NotSelected inv = new NotSelected("T", "B", "id1", 1, 2, 3);
        assertFalse(inv.isStayed());
        inv.setStayed(true);
        assertTrue(inv.isStayed());
        assertFalse(inv.isDeclined());
    }

    /**
     * Tests setting the declined flag
     * Pass if the flag is set correctly
     * Fail otherwise
     */
    @Test
    public void testSetDeclined() {
        NotSelected inv = new NotSelected("T", "B", "id1", 1, 2, 3);
        assertFalse(inv.isDeclined());
        inv.setDeclined(true);
        assertTrue(inv.isDeclined());
        assertFalse(inv.isStayed());
    }
}





