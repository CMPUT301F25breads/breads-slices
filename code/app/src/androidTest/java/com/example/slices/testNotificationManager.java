package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.example.slices.controllers.DBConnector;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogEntry;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the NotificationManager class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testNotificationManager {
    /**
     * The NotificationManager instance to test
     */
    private NotificationManager manager;
    /**
     * The DBConnector instance to use for testing
     */
    private DBConnector db;

    /**
     * Clears all notifications and logs from the database
     * @throws InterruptedException
     *      If the thread is interrupted while waiting for the latch to count down
     */
    private void clearAll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        db.clearNotifications(() -> latch.countDown());
        db.clearLogs(() -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }

    /**
     * Sets up the test by creating a new NotificationManager instance and a new DBConnector instance
     *
     */
    @Before
    public void setup() {
        manager = NotificationManager.getInstance();
        db = new DBConnector();
        try {
            clearAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Tests the sendNotification method of the NotificationManager class
     */
    @Test
    public void testSendNotification() {
        NotificationManager.sendNotification("Test", "This is a test notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getAllNotifications(new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        assertEquals(1, notifications.size());
                        assertEquals("Test", notifications.get(0).getTitle());
                        assertEquals("This is a test notification", notifications.get(0).getBody());
                        assertEquals(1, notifications.get(0).getSenderId());
                        assertEquals(2, notifications.get(0).getRecipientId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notifications");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to send notification");
            }


        });

    }

    /**
     * Tests the sendInvitation method of the NotificationManager class
     */
    @Test
    public void testSendInvitation() {
        NotificationManager.sendInvitation("Test", "This is a test invitation", 1, 2, 3, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Invitation sent successfully");
                db.getAllNotifications(new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        assertEquals(1, notifications.size());
                        assertEquals("Test", notifications.get(0).getTitle());
                        assertEquals("This is a test invitation", notifications.get(0).getBody());
                        assertEquals(1, notifications.get(0).getSenderId());
                        assertEquals(2, notifications.get(0).getRecipientId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notifications");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to send invitation");

            }
        });
    }

    /**
     * Tests the getInstance method of the NotificationManager class
     */
    @Test
    public void testSingletonInstance() {
        NotificationManager manager1 = NotificationManager.getInstance();
        NotificationManager manager2 = NotificationManager.getInstance();
        assertSame(manager1, manager2);
    }

    /**
     * Tests the logging of notifications
     */
    @Test
    public void testLogging(){
        NotificationManager.sendNotification("Test", "This is a test notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                //Sent successfully
                //Get the notificaiton logs
                db.getAllNotificationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> notifications) {
                        List<InvitationLogEntry> i = new ArrayList<>();
                        List<NotificationLogEntry> n = new ArrayList<>();

                        for (LogEntry log : notifications) {
                            if (log.getType() == LogType.INVITATION) {
                                i.add((InvitationLogEntry) log);
                            } else {
                                n.add((NotificationLogEntry) log);
                            }
                        }

                        assertEquals(0, i.size());
                        assertEquals(1, n.size());
                        assertEquals("Test This is a test notification", n.get(0).getMessage());
                        assertEquals(1, n.get(0).getSenderId());
                        assertEquals(2, n.get(0).getRecipientId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notifications");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to send notification");
            }
        });


    }

    /**
     * Tests the logging of invitations
     */
    @Test
    public void testLoggingInvitation(){
        NotificationManager.sendInvitation("Test", "This is a test invitation", 1, 2, 3, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                //Sent successfully
                //Get the notification logs
                db.getAllInvitationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> logs) {
                        List<InvitationLogEntry> i = new ArrayList<>();
                        List<NotificationLogEntry> n = new ArrayList<>();

                        for (LogEntry log : logs) {
                            if (log.getType() == LogType.INVITATION) {
                                i.add((InvitationLogEntry) log);
                            } else {
                                n.add((NotificationLogEntry) log);
                            }

                        }

                        assertEquals(1, i.size());
                        assertEquals(0, n.size());
                        assertEquals("Test This is a test invitation", i.get(0).getMessage());
                        assertEquals(1, i.get(0).getSenderId());
                        assertEquals(2, i.get(0).getRecipientId());
                        assertEquals(3, i.get(0).getEventId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notifications");
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to send notification");
            }
        });
    }

}
