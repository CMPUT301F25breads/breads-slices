package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;
import com.example.slices.models.NotificationType;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the NotificationLogEntry class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testsNotificationLogEntry {
    private NotificationLogEntry notificationLogEntry;
    private Notification notification;
    private Timestamp timestamp;
    /**
     * Sets up the test by creating a NotificationLogEntry object
     */
    @Before
    public void setup() {
        timestamp = Timestamp.now();
        notification = new Notification("Test Title", "Test Body", 1, 2, 3);
        notification.setTimestamp(timestamp);
        notificationLogEntry = new NotificationLogEntry(notification, 1);
    }

    /**
     * Tests the constructor and getter methods for the NotificationLogEntry class
     */
    @Test
    public void testConstructor() {
        NotificationLogEntry defaultE = new NotificationLogEntry();
        assertEquals(LogType.NOTIFICATION, defaultE.getType());
        assertFalse(defaultE.isRead());
    }
    /**
     * Tests the constructor and getter methods for the NotificationLogEntry class
     */
    @Test
    public void testConstructorAndGetters() {
        assertEquals("Test Title Test Body", notificationLogEntry.getMessage());
        assertEquals(3, notificationLogEntry.getSenderId());
        assertEquals(2, notificationLogEntry.getRecipientId());
        assertEquals(1, notificationLogEntry.getNotificationId());
        assertEquals(LogType.NOTIFICATION, notificationLogEntry.getType());
        assertFalse(notificationLogEntry.isRead());
        assertEquals(timestamp, notificationLogEntry.getTimestamp());

    }

    /**
     * Tests set read and mark as read methods for the NotificationLogEntry class
     */
    @Test
    public void testSetRead() {
        notificationLogEntry.setRead(true);
        assertTrue(notificationLogEntry.isRead());
        notificationLogEntry.setRead(false);
        assertFalse(notificationLogEntry.isRead());
        notificationLogEntry.markAsRead();
        assertTrue(notificationLogEntry.isRead());
    }





}
