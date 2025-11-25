package com.example.slices.controllertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.models.LogEntry;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the Logger class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class LoggerTest {

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        Logger.setMode(false);
        NotificationManager.setTesting(true);
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", success);
    }


    @AfterClass
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean success = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", success);
        //Revert out of testing mode
        EntrantController.setTesting(false);
        EventController.setTesting(false);
        Logger.setTesting(false);
        NotificationManager.setTesting(false);
    }

    /**
     * Await a latch to complete
     * @param latch
     *      Latch to wait for
     */
    private void await(CountDownLatch latch) {
        try {
            boolean ok = latch.await(20, TimeUnit.SECONDS);
            assertTrue("Timed out waiting for async operation", ok);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }


    /**
     * Clears all logs from the database
     *
     */
    private void clearAll() {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        await(latch);
    }

    /**
     * Tests the logAction method of the Logger class
     * Pass if the log is logged successfully
     * Fail if the log is not logged successfully
     */
    @Test
    public void testLogAction() {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        Notification notification = new Notification("Test Notification", "Test Message", "1", 2, 3);
        Logger.logNotification("Test Notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Notification logged successfully");
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to log notification");
                latch.countDown();
            }
        });
        await(latch);
        CountDownLatch latch2 = new CountDownLatch(1);
        Logger.getLogsOfType(LogType.NOTIFICATION_SENT, new LogListCallback() {
            @Override
            public void onSuccess(List<LogEntry> notifications) {
                assertEquals(1, notifications.size());
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get notifications");
                latch2.countDown();
            }
        });
        await(latch2);



    }


    /**
     * Tests the deleteLog method of the Logger class
     * Pass if the log is deleted successfully
     * Fail if the log is not deleted successfully
     */
    @Test
    public void testDeleteLog() {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        Notification notification = new Notification("Test Notification", "Test Message", "1", 2, 3);
        Logger.logNotification("Test Notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Notification logged successfully");
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to log notification");
                latch.countDown();
            }
        });
        await(latch);
        //now get the log entry
        CountDownLatch latch2 = new CountDownLatch(1);
        Logger.getLogsOfType(LogType.NOTIFICATION_SENT, new LogListCallback() {
            @Override
            public void onSuccess(List<LogEntry> notifications) {
                assertEquals(1, notifications.size());
                Logger.deleteLog(notifications.get(0).getLogId(), new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Notification deleted successfully");
                        latch2.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to delete notification");
                        latch2.countDown();

                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get notifications");
                latch2.countDown();
            }
        });
        await(latch2);
    }




}
