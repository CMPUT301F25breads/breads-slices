package com.example.slices.modeltests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;

public class NotificationLogEntryTest {

    /**
     * Tests default constructor initializes
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        NotificationLogEntry log = new NotificationLogEntry();

        assertEquals(LogType.NOTIFICATION, log.getType());
        assertFalse(log.isRead());
        assertNull(log.getMessage());
        assertNull(log.getTimestamp());
        assertNull(log.getLogId());
        assertNull(log.getNotificationId());
        assertEquals(0, log.getSenderId());
        assertEquals(0, log.getRecipientId());
    }

    /**
     * Tests full constructor copies all fields from Notification
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testConstructorFromNotification() {
        Notification n = new Notification("Hello", "World", "notif123", 88, 44);
        n.setRead(true);
        NotificationLogEntry log = new NotificationLogEntry(n, "log789");
        assertEquals("Hello World", log.getMessage());
        assertEquals(n.getTimestamp(), log.getTimestamp());
        assertEquals("notif123", log.getNotificationId());
        assertEquals(44, log.getSenderId());
        assertEquals(88, log.getRecipientId());
        assertTrue(log.isRead());
        assertEquals("log789", log.getLogId());
        assertEquals(LogType.NOTIFICATION, log.getType());
    }

    /**
     * Tests that unread notifications remain unread in the log
     * Pass if the log is not marked as read
     * Fail otherwise
     */
    @Test
    public void testUnreadNotificationPropagation() {
        Notification n = new Notification("A", "B", "id1", 1, 2);
        assertFalse(n.getRead());
        NotificationLogEntry log = new NotificationLogEntry(n, "log1");
        assertFalse(log.isRead());
    }

    /**
     * Tests markAsRead sets read = true
     * Pass if the log is marked as read
     * Fail otherwise
     */
    @Test
    public void testMarkAsRead() {
        NotificationLogEntry log = new NotificationLogEntry();
        assertFalse(log.isRead());
        log.markAsRead();
        assertTrue(log.isRead());
    }
}
