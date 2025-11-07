package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.example.slices.controllers.DBConnector;
import com.example.slices.controllers.Logger;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.models.LogEntry;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the Logger class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testLogger {
    /**
     * The Logger instance to test
     */
    private Logger logger;
    /**
     * The DBConnector instance to use for testing
     */
    private DBConnector db;

    /**
     * Sets up the test by creating a new Logger instance and a new DBConnector instance
     */
    @Before
    public void setup() {
        logger = Logger.getInstance();
        db = new DBConnector();
    }

    /**
     * Clears all logs from the database
     * @throws InterruptedException
     *      If the thread is interrupted while waiting for the latch to count down
     */
    private void clearAll() throws InterruptedException {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(4);
        db.clearEntrants(() -> latch.countDown());
        db.clearEvents(() -> latch.countDown());
        db.clearNotifications(() -> latch.countDown());
        db.clearLogs(() -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }

    /**
     * Tests the log method of the Logger class
     * @throws InterruptedException
     *      If the thread is interrupted while waiting for the latch to count down
     */
    @Test
    public void testLog() throws InterruptedException {
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        Logger.log(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Notification logged successfully");
                db.getAllNotificationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> notifications) {
                        NotificationLogEntry logEntry = (NotificationLogEntry) notifications.get(0);
                        assertEquals(1, notifications.size());
                        assertEquals("Test Notification Test Message", notifications.get(0).getMessage());
                        assertEquals(3, logEntry.getSenderId());
                        assertEquals(2, logEntry.getRecipientId());
                        assertEquals(1, logEntry.getNotificationId());
                        assertEquals(false, logEntry.isRead());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notifications");
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to log notification");
            }
        });

    }

    /**
     * Tests the getInstance method of the Logger class
     */
    @Test
    public void testSingletonInstance() {
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();
        assertEquals(logger1, logger2);
    }

}
