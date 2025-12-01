package com.example.slices.controllers;

import android.annotation.SuppressLint;



import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.NotificationNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.NotificationCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.example.slices.models.NotSelected;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton class to manage sending notifications and invitations.
 * Handles creating notifications, assigning IDs, writing to the database, and logging.
 * Uses DBConnector for database operations.
 *
 * @author Ryan
 * @version 1.5
 */
public class NotificationManager {

    /**
     * Singleton instance of NotificationManager
     */
    private static NotificationManager instance;


    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CollectionReference notificationRef = db.collection("notifications");


    /**
     * Private constructor to prevent external instantiation
     */
    private NotificationManager() {
    }

    /**
     * Returns the singleton instance of NotificationManager
     *
     * @return NotificationManager instance
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Sets the testing mode for the NotificationManager
     * @param testing
     *      True if testing mode is enabled, false otherwise
     */

    public static void setTesting(boolean testing) {
        if (testing) {
            notificationRef = db.collection("test_notifications");
        } else {
            notificationRef = db.collection("notifications");
        }
    }



    /**
     * Sends a standard notification to a recipient.
     * Creates a Notification object, writes it to the database, and logs it.
     *
     * @param title
     *      Title of the notification
     * @param body
     *      Body text of the notification
     * @param recipientId
     *      ID of the recipient entrant
     * @param senderId
     *      ID of the sender entrant
     * @param callback
     *      Callback for success/failure of database write
     */
    public static void sendNotification(String title, String body,
                                        int recipientId, int senderId,
                                        DBWriteCallback callback) {
        sendNotification(title, body, recipientId, senderId, 0, callback);
    }

