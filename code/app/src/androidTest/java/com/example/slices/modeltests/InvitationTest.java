package com.example.slices.modeltests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.slices.models.Invitation;
import com.example.slices.models.NotificationType;

public class InvitationTest {

    /**
     * Tests default constructor initializes meaningful defaults
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        Invitation inv = new Invitation();
        assertNull(inv.getId());
        assertNull(inv.getTitle());
        assertNull(inv.getBody());
        assertEquals(0, inv.getSenderId());
        assertEquals(0, inv.getRecipientId());
        assertEquals(0, inv.getEventId());
        assertFalse(inv.getRead());
        assertFalse(inv.isAccepted());
        assertFalse(inv.isDeclined());
    }

    /**
     * Tests full constructor initializes all fields correctly
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        Invitation inv = new Invitation("Congrats!", "You got in!", "abc123", 10, 20, 33);

        assertEquals("abc123", inv.getId());
        assertEquals("Congrats!", inv.getTitle());
        assertEquals("You got in!", inv.getBody());
        assertEquals(10, inv.getRecipientId());
        assertEquals(20, inv.getSenderId());
        assertEquals(33, inv.getEventId());
        assertFalse(inv.getRead());
        assertNotNull(inv.getTimestamp());
        assertEquals(NotificationType.INVITATION, inv.getType());
        assertFalse(inv.isAccepted());
        assertFalse(inv.isDeclined());
    }

    /**
     * Tests marking an invitation as accepted
     * Pass if the flag is set correctly
     * Fail otherwise
     */
    @Test
    public void testAcceptInvitation() {
        Invitation inv = new Invitation("T", "B", "id1", 1, 2, 3);
        assertFalse(inv.isAccepted());
        inv.setAccepted(true);
        assertTrue(inv.isAccepted());
        // Accepting does not automatically decline
        assertFalse(inv.isDeclined());
    }

    /**
     * Tests marking an invitation as declined
     * Pass if the flag is set correctly
     * Fail otherwise
     */
    @Test
    public void testDeclineInvitation() {
        Invitation inv = new Invitation("T", "B", "id2", 1, 2, 3);
        assertFalse(inv.isDeclined());
        inv.setDeclined(true);
        assertTrue(inv.isDeclined());
        // Declining does not accept it
        assertFalse(inv.isAccepted());
    }
}





