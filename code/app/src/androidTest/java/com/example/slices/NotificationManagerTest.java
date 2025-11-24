package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.exceptions.NotificationNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.NotificationCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
/**
 * Tests for the NotificationManager class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class NotificationManagerTest {

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        //Chuck it in testing mode
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        NotificationManager.setTesting(true);

        //Clean it out
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Global setup timed out", completed);
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Global teardown timed out", completed);

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
     * Clear all related collections for tests
     */
    private void clearAll()  {
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        await(latch);
    }

    /**
     * Helper to create an entrant
     * @param name
     *      Entrant name
     * @return
     *      Entrant created
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private Entrant createEntrant(String name) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Entrant> ref = new AtomicReference<>();

        EntrantController.createEntrant(
                name, name + "@mail.com", "123",
                new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        ref.set(entrant);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create entrant: " + e.getMessage());
                    }
                });

        await(latch);
        return ref.get();
    }

    /**
     * Create a valid event (uses EventController helper for future times)
     * @return
     *      Event
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private Event createValidEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();

        List<Timestamp> times = EventController.getTestEventTimes();
        Timestamp regStart = times.get(0);
        Timestamp regEnd = times.get(1);
        Timestamp eventDate = times.get(2);

        EventController.createEvent("Event", "Desc", "Loc", "Guide", "Img",
                eventDate, regStart, regEnd, 10, 5, false, "none", 123,
                new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        ref.set(event);
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create event: " + e.getMessage());
                    }
                });

        await(latch);
        return ref.get();
    }


    /**
     * Helper to get all notifications for a recipient
     * @param recipientId
     *      Recipient ID
     * @return
     *      List of notifications
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private List<Notification> getNotificationsForRecipient(int recipientId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Notification>> ref = new AtomicReference<>(new ArrayList<>());

        NotificationManager.getNotificationsByRecipientId(recipientId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                ref.set(notifications);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        await(latch);
        return ref.get();
    }

    /**
     * Helper to get all notifications for a sender
     * @param senderId
     *      Sender ID
     * @return
     *      List of notifications
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private List<Notification> getNotificationsForSender(int senderId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Notification>> ref = new AtomicReference<>(new ArrayList<>());

        NotificationManager.getNotificationsBySenderId(senderId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                ref.set(notifications);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        await(latch);
        return ref.get();
    }

    /**
     * Helper to get all invitations for a recipient
     * @param recipientId
     *      Recipient ID
     * @return
     *      List of invitations
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private List<Invitation> getInvitationsForRecipient(int recipientId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Invitation>> ref = new AtomicReference<>(new ArrayList<>());

        NotificationManager.getInvitationByRecipientId(recipientId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                List<Invitation> list = new ArrayList<>();
                for (Notification n : notifications) {
                    list.add((Invitation)n);
                }
                ref.set(list);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        await(latch);
        return ref.get();
    }

    /**
     * Helper to get all invitations for an event
     * @param eventId
     *      Event ID
     * @return
     *      List of invitations
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private List<Invitation> getInvitationsForEvent(int eventId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Invitation>> ref = new AtomicReference<>(new ArrayList<>());

        NotificationManager.getInvitationByEventId(eventId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                List<Invitation> list = new ArrayList<>();
                for (Notification n : notifications) {
                    list.add((Invitation)n);
                }
                ref.set(list);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        await(latch);
        return ref.get();
    }

    /**
     * Helper to get all invitations for a sender
     * @param senderId
     *      Sender ID
     * @return
     *      List of invitations
     * @throws InterruptedException
     *      If latch fails to complete
     */
    private List<Invitation> getInvitationsForSender(int senderId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Invitation>> ref = new AtomicReference<>(new ArrayList<>());
        NotificationManager.getInvitationBySenderId(senderId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                List<Invitation> list = new ArrayList<>();
                for (Notification n : notifications) {
                    list.add((Invitation)n);
                }
                ref.set(list);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        await(latch);
        return ref.get();
    }


    /**
     * Tests the singleton instance
     * Pass if the singleton is not null
     * Fail otherwise
     */
    @Test
    public void testGetInstanceNotNull() {
        NotificationManager mgr = NotificationManager.getInstance();
        assertNotNull(mgr);
    }


    /**
     * Tests the sendNotification method
     * Pass if the notification is written to the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testSendNotificationCreatesNotification() throws InterruptedException {
        clearAll();
        Entrant recipient = createEntrant("Rec1");
        Entrant sender = createEntrant("Sender1");
        CountDownLatch latch = new CountDownLatch(1);
        //Send notification
        NotificationManager.sendNotification("Hello", "World", recipient.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                //Now fetch and assert
                NotificationManager.getNotificationsByRecipientId(recipient.getId(), new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        assertEquals(1, notifications.size());
                        Notification n = notifications.get(0);
                        assertEquals("Hello", n.getTitle());
                        assertEquals("World", n.getBody());
                        assertEquals(recipient.getId(), n.getRecipientId());
                        assertEquals(sender.getId(), n.getSenderId());
                        assertEquals(NotificationType.NOTIFICATION, n.getType());
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to fetch notification: " + e.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("sendNotification failed: " + e.getMessage());
            }
        });
        await(latch);
    }

    /**
     * Tests the sendNotifications method on empty list
     * Pass if nothing is written to the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testSendNotificationsEmptyList() throws InterruptedException {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.sendNotifications("T", "B", new ArrayList<>(), 123, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Should not fail when recipients list is empty");
            }
        });

        await(latch);
        CountDownLatch check = new CountDownLatch(1);
        NotificationManager.getAllNotifications(new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                assertTrue(notifications.isEmpty());
                check.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("getAllNotifications failed");
            }
        });
        await(check);
    }

    /**
     * Tests the sendNotifications method on multiple recipients
     * Pass if all notifications are written to the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testSendNotificationsMultipleRecipients() throws InterruptedException {
        clearAll();

        Entrant r1 = createEntrant("R1");
        Entrant r2 = createEntrant("R2");
        Entrant sender = createEntrant("Sender2");

        List<Entrant> recips = new ArrayList<>();
        recips.add(r1);
        recips.add(r2);

        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.sendNotifications("Multi", "Body", recips, sender.getId(), new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        NotificationManager.getAllNotifications(new NotificationListCallback() {
                            @Override
                            public void onSuccess(List<Notification> notifications) {
                                assertEquals(2, notifications.size());
                                List<Integer> recipientIds = new ArrayList<>();

                                for (Notification n : notifications) {
                                    recipientIds.add(n.getRecipientId());
                                    assertEquals(sender.getId(), n.getSenderId());
                                }

                                assertTrue(recipientIds.contains(r1.getId()));
                                assertTrue(recipientIds.contains(r2.getId()));
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("getAllNotifications failed");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("sendNotifications failed: " + e.getMessage());
                    }
                });
        await(latch);
    }


    /**
     * Tests the getNotificationId method on empty database
     * Pass if the ID is 1
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetNotificationId() throws InterruptedException {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        //Get the id
        NotificationManager.getNotificationId(new NotificationIDCallback() {
            @Override
            public void onSuccess(int id) {
                ref.set(id);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("getNotificationId failed: " + e.getMessage());
            }
        });
        await(latch);
        assertEquals(1, (int)ref.get());
    }



    /**
     * Tests the getAllNotifications method on empty database
     * Pass if the list is empty
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */

    @Test
    public void testGetAllNotificationsEmpty() throws InterruptedException {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.getAllNotifications(new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                assertTrue(notifications.isEmpty());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("getAllNotifications failed");
            }
        });
        await(latch);
    }

    /**
     * Tests the getAllNotifications method on non-empty database
     * Pass if the list is not empty
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetAllNotificationsMultiple() throws InterruptedException {
        clearAll();
        Entrant r = createEntrant("R");
        Entrant s = createEntrant("S");
        CountDownLatch latch = new CountDownLatch(2);
        NotificationManager.sendNotification("T1", "B1", r.getId(), s.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("sendNotification failed");
            }
        });
        NotificationManager.sendNotification("T2", "B2", r.getId(), s.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        await(latch);
        CountDownLatch check = new CountDownLatch(1);

        NotificationManager.getAllNotifications(new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                assertEquals(2, notifications.size());
                check.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getAllNotifications failed");
            }
        });
        await(check);
    }


    /**
     * Tests the writeNotification method and updateNotification method
     * Pass if the notification is written to the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testWriteAndUpdateNotification() throws InterruptedException {
        clearAll();

        Notification n = new Notification("Title", "Body", "42", 1, 2);
        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.writeNotification(n, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("writeNotification failed");
            }
        });
        await(latch);
        CountDownLatch get = new CountDownLatch(1);
        NotificationManager.getNotificationById("42", new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                assertEquals("Title", notification.getTitle());
                assertEquals("Body", notification.getBody());
                get.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("getNotificationById failed");
            }
        });
        await(get);

        n.setTitle("NewTitle");
        n.setBody("NewBody");
        CountDownLatch upd = new CountDownLatch(1);
        NotificationManager.updateNotification(n, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                upd.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("updateNotification failed");
            }
        });
        await(upd);

        CountDownLatch check = new CountDownLatch(1);
        NotificationManager.getNotificationById("42", new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                assertEquals("NewTitle", notification.getTitle());
                assertEquals("NewBody", notification.getBody());
                check.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("getNotificationById failed after update");
            }
        });
        await(check);
    }

    /**
     * Tests the deleteNotification method
     * Pass if the notification is removed from the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testDeleteNotificationRemovesFromDb() throws InterruptedException {
        clearAll();
        Notification n = new Notification("Del", "Me", "7", 1, 2);
        CountDownLatch wLatch = new CountDownLatch(1);
        NotificationManager.writeNotification(n, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                wLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("writeNotification failed");
            }
        });
        await(wLatch);
        CountDownLatch del = new CountDownLatch(1);
        NotificationManager.deleteNotification(n.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("deleteNotification failed");
            }
        });
        await(del);

        CountDownLatch check = new CountDownLatch(1);
        NotificationManager.getNotificationById(n.getId(), new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                fail("Notification should have been deleted");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof NotificationNotFound);
                check.countDown();
            }
        });
        await(check);
    }


    /**
     * Tests the getNotificationById method on non-existent notification
     * Pass if the notification is not found
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetNotificationByIdNotFound() throws InterruptedException {
        clearAll();

        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.getNotificationById("9999", new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                fail("Should not find non-existent notification");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof NotificationNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests the getNotificationsByRecipientId method
     * Pass if the list is not empty
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetNotificationsByRecipientIdMultiple() throws InterruptedException {
        clearAll();

        Entrant rec = createEntrant("RecX");
        Entrant other = createEntrant("Other");
        Entrant sender = createEntrant("SenderX");

        CountDownLatch latch = new CountDownLatch(3);
        NotificationManager.sendNotification("T1", "B1", rec.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendNotification("T2", "B2", rec.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendNotification("T3", "B3", other.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        await(latch);

        List<Notification> recNotifs = getNotificationsForRecipient(rec.getId());
        assertEquals(2, recNotifs.size());
        for (Notification n : recNotifs) {
            assertEquals(rec.getId(), n.getRecipientId());
        }
    }

    /**
     * Tests the getNotificationsBySenderId method
     * Pass if the list is not empty
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetNotificationsBySenderIdMultiple() throws InterruptedException {
        clearAll();

        Entrant rec1 = createEntrant("RecY1");
        Entrant rec2 = createEntrant("RecY2");
        Entrant sender = createEntrant("SenderY");

        CountDownLatch latch = new CountDownLatch(3);
        NotificationManager.sendNotification("A", "A", rec1.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendNotification("B", "B", rec2.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendNotification("C", "C", rec1.getId(), sender.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        await(latch);

        List<Notification> list = getNotificationsForSender(sender.getId());
        assertEquals(3, list.size());
        for (Notification n : list) {
            assertEquals(sender.getId(), n.getSenderId());
        }
    }

    /**
     * Tests the sendInvitation method
     * Pass if the invitation is written to the database
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */

    @Test
    public void testSendInvitationCreatesInvitation() throws InterruptedException {
        clearAll();

        Entrant rec = createEntrant("InvRec");
        Entrant sender = createEntrant("InvSender");
        Event event = createValidEvent();

        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "InviteTitle", "InviteBody",
                rec.getId(), sender.getId(), event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("sendInvitation failed: " + e.getMessage());
                    }
                });
        await(latch);
        List<Invitation> invites = getInvitationsForRecipient(rec.getId());
        assertEquals(1, invites.size());
        Invitation inv = invites.get(0);
        assertEquals("InviteTitle", inv.getTitle());
        assertEquals("InviteBody", inv.getBody());
        assertEquals(rec.getId(), inv.getRecipientId());
        assertEquals(sender.getId(), inv.getSenderId());
        assertEquals(event.getId(), inv.getEventId());
        assertEquals(NotificationType.INVITATION, inv.getType());
    }

    /**
     * Tests the getInvitationById method on non-existent invitation
     * Pass if the invitation is not found
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetInvitationByIdNotFound() throws InterruptedException {
        clearAll();

        CountDownLatch latch = new CountDownLatch(1);
        NotificationManager.getInvitationById("9999", new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                fail("Should not find non-existent invitation");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof NotificationNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests the updateInvitation method
     * Pass if the invitation is updated
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */

    @Test
    public void testUpdateInvitationPersistsChanges() throws InterruptedException {
        clearAll();

        Entrant rec = createEntrant("InvUpdRec");
        Entrant sender = createEntrant("InvUpdSender");
        Event event = createValidEvent();

        //Send invitation
        CountDownLatch sendLatch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "Original", "Body",
                rec.getId(), sender.getId(), event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess(){
                        sendLatch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e){
                        fail();
                    }
                });
        await(sendLatch);

        //Fetch it
        List<Invitation> list = getInvitationsForRecipient(rec.getId());
        assertEquals(1, list.size());
        Invitation inv = list.get(0);

        inv.setTitle("NewTitle");
        inv.setBody("NewBody");

        CountDownLatch updLatch = new CountDownLatch(1);
        NotificationManager.updateInvitation(inv, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                updLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("updateInvitation failed");
            }
        });
        await(updLatch);

        CountDownLatch check = new CountDownLatch(1);
        NotificationManager.getInvitationById(inv.getId(), new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                Invitation reloaded = (Invitation)notification;
                assertEquals("NewTitle", reloaded.getTitle());
                assertEquals("NewBody", reloaded.getBody());
                check.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getInvitationById failed after update");
            }
        });
        await(check);
    }

    /**
     * Tests the getInvitationByRecipientId and getInvitationBySenderId and getInvitationByEventId methods
     * Pass if the list is not empty
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testGetInvitationByRecipientAndSenderAndEvent() throws InterruptedException {
        clearAll();

        Entrant r1 = createEntrant("IR1");
        Entrant r2 = createEntrant("IR2");
        Entrant sender = createEntrant("ISender");
        Event e1 = createValidEvent();
        Event e2 = createValidEvent();

        CountDownLatch latch = new CountDownLatch(3);
        NotificationManager.sendInvitation("I1", "B1", r1.getId(), sender.getId(), e1.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendInvitation("I2", "B2", r1.getId(), sender.getId(), e2.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        NotificationManager.sendInvitation("I3", "B3", r2.getId(), sender.getId(), e1.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess(){
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail();
            }
        });
        await(latch);

        //By recipient r1: should be 2
        List<Invitation> byRec = getInvitationsForRecipient(r1.getId());
        assertEquals(2, byRec.size());

        //By sender: should be 3
        List<Invitation> bySend = getInvitationsForSender(sender.getId());
        assertEquals(3, bySend.size());

        //By event e1: should be 2 (r1 and r2)
        List<Invitation> byEvent = getInvitationsForEvent(e1.getId());
        assertEquals(2, byEvent.size());
    }

    /**
     * Tests the acceptInvitation method
     * Pass if the invitation is accepted
     * Fail otherwise
     * @throws InterruptedException
     *      If latch fails to complete
     */
    @Test
    public void testAcceptInvitationMovesEntrantFromWaitlistToEvent() throws InterruptedException {
        clearAll();

        Entrant entrant = createEntrant("AccInv");
        Event event = createValidEvent();

        //Add entrant to waitlist
        CountDownLatch wlLatch = new CountDownLatch(1);
        EventController.addEntrantToWaitlist(event, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                wlLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("addEntrantToWaitlist failed");
            }
        });
        await(wlLatch);
        //Write back updated event state
        CountDownLatch updEvent = new CountDownLatch(1);
        EventController.updateEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                updEvent.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("updateEvent failed");
            }
        });
        await(updEvent);
        //Send invitation for that entrant/event
        CountDownLatch sendLatch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "Join", "Pwease join",
                entrant.getId(), 999, event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess(){
                        sendLatch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e){
                        fail("sendInvitation failed");
                    }
                });
        await(sendLatch);

        //Get the invitation object
        List<Invitation> invList = getInvitationsForRecipient(entrant.getId());
        assertEquals(1, invList.size());
        Invitation inv = invList.get(0);

        CountDownLatch accLatch = new CountDownLatch(1);
        NotificationManager.acceptInvitation(inv, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                accLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("acceptInvitation failed: " + e.getMessage());
            }
        });
        await(accLatch);

        //Check invitation flags
        CountDownLatch invCheck = new CountDownLatch(1);
        NotificationManager.getInvitationById(inv.getId(), new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                Invitation re = (Invitation)notification;
                assertTrue(re.isAccepted());
                assertFalse(re.isDeclined());
                invCheck.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to reload invitation");
            }
        });
        await(invCheck);

        //Check event membership
        CountDownLatch evCheck = new CountDownLatch(1);
        EventController.getEvent(event.getId(), new EventCallback() {
            @Override
            public void onSuccess(Event e) {
                assertTrue(e.getEntrants().contains(entrant));
                assertFalse(e.getWaitlist().getEntrants().contains(entrant));
                evCheck.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to reload event");
            }
        });
        await(evCheck);
    }

    /**
     * Tests the acceptInvitation method on invitation when recipient is not on waitlist
     * Pass if the invitation is not accepted
     * Fail otherwise
     * @throws InterruptedException
     *      Thrown if latch fails to complete
     */
    @Test
    public void testAcceptInvitationFailsWhenNotOnWaitlist() throws InterruptedException {
        clearAll();

        Entrant entrant = createEntrant("AccFail");
        Event event = createValidEvent();

        //Send invitation
        CountDownLatch sendLatch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "Join", "Please join",
                entrant.getId(), 999, event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess(){
                        sendLatch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e){
                        fail("sendInvitation failed");
                    }
                });
        await(sendLatch);

        List<Invitation> list = getInvitationsForRecipient(entrant.getId());
        assertEquals(1, list.size());
        Invitation inv = list.get(0);

        CountDownLatch accLatch = new CountDownLatch(1);
        AtomicReference<Exception> ref = new AtomicReference<>();
        NotificationManager.acceptInvitation(inv, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Should fail when entrant not on waitlist");
            }

            @Override
            public void onFailure(Exception e) {
                ref.set(e);
                accLatch.countDown();
            }
        });
        await(accLatch);
        assertNotNull(ref.get());
    }

    /**
     * Tests the declineInvitation method
     * Pass if the invitation is declined
     * Fail otherwise
     * @throws InterruptedException
     *      Thrown if latch fails to complete
     */
    @Test
    public void testDeclineInvitationRemovesFromWaitlistOnly() throws InterruptedException {
        clearAll();

        Entrant entrant = createEntrant("DecInv");
        Event event = createValidEvent();

        //Add entrant to waitlist
        CountDownLatch wlLatch = new CountDownLatch(1);
        EventController.addEntrantToWaitlist(event, entrant, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                wlLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("addEntrantToWaitlist failed");
            }
        });
        await(wlLatch);

        CountDownLatch updEvent = new CountDownLatch(1);
        EventController.updateEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                updEvent.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("updateEvent failed");
            }
        });
        await(updEvent);

        //Send invitation
        CountDownLatch sendLatch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "Join", "Please join",
                entrant.getId(), 999, event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess(){
                        sendLatch.countDown();
                    }
                    @Override public void onFailure(Exception e){
                        fail("sendInvitation failed");
                    }
                });
        await(sendLatch);

        List<Invitation> list = getInvitationsForRecipient(entrant.getId());
        assertEquals(1, list.size());
        Invitation inv = list.get(0);

        CountDownLatch decLatch = new CountDownLatch(1);
        NotificationManager.declineInvitation(inv, new DBWriteCallback() {
            @Override
            public void onSuccess(){
                decLatch.countDown();
            }
            @Override
            public void onFailure(Exception e){
                fail("declineInvitation failed");
            }
        });
        await(decLatch);

        //Check invitation flags
        CountDownLatch invCheck = new CountDownLatch(1);
        NotificationManager.getInvitationById(inv.getId(), new NotificationCallback() {
            @Override
            public void onSuccess(Notification notification) {
                Invitation re = (Invitation)notification;
                assertTrue(re.isDeclined());
                assertFalse(re.isAccepted());
                invCheck.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to reload invitation");
            }
        });
        await(invCheck);

        //Check event: not on waitlist, also not in entrants
        CountDownLatch evCheck = new CountDownLatch(1);
        EventController.getEvent(event.getId(), new EventCallback() {
            @Override
            public void onSuccess(Event e) {
                assertFalse(e.getWaitlist().getEntrants().contains(entrant));
                assertFalse(e.getEntrants().contains(entrant));
                evCheck.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to reload event");
            }
        });
        await(evCheck);
    }

    /**
     * Tests the declineInvitation method on invitation when recipient is not on waitlist
     * Pass if the invitation is not declined
     * Fail otherwise
     * @throws InterruptedException
     *      Thrown if latch fails to complete
     */
    @Test
    public void testDeclineInvitationFailsWhenNotOnWaitlist() throws InterruptedException {
        clearAll();

        Entrant entrant = createEntrant("DecFail");
        Event event = createValidEvent();

        //Send invitation but not on waitlist
        CountDownLatch sendLatch = new CountDownLatch(1);
        NotificationManager.sendInvitation(
                "Join", "Please join",
                entrant.getId(), 999, event.getId(),
                new DBWriteCallback() {
                    @Override
                    public void onSuccess(){
                        sendLatch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e){
                        fail("sendInvitation failed");
                    }
                });
        await(sendLatch);

        List<Invitation> list = getInvitationsForRecipient(entrant.getId());
        assertEquals(1, list.size());
        Invitation inv = list.get(0);


        CountDownLatch decLatch = new CountDownLatch(1);
        AtomicReference<Exception> ref = new AtomicReference<>();
        NotificationManager.declineInvitation(inv, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Should fail when entrant not on waitlist");
            }

            @Override
            public void onFailure(Exception e) {
                ref.set(e);
                decLatch.countDown();
            }
        });
        await(decLatch);
        assertNotNull(ref.get());
    }

    /**
     * Tests that invitation is deleted when recipient is removed from waitlist
     * Pass if the invitation is deleted
     * Fail otherwise
     */
    @Test
    public void testDeleteInvitationRemovesFromWaitlist() {
        //TODO: add test here
    }

    /**
     * Test that invitation is deleted when user accepts invitation
     * Pass if the invitation is deleted
     * Fail otherwise
     */
    @Test
    public void testAcceptInvitationRemovesFromWaitlist() {
        //TODO: add test here
    }

    /**
     * Tests that invitation is deleted when user declines invitation
     * Pass if the invitation is deleted
     * Fail otherwise
     */
    @Test
    public void testDeclineInvitationRemovesFromWaitlist() {
        //TODO: add test here
    }

    @Test
    public void testSendMessageCreatesLog() {
        //TODO: add test here
    }

    @Test
    public void testDeleteMessageCreatesLog() {
        //TODO: add test here
    }

    @Test
    public void testUpdateMessageCreatesLog() {
        //TODO: add test here
    }

    @Test
    public void sendInvitationCreatesLog() {
        //TODO: add test
    }

    @Test
    public void testDeclineInvitationCreatesLog() {
        //TODO: add test here
    }

    @Test
    public void testAcceptInvitationCreatesLog() {
        //TODO: add test here
    }






}





