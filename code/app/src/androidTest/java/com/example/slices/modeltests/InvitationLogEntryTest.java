package com.example.slices.modeltests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogType;

/**
 * Tests for the InvitationLogEntry class
 * @author Ryan Haubrich
 * @version 1.0
 */

public class InvitationLogEntryTest {

    /**
     * Tests default constructor initializes
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        InvitationLogEntry log = new InvitationLogEntry();
        assertEquals(LogType.INVITATION, log.getType());
        assertFalse(log.isRead());
        assertNull(log.getMessage());
        assertNull(log.getTimestamp());
        assertNull(log.getLogId());
        assertNull(log.getNotificationId());
        assertEquals(0, log.getSenderId());
        assertEquals(0, log.getRecipientId());
        assertEquals(0, log.getEventId());
        assertFalse(log.isAccepted());
        assertFalse(log.isDeclined());
    }

    /**
     * Tests constructor copying all fields from Invitation
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testConstructorFromInvitation() {
        Invitation inv = new Invitation("Title", "Body", "notif123", 10, 20, 99);
        inv.setAccepted(true);
        inv.setDeclined(false);
        inv.setRead(true);
        InvitationLogEntry log = new InvitationLogEntry(inv, "log001");
        assertEquals("Title Body", log.getMessage());
        assertEquals(inv.getTimestamp(), log.getTimestamp());
        assertEquals("notif123", log.getNotificationId());
        assertEquals(20, log.getSenderId());
        assertEquals(10, log.getRecipientId());
        assertEquals(99, log.getEventId());
        assertTrue(log.isAccepted());
        assertFalse(log.isDeclined());
        assertTrue(log.isRead());
        assertEquals("log001", log.getLogId());
        assertEquals(LogType.INVITATION, log.getType());
    }

    /**
     * Tests propagation of unread
     * Pass if the log is not marked as read
     * Fail otherwise
     */
    @Test
    public void testUnreadPropagation() {
        Invitation inv = new Invitation("X", "Y", "id1", 1, 2, 3);
        assertFalse(inv.getRead());
        InvitationLogEntry log = new InvitationLogEntry(inv, "log10");
        assertFalse(log.isRead());
    }

    /**
     * Tests accepted and declined propagation correctly
     * Pass if the log is marked as accepted and declined
     * Fail otherwise
     */
    @Test
    public void testAcceptedDeclinedPropagation() {
        Invitation inv = new Invitation("A", "B", "id2", 1, 2, 55);
        inv.setAccepted(true);
        inv.setDeclined(true);
        InvitationLogEntry log = new InvitationLogEntry(inv, "log777");
        assertTrue(log.isAccepted());
        assertTrue(log.isDeclined());
    }

    /**
     * Tests markAsRead sets read = true
     * Pass if the log is marked as read
     * Fail otherwise
     */
    @Test
    public void testMarkAsRead() {
        InvitationLogEntry log = new InvitationLogEntry();
        assertFalse(log.isRead());
        log.markAsRead();
        assertTrue(log.isRead());
    }

}