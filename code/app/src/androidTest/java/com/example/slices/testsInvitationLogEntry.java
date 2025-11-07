package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogType;

import org.junit.Test;

/**
 * Tests for the InvitationLogEntry class
 * @author Ryan Haubrich
 * @version 1.0
 */

public class testsInvitationLogEntry {

    private InvitationLogEntry invitationLogEntry;
    private Invitation invitation;

    /**
     * Tests the constructor for the InvitationLogEntry class
     */
    @Test
    public void testConstructor() {
        invitation = new Invitation("Test Title", "Test Body", 1, 2, 3, 4);
        invitationLogEntry = new InvitationLogEntry(invitation, 1);

        assertEquals("Test Title Test Body", invitationLogEntry.getMessage());
        assertEquals(3, invitationLogEntry.getSenderId());
        assertEquals(2, invitationLogEntry.getRecipientId());
        assertEquals(4, invitationLogEntry.getEventId());
        assertEquals(1, invitationLogEntry.getNotificationId());
        assertEquals(LogType.INVITATION, invitationLogEntry.getType());
        assertFalse(invitationLogEntry.isRead());
    }

    /**
     * Tests the setter and getter methods for the InvitationLogEntry class
     */
    @Test
    public void testSetterAndGetters() {
        invitationLogEntry = new InvitationLogEntry();
        invitationLogEntry.setSenderId(1);
        invitationLogEntry.setRecipientId(2);
        invitationLogEntry.setEventId(3);
        invitationLogEntry.setNotificationId(4);
        invitationLogEntry.setRead(true);
        invitationLogEntry.setAccepted(true);
        invitationLogEntry.setDeclined(true);


        assertEquals(1, invitationLogEntry.getSenderId());
        assertEquals(2, invitationLogEntry.getRecipientId());
        assertEquals(3, invitationLogEntry.getEventId());
        assertEquals(4, invitationLogEntry.getNotificationId());
        assertEquals(LogType.INVITATION, invitationLogEntry.getType());
        assertTrue(invitationLogEntry.isRead());
        assertTrue(invitationLogEntry.isAccepted());
        assertTrue(invitationLogEntry.isDeclined());
    }

    /**
     * Tests the markAsRead method for the InvitationLogEntry class
     */
    @Test
    public void testMarkAsRead() {
        invitationLogEntry = new InvitationLogEntry();
        invitationLogEntry.markAsRead();
        assertTrue(invitationLogEntry.isRead());
    }


}
