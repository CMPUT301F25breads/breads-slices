package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;


import android.util.Log;

import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.interfaces.InvitationCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.interfaces.NotificationCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Event;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogEntry;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;
import com.example.slices.testing.DebugLogger;
import com.example.slices.testing.TestUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Ignore
public class testsDBConnector {

    private DBConnector db;




    @Before
    public void setup() throws InterruptedException {
        db = new DBConnector();
        //Create a notification
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Log.d("TestsDBConnector", "Notification created");
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create notification" + e.getMessage());
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Create a log
        NotificationLogEntry log = new NotificationLogEntry(notification, 1);
        db.writeLog(log, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Log.d("TestsDBConnector", "Log created");
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create log" + e.getMessage());
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void clearAll() throws InterruptedException {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(3);
        db.clearEntrants(() -> latch.countDown());
        db.clearEvents(() -> latch.countDown());
        db.clearNotifications(() -> latch.countDown());
        db.clearLogs(() -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }

    // ---------- ENTRANT TESTS ----------

    @Test
    public void testWriteAndGetEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEntrant(entrant.getId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        assertEquals(entrant.getName(), result.getName());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrant");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetEntrantByDeviceId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        entrant.setDeviceId("testDeviceId");
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrantDeviceId(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEntrantByDeviceId(entrant.getDeviceId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        assertEquals(entrant.getDeviceId(), result.getDeviceId());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrant by device ID");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant device ID");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetNewEntrantId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int newId) {
                assertTrue(newId >= 1);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new entrant ID");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testUpdateEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                entrant.setName("Updated Name");
                db.updateEntrant(entrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getEntrant(entrant.getId(), new EntrantCallback() {
                            @Override
                            public void onSuccess(Entrant result) {
                                assertEquals("Updated Name", result.getName());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated entrant");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update entrant");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.deleteEntrant(String.valueOf(entrant.getId()));
                db.getEntrant(entrant.getId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        fail("Entrant should have been deleted");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown(); // Expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant for delete test");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    // ---------- EVENT TESTS ----------

    @Test
    public void testWriteAndGetEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        assertEquals(event.getName(), result.getName());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testUpdateEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        event.setName("Updated Event");
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.updateEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getEvent(event.getId(), new EventCallback() {
                            @Override
                            public void onSuccess(Event result) {
                                assertEquals("Updated Event", result.getName());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated event");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event for update");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.deleteEvent(String.valueOf(event.getId()));
                db.getEvent(event.getId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event result) {
                        fail("Event should have been deleted");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown(); // Expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event for delete");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetEntrantsForEvent() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event event = TestUtils.createTestEvents(1, 10, 3).get(0);
        List<Entrant> entrants = TestUtils.createTestEntrants(3, 10);
        event.setEntrants(entrants);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
                    @Override
                    public void onSuccess(List<Entrant> result) {
                        assertEquals(3, result.size());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrants for event");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write event with entrants");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetAllFutureEvents() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Event eventG = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);
        //Set up event that is not in the future
        Calendar cal = Calendar.getInstance();
        cal.set(2024, 11, 12, 15, 0, 0);
        Date date = cal.getTime();
        Timestamp eventDate = new Timestamp(date);
        cal.set(2024, 11, 12, 13, 0, 0);
        Date date2 = cal.getTime();
        Timestamp regDeadline = new Timestamp(date2);

        Event x = new Event("Test Event", "Test Description", "Test Location", eventDate, regDeadline, 10, true, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                assertNotNull(event);

                db.getAllFutureEvents(new EventListCallback() {
                    @Override
                    public void onSuccess(List<Event> result) {
                        assertFalse(result.isEmpty());
                        assertTrue(result.contains(eventG));
                        assertFalse(result.contains(event));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get all future events");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to create event");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }


    // ---------- NOTIFICATION TESTS ----------


    @Test
    public void testGetNotificationId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        db.getNotificationId(new NotificationIDCallback() {
            @Override
            public void onSuccess(int newId) {
                assertTrue(newId >= 1);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new notification ID");
            }
        });
    }

    @Test
    public void testGetAllNotifications() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        Notification notification2 = new Notification("Test Notification2", "Test Message2", 2, 2, 3);
        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(notification2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getAllNotifications(new NotificationListCallback() {
                            @Override
                            public void onSuccess(List<Notification> result) {
                                assertTrue(result.contains(notification));
                                assertTrue(result.contains(notification2));
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get all notifications");
                            }

                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write notification");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification");
            }
        });


    }


    @Test
    public void testWriteAndGetNotification() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);

        CountDownLatch latch = new CountDownLatch(1);

        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getNotificationById(notification.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        assertEquals("Test Message", result.getBody());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get notification");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testUpdateNotification() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        notification.setBody("Updated");
        CountDownLatch latch = new CountDownLatch(1);

        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                notification.setBody("Updated");
                db.updateNotification(notification, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getNotificationById(notification.getId(), new NotificationCallback() {
                            @Override
                            public void onSuccess(Notification result) {
                                assertEquals("Updated", result.getBody());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated notification");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update notification");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification for update");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteNotification() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.deleteNotification(String.valueOf(notification.getId()));
                db.getNotificationById(notification.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        fail("Notification should have been deleted");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown(); // Expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification for delete");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetNotificationByRecipientId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        Notification notification2 = new Notification("Test Notification2", "Test Message2", 2, 2, 3);
        Notification notification3 = new Notification("Test Notification3", "Test Message3", 3, 4, 3);


        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(notification2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.writeNotification(notification3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                db.getNotificationByRecipientId(2, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        assertEquals(2, result.size());
                                        assertTrue(result.contains(notification2));
                                        assertTrue(result.contains(notification));
                                        assertFalse(result.contains(notification3));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get notifications by recipient ID");
                                    }

                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to write notification");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write notification");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification");
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetNotificationsBySenderId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        Notification notification2 = new Notification("Test Notification2", "Test Message2", 2, 2, 3);
        Notification notification3 = new Notification("Test Notification3", "Test Message3", 3, 2, 4);
        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(notification2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.writeNotification(notification3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                db.getNotificationsBySenderId(3, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        assertEquals(2, result.size());
                                        assertTrue(result.contains(notification2));
                                        assertTrue(result.contains(notification));
                                        assertFalse(result.contains(notification3));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get notifications by recipient ID");
                                    }

                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to write notification");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write notification");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write notification");
            }
        });

        latch.await(10, TimeUnit.SECONDS);
    }


    // ---------- INVITATION TESTS ----------

    @Test
    public void testGetInvitationById() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        DebugLogger.d("Invitation", "Test Invitation");
        CountDownLatch latch = new CountDownLatch(1);

        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getInvitationById(invitation.getId(), new NotificationCallback() {
                    @Override
                    public void onSuccess(Notification result) {
                        assertEquals(invitation.getEventId(), ((Invitation) result).getEventId());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get invitation");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write invitation");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetInvitationByRecipientId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        Invitation invitation2 = new Invitation("Test Invitation2", "Test Message2", 2, 2, 3, 4);
        Invitation invitation3 = new Invitation("Test Invitation3", "Test Message3", 3, 3, 4, 4);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(invitation2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.writeNotification(invitation3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                db.getInvitationByRecipientId(2, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        assertEquals(2, result.size());
                                        assertTrue(result.contains(invitation2));
                                        assertTrue(result.contains(invitation));
                                        assertFalse(result.contains(invitation3));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get invitations by recipient ID");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to write invitation");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write invitation");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write invitation");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }


    @Test
    public void testGetInvitationBySenderId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        Invitation invitation2 = new Invitation("Test Invitation2", "Test Message2", 2, 2, 3, 4);
        Invitation invitation3 = new Invitation("Test Invitation3", "Test Message3", 3, 3, 4, 4);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(invitation2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.writeNotification(invitation3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                db.getInvitationBySenderId(3, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        assertEquals(2, result.size());
                                        assertTrue(result.contains(invitation2));
                                        assertTrue(result.contains(invitation));
                                        assertFalse(result.contains(invitation3));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get invitations by sender ID");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to write invitation");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write invitation");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write invitation");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetInvitationByEventId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        Invitation invitation2 = new Invitation("Test Invitation2", "Test Message2", 2, 2, 3, 4);
        Invitation invitation3 = new Invitation("Test Invitation3", "Test Message3", 3, 3, 4, 5);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.writeNotification(invitation2, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.writeNotification(invitation3, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                db.getInvitationByEventId(4, new NotificationListCallback() {
                                    @Override
                                    public void onSuccess(List<Notification> result) {
                                        assertEquals(2, result.size());
                                        assertTrue(result.contains(invitation2));
                                        assertTrue(result.contains(invitation));
                                        assertFalse(result.contains(invitation3));
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        fail("Failed to get invitations by event ID");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to write invitation");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write invitation");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write invitation");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testUpdateInvitation() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                invitation.setBody("Updated");
                db.updateInvitation(invitation, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getInvitationById(invitation.getId(), new NotificationCallback() {
                            @Override
                            public void onSuccess(Notification result) {
                                Invitation i = (Invitation) result;
                                assertEquals("Updated", i.getBody());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated invitation");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update invitation");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write invitation for update");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }


    // ---------- LOG TESTS ----------

    @Test
    public void testWriteAndGetNotificationLog() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);

        NotificationLogEntry log = new NotificationLogEntry(notification, 1);

        // Create a latch to wait for the write)
        CountDownLatch latch = new CountDownLatch(1);

        db.writeLog(log, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getAllNotificationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> result) {
                        assertTrue(result.stream().anyMatch(l -> l.getMessage().equals("Test Notification Test Message")));
                        latch.countDown();
                    }


                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get logs");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write log");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteLog() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Notification notification = new Notification("Test Notification", "Test Message", 1, 2, 3);
        NotificationLogEntry notificationLogEntry = new NotificationLogEntry(notification, 1);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeLog(notificationLogEntry, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.deleteLog(String.valueOf(notificationLogEntry.getLogId()));
                db.getAllNotificationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> logs) {
                        assertFalse(logs.contains(notificationLogEntry));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get logs");
                    }

                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write log");
            }
        });
    }

    @Test
    public void testGetAllInvitationLogs() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Invitation invitation = new Invitation("Test Invitation", "Test Message", 1, 2, 3, 4);
        InvitationLogEntry log = new InvitationLogEntry(invitation, 1);
        CountDownLatch latch = new CountDownLatch(1);
        db.writeLog(log, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getAllInvitationLogs(new LogListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> result) {
                        assertTrue(result.stream().anyMatch(l -> l.getMessage().equals("Test Invitation Test Message")));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get logs");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write log");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }
}


