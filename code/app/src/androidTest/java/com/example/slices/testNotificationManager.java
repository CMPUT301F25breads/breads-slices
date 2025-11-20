package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.interfaces.NotificationCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogEntry;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;
import com.example.slices.testing.DebugLogger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for the NotificationManager class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testNotificationManager {

    // ---------- SETUP AND TEARDOWN ----------

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        NotificationManager.setTesting(true);

        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);

        latch.await(15, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);

        latch.await(15, TimeUnit.SECONDS);

        EntrantController.setTesting(false);
        EventController.setTesting(false);
        Logger.setTesting(false);
        NotificationManager.setTesting(false);
    }

    private void clearAll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.clearNotifications(latch::countDown);
        latch.await(15, TimeUnit.SECONDS);
    }

    // ---------- HELPER FOR ASYNC TESTS ----------

    private void runAsyncTest(RunnableWithCallback testLogic) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> failure = new AtomicReference<>();

        testLogic.run(e -> {
            if (e != null) failure.set(e);
            latch.countDown();
        });

        latch.await(15, TimeUnit.SECONDS);
        if (failure.get() != null) throw failure.get();
    }

    interface RunnableWithCallback {
        interface Callback {
            void done(AssertionError e);
        }
        void run(Callback callback);
    }

    // ---------- NOTIFICATION TESTS ----------

    @Test
    public void testSendNotification() throws InterruptedException {
        clearAll();
        runAsyncTest(cb -> NotificationManager.sendNotification("Test", "This is a test notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.getAllNotifications(new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        try {
                            assertEquals(1, notifications.size());
                            Notification n = notifications.get(0);
                            assertEquals("Test", n.getTitle());
                            assertEquals("This is a test notification", n.getBody());
                            assertEquals(1, n.getSenderId());
                            assertEquals(2, n.getRecipientId());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get notifications"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to send notification"));
            }
        }));
    }

    @Test
    public void testSendInvitation() throws InterruptedException {
        clearAll();
        runAsyncTest(cb -> NotificationManager.sendInvitation("Test", "This is a test invitation", 1, 2, 3, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.getAllNotifications(new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        try {
                            assertEquals(1, notifications.size());
                            Notification n = notifications.get(0);
                            assertEquals("Test", n.getTitle());
                            assertEquals("This is a test invitation", n.getBody());
                            assertEquals(1, n.getSenderId());
                            assertEquals(2, n.getRecipientId());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get notifications"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to send invitation"));
            }
        }));
    }

    @Test
    public void testSingletonInstance() {
        NotificationManager manager1 = NotificationManager.getInstance();
        NotificationManager manager2 = NotificationManager.getInstance();
        assertSame(manager1, manager2);
    }

    @Test
    public void testLoggingNotification() throws InterruptedException {
        clearAll();
        runAsyncTest(cb -> NotificationManager.sendNotification("Test", "This is a test notification", 1, 2, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.getAllNotificationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> logs) {
                        try {
                            List<NotificationLogEntry> n = logs.stream()
                                    .filter(l -> l.getType() == LogType.NOTIFICATION)
                                    .map(l -> (NotificationLogEntry) l)
                                    .toList();

                            assertEquals(1, n.size());
                            NotificationLogEntry log = n.get(0);
                            assertEquals("Test This is a test notification", log.getMessage());
                            assertEquals(1, log.getSenderId());
                            assertEquals(2, log.getRecipientId());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get notification logs"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to send notification"));
            }
        }));
    }

    @Test
    public void testLoggingInvitation() throws InterruptedException {
        clearAll();
        runAsyncTest(cb -> NotificationManager.sendInvitation("Test", "This is a test invitation", 1, 2, 3, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.getAllInvitationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> logs) {
                        try {
                            List<InvitationLogEntry> i = logs.stream()
                                    .filter(l -> l.getType() == LogType.INVITATION)
                                    .map(l -> (InvitationLogEntry) l)
                                    .toList();

                            assertEquals(1, i.size());
                            InvitationLogEntry log = i.get(0);
                            assertEquals("Test This is a test invitation", log.getMessage());
                            assertEquals(1, log.getSenderId());
                            assertEquals(2, log.getRecipientId());
                            assertEquals(3, log.getEventId());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get invitation logs"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to send invitation"));
            }
        }));
    }

    // ---------- GENERIC NOTIFICATION CRUD ----------

    @Test
    public void testWriteAndGetNotification() throws InterruptedException {
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);

        runAsyncTest(cb -> NotificationManager.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.getNotificationById(notification.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        try {
                            assertEquals("Test Message", result.getBody());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get notification"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write notification"));
            }
        }));
    }

    @Test
    public void testUpdateNotification() throws InterruptedException {
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        notification.setBody("Updated");

        runAsyncTest(cb -> NotificationManager.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.updateNotification(notification, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.getNotificationById(notification.getId(), new NotificationCallback() {
                            @Override
                            public void onSuccess(Notification result) {
                                try {
                                    assertEquals("Updated", result.getBody());
                                } catch (AssertionError e) {
                                    cb.done(e);
                                    return;
                                }
                                cb.done(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to get updated notification"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to update notification"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write notification for update"));
            }
        }));
    }

    @Test
    public void testDeleteNotification() throws InterruptedException {
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);

        runAsyncTest(cb -> NotificationManager.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.deleteNotification(String.valueOf(notification.getId()));
                NotificationManager.getNotificationById(notification.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        cb.done(new AssertionError("Notification should have been deleted"));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(null); // expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write notification for delete"));
            }
        }));
    }

    // ---------- GET BY RECIPIENT / SENDER ----------

    @Test
    public void testGetNotificationByRecipientId() throws InterruptedException {
        clearAll();
        Notification n1 = new Notification("N1", "Msg1", 1, 2, 3);
        Notification n2 = new Notification("N2", "Msg2", 2, 2, 3);
        Notification n3 = new Notification("N3", "Msg3", 3, 4, 3);

        runAsyncTest(cb -> NotificationManager.writeNotification(n1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.writeNotification(n2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.writeNotification(n3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                NotificationManager.getNotificationByRecipientId(2, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        try {
                                            assertEquals(2, result.size());
                                            assertTrue(result.contains(n1));
                                            assertTrue(result.contains(n2));
                                            assertFalse(result.contains(n3));
                                        } catch (AssertionError e) {
                                            cb.done(e);
                                            return;
                                        }
                                        cb.done(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.done(new AssertionError("Failed to get notifications by recipient ID"));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to write notification"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to write notification"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write notification"));
            }
        }));
    }

    @Test
    public void testGetNotificationsBySenderId() throws InterruptedException {
        clearAll();
        Notification n1 = new Notification("N1", "Msg1", 1, 2, 3);
        Notification n2 = new Notification("N2", "Msg2", 2, 2, 3);
        Notification n3 = new Notification("N3", "Msg3", 3, 2, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(n1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.writeNotification(n2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.writeNotification(n3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                NotificationManager.getNotificationsBySenderId(3, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        try {
                                            assertEquals(2, result.size());
                                            assertTrue(result.contains(n1));
                                            assertTrue(result.contains(n2));
                                            assertFalse(result.contains(n3));
                                        } catch (AssertionError e) {
                                            cb.done(e);
                                            return;
                                        }
                                        cb.done(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.done(new AssertionError("Failed to get notifications by sender ID"));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to write notification"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to write notification"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write notification"));
            }
        }));
    }

    // ---------- INVITATION TESTS (CRUD & GET) ----------

    @Test
    public void testWriteAndGetInvitation() throws InterruptedException {
        clearAll();
        Invitation inv = new Invitation("Invite", "Msg", 1, 2, 3, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(inv, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.getInvitationById(inv.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        try {
                            assertEquals(inv.getEventId(), ((Invitation) result).getEventId());
                        } catch (AssertionError e) {
                            cb.done(e);
                            return;
                        }
                        cb.done(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to get invitation"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write invitation"));
            }
        }));
    }

    @Test
    public void testUpdateInvitation() throws InterruptedException {
        clearAll();
        Invitation inv = new Invitation("Invite", "Msg", 1, 2, 3, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(inv, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                inv.setBody("Updated");
                NotificationManager.updateInvitation(inv, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.getInvitationById(inv.getId(), new NotificationCallback() {
                            @Override
                            public void onSuccess(Notification result) {
                                try {
                                    assertEquals("Updated", ((Invitation) result).getBody());
                                } catch (AssertionError e) {
                                    cb.done(e);
                                    return;
                                }
                                cb.done(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to get updated invitation"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to update invitation"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write invitation for update"));
            }
        }));
    }

    @Test
    public void testGetInvitationByRecipientId() throws InterruptedException {
        clearAll();
        Invitation inv1 = new Invitation("I1", "M1", 1, 2, 3, 4);
        Invitation inv2 = new Invitation("I2", "M2", 2, 2, 3, 4);
        Invitation inv3 = new Invitation("I3", "M3", 3, 3, 4, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(inv1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.writeNotification(inv2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.writeNotification(inv3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                NotificationManager.getInvitationByRecipientId(2, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        try {
                                            assertEquals(2, result.size());
                                            assertTrue(result.contains(inv1));
                                            assertTrue(result.contains(inv2));
                                            assertFalse(result.contains(inv3));
                                        } catch (AssertionError e) {
                                            cb.done(e);
                                            return;
                                        }
                                        cb.done(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.done(new AssertionError("Failed to get invitations by recipient ID"));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to write invitation"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to write invitation"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write invitation"));
            }
        }));
    }

    @Test
    public void testGetInvitationBySenderId() throws InterruptedException {
        clearAll();
        Invitation inv1 = new Invitation("I1", "M1", 1, 2, 3, 4);
        Invitation inv2 = new Invitation("I2", "M2", 2, 3, 3, 4);
        Invitation inv3 = new Invitation("I3", "M3", 3, 2, 4, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(inv1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.writeNotification(inv2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.writeNotification(inv3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                NotificationManager.getInvitationBySenderId(2, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        try {
                                            assertEquals(2, result.size());
                                            assertTrue(result.contains(inv1));
                                            assertTrue(result.contains(inv3));
                                            assertFalse(result.contains(inv2));
                                        } catch (AssertionError e) {
                                            cb.done(e);
                                            return;
                                        }
                                        cb.done(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.done(new AssertionError("Failed to get invitations by sender ID"));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to write invitation"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to write invitation"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write invitation"));
            }
        }));
    }

    @Test
    public void testGetInvitationByEventId() throws InterruptedException {
        clearAll();
        Invitation inv1 = new Invitation("I1", "M1", 1, 2, 10, 4);
        Invitation inv2 = new Invitation("I2", "M2", 2, 3, 10, 4);
        Invitation inv3 = new Invitation("I3", "M3", 3, 2, 11, 4);

        runAsyncTest(cb -> NotificationManager.writeNotification(inv1, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                NotificationManager.writeNotification(inv2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.writeNotification(inv3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                NotificationManager.getInvitationByEventId(10, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        try {
                                            assertEquals(2, result.size());
                                            assertTrue(result.contains(inv1));
                                            assertTrue(result.contains(inv2));
                                            assertFalse(result.contains(inv3));
                                        } catch (AssertionError e) {
                                            cb.done(e);
                                            return;
                                        }
                                        cb.done(null);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        cb.done(new AssertionError("Failed to get invitations by event ID"));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                cb.done(new AssertionError("Failed to write invitation"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.done(new AssertionError("Failed to write invitation"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.done(new AssertionError("Failed to write invitation"));
            }
        }));
    }
}




