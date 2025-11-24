package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
 * @version 0.1
 */
public class NotificationManager {

    /**
     * Singleton instance of NotificationManager
     */
    private static NotificationManager instance;


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

    public static void setTesting(boolean testing) {
        if (testing) {
            notificationRef = db.collection("test_notifications");
        } else {
            notificationRef = db.collection("notifications");
        }
    }

    public static void sendNotifications(String title, String body, List<Entrant> recipients, int senderId,
                                         DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            callback.onSuccess();
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        for (Entrant recipient : recipients) {
            sendNotification(title, body, recipient.getId(), senderId, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (completed.incrementAndGet() == recipients.size()) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }




    /**
     * Sends a standard notification to a recipient.
     * Creates a Notification object, writes it to the database, and logs it.
     *
     * @param title Title of the notification
     * @param body Body text of the notification
     * @param recipientId ID of the recipient entrant
     * @param senderId ID of the sender entrant
     * @param callback Callback for success/failure of database write
     */



    public static void sendNotification(String title, String body,
                                        int recipientId, int senderId,
                                        DBWriteCallback callback) {

        DocumentReference ref = notificationRef.document();
        String id = ref.getId();

        Notification notification = new Notification(title, body, id, recipientId, senderId);
        notification.setType(NotificationType.NOTIFICATION);

        ref.set(notification)
                .addOnSuccessListener(aVoid ->
                        Logger.log(notification, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        }))
                .addOnFailureListener(callback::onFailure);
    }

    public static void sendBulkNotification(String title, String body, List<Integer> recipients,
                                            int senderId, DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            callback.onSuccess();
            return;
        }
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
                            callback.onSuccess();
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
                        callback.onFailure(e);
                    }
                }
            });

        }
    }

    public static void sendBulkInvitation(String title, String body, List<Integer> recipients,
                                          int senderId, int eventID, DBWriteCallback callback) {
        if (recipients.isEmpty()) {
            callback.onSuccess();
            return;
        }
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
                            callback.onSuccess();
                        }
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
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
     * @param title Title of the invitation
     * @param body Body text of the invitation
     * @param recipientId ID of the recipient entrant
     * @param senderId ID of the sender entrant
     * @param eventId ID of the associated event
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
                        Logger.log(invitation, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        }))
                .addOnFailureListener(callback::onFailure);
    }








    /**
     * Gets the next available notification ID
     * @param callback
     *      Callback to call when the operation is complete
     *      */
    public static void getNotificationId(NotificationIDCallback callback) {
        notificationRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    int highestId = 0;
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            int id = doc.getLong("id").intValue();
                            if (id > highestId) highestId = id;
                        }
                    }
                    // Always call callback once
                    callback.onSuccess(highestId + 1);
                })
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to get next notification ID")));
    }

    /**
     * Gets all notifications from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getAllNotifications (NotificationListCallback callback){
        notificationRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Notification notification = doc.toObject(Notification.class);
                                notifications.add(notification);
                            }
                            callback.onSuccess(notifications);


                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));
                    }
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to delete notification")));
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write notification")));
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write notification")));
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
                        onComplete.run();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> onComplete.run())
                            .addOnFailureListener(e -> onComplete.run());
                })
                .addOnFailureListener(e -> {
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Notification notification = doc.toObject(Notification.class);
                            callback.onSuccess(notification);
                        } else {
                            callback.onFailure(new NotificationNotFound("Notification not found", String.valueOf(id)));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notification"));
                    }
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
        notificationRef.get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot) {
                System.out.println(doc.getData());
            }
        });

        notificationRef.whereEqualTo("recipientId", recipientId)
                .whereEqualTo("type", NotificationType.NOTIFICATION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Notification notification = doc.toObject(Notification.class);
                                notifications.add(notification);
                            }
                            callback.onSuccess(notifications);
                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));
                    }
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Notification notification = doc.toObject(Notification.class);
                                notifications.add(notification);
                            }
                            callback.onSuccess(notifications);
                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));

                    }
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Invitation invitation = doc.toObject(Invitation.class);
                            callback.onSuccess(invitation);
                        } else {
                            callback.onFailure(new NotificationNotFound("Notification not found", String.valueOf(id)));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notification"));
                    }
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Invitation invitation = doc.toObject(Invitation.class);
                                notifications.add(invitation);
                            }
                            callback.onSuccess(notifications);
                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));
                    }
                });
    }
    public static void updateInvitation(Invitation invitation, DBWriteCallback callback) {
        notificationRef.document(String.valueOf(invitation.getId()))
                .set(invitation)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write invitation")));
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Invitation invitation = doc.toObject(Invitation.class);
                                notifications.add(invitation);
                            }
                            callback.onSuccess(notifications);
                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));
                    }
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Invitation invitation = doc.toObject(Invitation.class);
                                notifications.add(invitation);
                            }
                            callback.onSuccess(notifications);
                        } else {
                            callback.onSuccess(new ArrayList<Notification>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notifications"));
                    }
                });

    }

    public static void acceptInvitation(Invitation invitation, DBWriteCallback callback) {

        // Mark accepted immediately
        invitation.setAccepted(true);
        invitation.setDeclined(false);

        // 1. Get the event
        EventController.getEvent(invitation.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {

                // 2. Get the entrant
                EntrantController.getEntrant(invitation.getRecipientId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {

                        // 3. Remove from waitlist
                        EventController.removeEntrantFromWaitlist(event, entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {

                                // 4. Add to event
                                EventController.addEntrantToEvent(event, entrant, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {

                                        // 5. Update invitation in DB
                                        updateInvitation(invitation, new DBWriteCallback() {
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
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    public static void declineInvitation(Invitation invitation, DBWriteCallback callback) {

        invitation.setDeclined(true);
        invitation.setAccepted(false);

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

                                // 4. Update invitation in DB
                                updateInvitation(invitation, new DBWriteCallback() {
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
    }

}