    /**
     * Sends a standard notification to a recipient with an associated event.
     * Creates a Notification object, writes it to the database, and logs it.
     *
     * @param title Title of the notification
     * @param body Body text of the notification
     * @param recipientId ID of the recipient entrant
     * @param senderId ID of the sender entrant
     * @param eventId ID of the associated event (0 if not event-related)
     * @param callback Callback for success/failure of database write
     */
    public static void sendNotification(String title, String body,
                                        int recipientId, int senderId, int eventId,
                                        DBWriteCallback callback) {

        DocumentReference ref = notificationRef.document();
        String id = ref.getId();

        Notification notification = new Notification(title, body, id, recipientId, senderId);
        notification.setType(NotificationType.NOTIFICATION);
        notification.setEventId(eventId);

        ref.set(notification)
                .addOnSuccessListener(aVoid ->
                        Logger.logNotification(title + " " + body, recipientId, senderId, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logSystem("Notification sent successfully to recipientId=" + recipientId, null);
                                callback.onSuccess();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to log notification for recipientId=" + recipientId, null);
                                callback.onFailure(e);
                            }
                        }))
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to send notification to recipientId=" + recipientId + " senderId=" + senderId, null);
                    callback.onFailure(e);
                });
    }

    /**
     * Sends a bulk notification to a list of recipients.
     * @param title
     *      Title of the notification
     * @param body
     *      Body text of the notification
     * @param recipients
     *      List of recipients to send the notification to
     * @param senderId
     *      ID of the sender entrant
     * @param callback
     *      Callback for success/failure of database write
     */
    public static void sendBulkNotification(String title, String body, List<Integer> recipients,
                                            int senderId, DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            Logger.logSystem("sendBulkNotification called with empty recipients list", null);
            callback.onSuccess();
            return;
        }

        Logger.logSystem("Starting bulk notification send to " + recipients.size() + " recipients", null);

        AtomicInteger completed = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);
        for (int recipient : recipients) {
            sendNotification(title, body, recipient, senderId, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (failed.get()) {
                        return;
                    }
                    if (completed.incrementAndGet() == recipients.size()) {
                        if (!failed.get()) {
                            Logger.logSystem("Bulk notification send completed for " + recipients.size() + " recipients", null);
                            callback.onSuccess();
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
                        Logger.logError("Bulk notification send failed: " + e.getMessage(), null);
                        callback.onFailure(e);
                    }
                }
            });

        }
    }

    public static void sendNotSelected(String title, String body,
                                       int recipientId, int senderId, int eventId,
                                       DBWriteCallback callback) {
        DocumentReference ref = notificationRef.document();
        String id = ref.getId();

        NotSelected notification = new NotSelected(title, body, id, recipientId, senderId, eventId);

        ref.set(notification)
                .addOnSuccessListener(aVoid ->
                        Logger.logNotSelected(title + " " + body, recipientId, senderId, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logSystem("NotSelected sent successfully to recipientId=" + recipientId, null);
                                callback.onSuccess();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to log NotSelected for recipientId=" + recipientId, null);
                                callback.onFailure(e);
                            }
                        }))
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to send notification to recipientId=" + recipientId + " senderId=" + senderId, null);
                    callback.onFailure(e);
                });
    }

    public static void sendBulkNotSelected(String title, String body, List<Integer> recipients,
                                            int senderId, int eventId, DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            Logger.logSystem("sendBulkNotSelected called with empty recipients list", null);
            callback.onSuccess();
            return;
        }

        Logger.logSystem("Starting bulk not selected send to " + recipients.size() + " recipients", null);

        AtomicInteger completed = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);
        for (int recipient : recipients) {
            sendNotSelected(title, body, recipient, senderId, eventId, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (failed.get()) {
                        return;
                    }
                    if (completed.incrementAndGet() == recipients.size()) {
                        if (!failed.get()) {
                            Logger.logSystem("Bulk not selected send completed for " + recipients.size() + " recipients", null);
                            callback.onSuccess();
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
                        Logger.logError("Bulk not selected send failed: " + e.getMessage(), null);
                        callback.onFailure(e);
                    }
                }
            });

        }
    }

    /**
     * Sends a bulk invitation to a list of recipients.
     * @param title
     *      Title of the invitation
     * @param body
     *      Body text of the invitation
     * @param recipients
     *      List of recipients to send the invitation to
     * @param senderId
     *      ID of the sender entrant
     * @param eventID
     *      ID of the associated event
     * @param callback
     *      Callback for success/failure of database write
     */
    public static void sendBulkInvitation(String title, String body, List<Integer> recipients,
                                          int senderId, int eventID, DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            Logger.logSystem("sendBulkInvitation called with empty recipients list for eventId=" + eventID, null);
            callback.onSuccess();
            return;
        }

        Logger.logSystem("Starting bulk invitation send for eventId=" + eventID
                + " to " + recipients.size() + " recipients", null);

        AtomicInteger completed = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);
        for (int recipient : recipients) {
            sendInvitation(title, body, recipient, senderId, eventID, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (failed.get()) {
                        return;
                    }
                    if (completed.incrementAndGet() == recipients.size()) {
                        if (!failed.get()) {
                            Logger.logSystem("Bulk invitation send completed for eventId=" + eventID, null);
                            callback.onSuccess();
                        }
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
                        Logger.logError("Bulk invitation send failed for eventId=" + eventID + ": " + e.getMessage(), null);
                        callback.onFailure(e);
                    }
                }
            });
        }
    }







    /**
     * Sends an invitation notification for an event.
     * Creates an Invitation object, writes it to the database, and logs it.
     *
     * @param title
     *      Title of the invitation
     * @param body
     *      Body text of the invitation
     * @param recipientId
     *      ID of the recipient entrant
     * @param senderId
     *      ID of the sender entrant
     * @param eventId
     *      ID of the associated event
     */
    public static void sendInvitation(String title, String body,
                                      int recipientId, int senderId,
                                      int eventId,
                                      DBWriteCallback callback) {

        DocumentReference ref = notificationRef.document();
        String id = ref.getId();

        Invitation invitation = new Invitation(title, body, id, recipientId, senderId, eventId);
        invitation.setType(NotificationType.INVITATION);

        ref.set(invitation)
                .addOnSuccessListener(aVoid ->
                        Logger.logInvSent(eventId, recipientId, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logSystem("Invitation sent successfully for eventId=" + eventId
                                        + " to recipientId=" + recipientId, null);
                                callback.onSuccess();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to log invitation sent for eventId=" + eventId
                                        + " recipientId=" + recipientId, null);
                                callback.onFailure(e);
                            }
                        }))
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to send invitation for eventId=" + eventId
                            + " to recipientId=" + recipientId, null);
                    callback.onFailure(e);
                });
    }





    /**
     * Gets all notifications from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getAllNotifications (NotificationListCallback callback){
        notificationRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            notifications.add(notification);
                        }
                        Logger.logSystem("Fetched " + notifications.size() + " notifications", null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No notifications found in getAllNotifications", null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications in getAllNotifications: " + e.getMessage(), null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));
                });
    }

    /**
     * Convenience accessor used by tests: fetch all notifications for a recipient (any type).
     * @param recipientId recipient entrant ID
     * @param callback callback to receive notifications
     */
    public static void getNotifications(int recipientId, NotificationListCallback callback) {
        notificationRef.whereEqualTo("recipientId", recipientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            // Map to specific subclass based on type for correct fields
                            NotificationType type = null;
                            try {
                                String typeStr = doc.getString("type");
                                if (typeStr != null) {
                                    type = NotificationType.valueOf(typeStr);
                                }
                            } catch (Exception ignored) {}

                            Notification notification = null;
                            if (type == NotificationType.INVITATION) {
                                notification = doc.toObject(Invitation.class);
                            } else if (type == NotificationType.NOT_SELECTED) {
                                notification = doc.toObject(NotSelected.class);
                            } else {
                                notification = doc.toObject(Notification.class);
                            }
                            if (notification != null) notifications.add(notification);
                        }
                    }
                    Logger.logSystem("Fetched " + notifications.size() + " notifications for recipientId=" + recipientId, null);
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications for recipientId=" + recipientId, null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));
                });
    }

    /**
     * Deletes a notification from the database
     * @param id
     *      Notification ID to delete
     */

    public static void deleteNotification (String id, DBWriteCallback callback){
        notificationRef.document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Logger.logSystem("Deleted notification with id=" + id, null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to delete notification with id=" + id, null);
                    callback.onFailure(new DBOpFailed("Failed to delete notification"));
                });
    }


    /**
     * Writes a notification to the database
     * @param notification
     *      Notification to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void writeNotification (Notification notification, DBWriteCallback callback){
        notificationRef.document(String.valueOf(notification.getId()))
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Logger.logSystem("Notification written with id=" + notification.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to write notification with id=" + notification.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write notification"));
                });
    }

    /**
     * Updates a notification in the database
     * @param notification
     *      Notification to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void updateNotification (Notification notification, DBWriteCallback callback){
        notificationRef.document(String.valueOf(notification.getId()))
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    Logger.logSystem("Notification updated with id=" + notification.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to update notification with id=" + notification.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write notification"));
                });
    }

    /**
     * Clears all notifications from the database asynchronously: Used for testing
     * @param onComplete
     *      Callback to call when the operation is complete

     */

    public static void clearNotifications(Runnable onComplete) {
        notificationRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Logger.logSystem("clearNotifications: no notifications to clear", null);
                        onComplete.run();
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Logger.logSystem("All notifications cleared successfully", null);
                                onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                Logger.logError("Failed to clear notifications in batch commit: " + e.getMessage(), null);
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to clear notifications: " + e.getMessage(), null);
                    System.out.println("Failed to clear notifications: " + e.getMessage());
                    onComplete.run();
                });
    }

    /**
     * Gets a notification from the database asynchronously
     * @param id
     *      Notification ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getNotificationById (String id, NotificationCallback callback){
        notificationRef.whereEqualTo("id", id)
                .whereEqualTo("type", NotificationType.NOTIFICATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Notification notification = doc.toObject(Notification.class);
                        Logger.logSystem("Found notification with id " + id, null);
                        callback.onSuccess(notification);
                    } else {
                        Logger.logSystem("No notification found with id " + id, null);
                        callback.onFailure(new NotificationNotFound("Notification not found", String.valueOf(id)));
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notification with id " + id, null);
                    callback.onFailure(new DBOpFailed("Failed to get notification"));
                });
    }

    /**
     * Gets all notifications for a single recipient from the database asynchronously
     * @param recipientId
     *      Recipient ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getNotificationsByRecipientId ( int recipientId, NotificationListCallback
            callback){
        notificationRef.whereEqualTo("recipientId", recipientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            NotificationType type = null;
                            try {
                                String typeStr = doc.getString("type");
                                if (typeStr != null) {
                                    type = NotificationType.valueOf(typeStr);
                                }
                            } catch (Exception ignored) {}

                            Notification notification;
                            if (type == NotificationType.INVITATION) {
                                notification = doc.toObject(Invitation.class);
                            } else if (type == NotificationType.NOT_SELECTED) {
                                notification = doc.toObject(NotSelected.class);
                            } else {
                                notification = doc.toObject(Notification.class);
                            }
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        Logger.logSystem("Found " + notifications.size() + " notifications by recipientID " + recipientId, null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No notifications found by recipientID " + recipientId, null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications by recipientID " + recipientId, null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));
                });
    }


    /**
     * Gets all notifications for a single sender from the database asynchronously
     * @param senderId
     *      ID of the sender to search for
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getNotificationsBySenderId ( int senderId, NotificationListCallback callback){
        notificationRef.whereEqualTo("senderId", senderId)
                .whereEqualTo("type", NotificationType.NOTIFICATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            notifications.add(notification);
                        }
                        Logger.logSystem("Found " + notifications.size() + " notifications by senderID " + senderId, null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No notifications found by senderID " + senderId, null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications by senderID " + senderId , null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));
                });
    }

    public static void getNotSelectedByRecipientId ( int recipientId, NotificationListCallback callback){
        notificationRef.whereEqualTo("recipientId", recipientId)
                .whereEqualTo("type", NotificationType.NOT_SELECTED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<Notification> notifications = new ArrayList<>();
                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    NotSelected notSelected = doc.toObject(NotSelected.class);
                                    notifications.add(notSelected);
                                }
                                Logger.logSystem("Found " + notifications.size() + " NotSelected by recipientID " + recipientId, null);
                                callback.onSuccess(notifications);
                            } else {
                                Logger.logSystem("No NotSelected found by recipientID " + recipientId, null);
                                callback.onSuccess(new ArrayList<>());
                            }
                        })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get NotSelected by recipientID " + recipientId, null);
                    callback.onFailure(new DBOpFailed("Failed to get NotSelected"));
                });
    }

    public static void acceptNotSelected(NotSelected notSelected, DBWriteCallback callback) {
        notSelected.setDeclined(false);
        notSelected.setStayed(true);
        updateNotSelected(notSelected, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.logSystem("NotSelected accept pipeline completed successfully", null);
                callback.onSuccess();
            }
            @Override
            public void onFailure(Exception e) {
                Logger.logError("NotSelected accept pipeline failed", null);
                callback.onFailure(e);
            }
        });
    }

    public static void declineNotSelected(NotSelected notSelected, DBWriteCallback callback) {
        notSelected.setDeclined(true);
        notSelected.setStayed(false);
        EventController.getEvent(notSelected.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                EntrantController.getEntrant(notSelected.getRecipientId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        EventController.removeEntrantFromWaitlist(event, entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logWaitlistModified("Removed from waitlist", event.getId(), entrant.getId(), null);
                                updateNotSelected(notSelected, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Logger.logSystem("NotSelected decline pipeline completed successfully", null);
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Logger.logError("NotSelected decline pipeline failed", null);
                                        callback.onFailure(e);
                                    }
                                });
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to remove entrant from waitlist during NotSelected decline pipeline", null);
                                callback.onFailure(e);
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to get entrant during NotSelected decline pipeline", null);
                        callback.onFailure(e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to get event during NotSelected decline pipeline", null);
                callback.onFailure(e);
            }
        });
    }

    public static void updateNotSelected(NotSelected notSelected, DBWriteCallback callback) {
        notificationRef.document(String.valueOf(notSelected.getId()))
                .set(notSelected)
                .addOnSuccessListener(aVoid -> {
                    Logger.logSystem("NotSelected updated with id=" + notSelected.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to update NotSelected with id=" + notSelected.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write NotSelected to database"));
                });
    }

    /**
     * Gets a single invitation from the database asynchronously
     * @param id
     *      ID of the invitation to search for
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getInvitationById(String id, NotificationCallback callback) {
        notificationRef.whereEqualTo("id", id )
                .whereEqualTo("type", NotificationType.INVITATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Invitation invitation = doc.toObject(Invitation.class);
                        Logger.logSystem("Found invitation with id " + id, null);
                        callback.onSuccess(invitation);
                    } else {
                        Logger.logSystem("No invitation found with id " + id, null);
                        callback.onFailure(new NotificationNotFound("Notification not found", String.valueOf(id)));
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get invitation with id " + id, null);
                    callback.onFailure(new DBOpFailed("Failed to get notification"));
                });
    }

    /**
     * Gets all invitations for a single recipient from the database asynchronously
     * @param recipientId
     *      ID of the recipient to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getInvitationByRecipientId(int recipientId, NotificationListCallback callback) {
        notificationRef.whereEqualTo("recipientId", recipientId)
                .whereEqualTo("type", NotificationType.INVITATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Invitation invitation = doc.toObject(Invitation.class);
                            notifications.add(invitation);
                        }
                        Logger.logSystem("Found " + notifications.size() + " invitations by recipient " + recipientId, null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No invitations found by recipient " + recipientId, null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get invitations by recipientId " + recipientId, null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications by recipient"));
                });
    }

    /**
     * Updates a notification in the database
     * @param invitation
     *      Notification to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void updateInvitation(Invitation invitation, DBWriteCallback callback) {
        notificationRef.document(String.valueOf(invitation.getId()))
                .set(invitation)
                .addOnSuccessListener(aVoid -> {
                    Logger.logSystem("Invitation updated with id=" + invitation.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to update invitation with id=" + invitation.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write invitation to database"));
                });
    }


    /**
     * Gets all invitations for a single sender from the database asynchronously
     * @param senderId
     *      ID of the sender to search for
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getInvitationBySenderId(int senderId, NotificationListCallback callback) {
        notificationRef.whereEqualTo("senderId", senderId)
                .whereEqualTo("type", NotificationType.INVITATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Invitation invitation = doc.toObject(Invitation.class);
                            notifications.add(invitation);
                        }
                        Logger.logSystem("Found " + notifications.size() + " invitations", null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No invitations found", null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications", null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));

                });
    }

    /**
     * Gets all invitations for a single event from the database asynchronously
     * @param eventId
     *      ID of the event to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getInvitationByEventId(int eventId, NotificationListCallback callback) {
        notificationRef.whereEqualTo("eventId", eventId)
                .whereEqualTo("type", NotificationType.INVITATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Invitation invitation = doc.toObject(Invitation.class);
                            notifications.add(invitation);
                        }
                        Logger.logSystem("Found " + notifications.size() + " invitations", null);
                        callback.onSuccess(notifications);
                    } else {
                        Logger.logSystem("No invitations found", null);
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get notifications", null);
                    callback.onFailure(new DBOpFailed("Failed to get notifications"));
                });

    }

    /**
     * Accepts an invitation to an event
     * @param invitation
     *      Invitation to accept
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void acceptInvitation(Invitation invitation, DBWriteCallback callback) {

        // Mark accepted immediately
        invitation.setAccepted(true);
        invitation.setDeclined(false);

        Logger.logInvAccepted(invitation.getEventId(), invitation.getRecipientId(), null);


        // 1. Get the event
        EventController.getEvent(invitation.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                // 2. Get the entrant
                EntrantController.getEntrant(invitation.getRecipientId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {

                        // If entrant already in event, just mark invitation accepted and persist
                        if (event.getEntrantIds() != null && event.getEntrantIds().contains(entrant.getId())) {
                            invitation.setAccepted(true);
                            invitation.setDeclined(false);
                            // Clean up waitlist/cancelled/invited state
                            if (event.getCancelledIds() != null) {
                                event.getCancelledIds().remove(Integer.valueOf(entrant.getId()));
                            }
                            if (event.getInvitedIds() != null && event.getInvitedIds().contains(entrant.getId())) {
                                event.getInvitedIds().remove(Integer.valueOf(entrant.getId()));
                            }
                            updateInvitation(invitation, new DBWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    EventController.updateEvent(event, new DBWriteCallback() {
                                        @Override
                                        public void onSuccess() {
                                            callback.onSuccess();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            callback.onFailure(e);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(e);
                                }
                            });
                            return;
                        }

                        // 3. Remove from waitlist
                        EventController.removeEntrantFromWaitlist(event, entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logWaitlistModified("Removed from waitlist", event.getId(), entrant.getId(), null);
                                // 4. Add to event
                                EventController.addEntrantToEvent(event, entrant, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Logger.logEntrantJoin(entrant.getId(), event.getId(), null);
                                        // 5. Update invitation in DB
                                        updateInvitation(invitation, new DBWriteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Logger.logSystem("Invitation accept pipeline completed successfully", null);
                                                callback.onSuccess();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Logger.logError("Invitation accept pipeline failed", null);
                                                callback.onFailure(e);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Logger.logError("Failed to add entrant to event during invitation accept pipeline", null);
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to remove entrant from waitlist during invitation accept pipeline", null);
                                callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to get entrant during invitation accept pipeline", null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to get event during invitation accept pipeline", null);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Declines an invitation to an event
     * @param invitation
     *      Invitation to decline
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void declineInvitation(Invitation invitation, DBWriteCallback callback) {

        invitation.setDeclined(true);
        invitation.setAccepted(false);

        Logger.logInvDeclined(invitation.getEventId(), invitation.getRecipientId(), null);


        // 1. Get event
        EventController.getEvent(invitation.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {

                // 2. Get entrant
                EntrantController.getEntrant(invitation.getRecipientId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {

                        // 3. Remove from waitlist only
                        EventController.removeEntrantFromWaitlist(event, entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logWaitlistModified("Removed from waitlist", event.getId(), entrant.getId(), null);

                                // 4. Add user to cancelledIds list
                                List<Integer> cancelledIds = event.getCancelledIds();
                                if (cancelledIds == null) {
                                    cancelledIds = new ArrayList<>();
                                    event.setCancelledIds(cancelledIds);
                                }
                                if (!cancelledIds.contains(entrant.getId())) {
                                    cancelledIds.add(entrant.getId());
                                    Logger.logSystem("Added entrant to cancelledIds: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                                    
                                    // Send automatic cancellation notification
                                    EventController.sendCancelledNotification(entrant.getId(), event.getEventInfo().getName());
                                }

                                // Ensure cancelled entrant is not shown as invited
                                List<Integer> invitedIds = event.getInvitedIds();
                                if (invitedIds != null && invitedIds.contains(entrant.getId())) {
                                    invitedIds.remove(Integer.valueOf(entrant.getId()));
                                    Logger.logSystem("Removed entrant from invitedIds after decline: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                                }

                                // 5. Persist updated event to Firestore
                                EventController.updateEvent(event, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Logger.logSystem("Event updated with cancelled user: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);

                                        // 6. Update invitation in DB
                                        updateInvitation(invitation, new DBWriteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Logger.logSystem("Invitation decline pipeline completed successfully", null);
                                                callback.onSuccess();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Logger.logError("Invitation decline pipeline failed", null);
                                                callback.onFailure(e);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Logger.logError("Failed to update event with cancelled user during invitation decline pipeline", null);
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to remove entrant from waitlist during invitation decline pipeline", null);
                                callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to get entrant during invitation decline pipeline", null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to get event during invitation decline pipeline", null);
                callback.onFailure(e);
            }
        });
    }

}
