package com.example.slices.modeltests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;

public class NotificationTest {

    /**
     * Tests default constructor initializes defaults
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     */
    @Test
    public void testDefaultConstructor() {
        Notification n = new Notification();
        assertNull(n.getId());
        assertNull(n.getTitle());
        assertNull(n.getBody());
        assertEquals(0, n.getRecipientId());
        assertEquals(0, n.getSenderId());
        assertEquals(0, n.getEventId());
        assertFalse(n.getRead());
        assertNull(n.getTimestamp());
        assertNull(n.getType());
    }

    /**
     * Tests full constructor initializes
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        Notification n = new Notification("Hello", "World", "abc123", 10, 20);
        assertEquals("abc123", n.getId());
        assertEquals("Hello", n.getTitle());
        assertEquals("World", n.getBody());
        assertEquals(10, n.getRecipientId());
        assertEquals(20, n.getSenderId());
        assertFalse(n.getRead());
        assertNotNull(n.getTimestamp());
        assertEquals(NotificationType.NOTIFICATION, n.getType());
    }

    /**
     * Tests timestamp is generated at construction
     * Pass if timestamp is not null
     * Fail otherwise
     */
    @Test
    public void testTimestampGenerated() {
        Notification n = new Notification("A", "B", "id1", 1, 2);
        assertNotNull(n.getTimestamp());
    }

    /**
     * Tests type is NOTIFICATION for default constructor
     * Pass if type is NOTIFICATION
     * Fail otherwise
     */
    @Test
    public void testTypeIsNotification() {
        Notification n = new Notification("T", "B", "id2", 5, 6);

        assertEquals(NotificationType.NOTIFICATION, n.getType());
    }

    /**
     * Tests equals
     * Pass if the notifications are equal
     * Fail otherwise
     */
    @Test
    public void testEqualsSameId() {
        Notification n1 = new Notification("A", "B", "idX", 1, 2);
        Notification n2 = new Notification("C", "D", "idX", 9, 9);

        assertEquals(n1, n2);
        assertEquals(n2, n1);
    }

    /**
     * Tests equals
     * Pass if the notifications are not equal
     * Fail otherwise
     */
    @Test
    public void testEqualsDifferentId() {
        Notification n1 = new Notification("A", "B", "id1", 1, 2);
        Notification n2 = new Notification("A", "B", "id2", 1, 2);

        assertNotEquals(n1, n2);
    }



}
