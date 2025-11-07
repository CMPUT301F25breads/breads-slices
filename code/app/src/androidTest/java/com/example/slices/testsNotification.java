package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Notification class
 * @author Ryan Haubrich
 * @version 1.0
 */

public class testsNotification {
    private Notification notification;
    private Timestamp timestamp;

    /**
     * Sets up the test by creating a Notification object
     */
    @Before
    public void setup() {
        timestamp = Timestamp.now();
        notification = new Notification("Test Title", "Test Body", 1, 2, 3);
        notification.setTimestamp(timestamp);
        notification.setEventId(4);
        notification.setType(NotificationType.NOTIFICATION);
    }

    /**
     * Tests the constructor and getter methods for the Notification class
     */
    @Test
    public void testConstructorAndGetters() {
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Body", notification.getBody());
        assertEquals(1, notification.getId());
        assertEquals(2, notification.getRecipientId());
        assertEquals(3, notification.getSenderId());
        assertEquals(timestamp, notification.getTimestamp());
        assertEquals(NotificationType.NOTIFICATION, notification.getType());
        assertEquals(4, notification.getEventId());
        assertFalse(notification.getRead());
    }

    /**
     * Tests the setter methods for the Notification class
     */
    @Test
    public void testSetters() {

        Timestamp newTimestamp = Timestamp.now();
        notification.setTitle("New Title");
        notification.setBody("New Body");
        notification.setId(5);
        notification.setRecipientId(6);
        notification.setSenderId(7);
        notification.setRead(true);
        notification.setEventId(8);
        notification.setType(NotificationType.INVITATION);
        notification.setTimestamp(newTimestamp);

        assertEquals("New Title", notification.getTitle());
        assertEquals("New Body", notification.getBody());
        assertEquals(5, notification.getId());
        assertEquals(6, notification.getRecipientId());
        assertEquals(7, notification.getSenderId());
        assertEquals(newTimestamp, notification.getTimestamp());
        assertEquals(NotificationType.INVITATION, notification.getType());
        assertEquals(8, notification.getEventId());
        assertTrue(notification.getRead());
    }

    /**
     * Tests the equals method for the Notification class
     */
    @Test
    public void testEquality() {
        Notification otherNotification = new Notification("Test Title", "Test Body", 1, 2, 3);
        otherNotification.setTimestamp(timestamp);
        otherNotification.setEventId(4);
        otherNotification.setType(NotificationType.NOTIFICATION);

        assertEquals(notification, otherNotification);
        assertEquals(notification.hashCode(), otherNotification.hashCode());

        otherNotification.setId(5);
        assertFalse(notification.equals(otherNotification));
        assertFalse(notification.hashCode() == otherNotification.hashCode());

        assertFalse(notification.equals(null));
        assertFalse(notification.equals(new Object()));
    }





}
