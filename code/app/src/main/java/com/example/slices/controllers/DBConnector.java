package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.Event;
import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.exceptions.NotificationNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.interfaces.LogIDCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.interfaces.NotificationCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLog;
import com.example.slices.models.Log;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLog;
import com.example.slices.models.NotificationType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to interact with the Firebase Firestore database.
 * It provides methods for getting, writing, and deleting data from the database.
 * @author Ryan Haubrich
 * @version 0.1
 *
 *
 *
 */
public class DBConnector {
    /**
     * Firebase Firestore database instance
     */
    private FirebaseFirestore db;

    /**
     * Collection reference for the entrant collection in the database
     */
    private CollectionReference entrantRef;
    /**
     * Collection reference for the auth collection in the database
     */
    private CollectionReference authRef;
    /**
     * Collection reference for the event collection in the database
     */
    private CollectionReference eventRef;
    /**
     * Collection reference for the notification collection in the database
     */
    private CollectionReference notificationRef;
    /**
     * Collection reference for the log collection in the database
     */
    private CollectionReference logRef;





    /**
     * Constructor for the DBConnector class.
     * Initializes the Firebase Firestore database and references to the different collections.
     *
     */

    public DBConnector() {
        db = FirebaseFirestore.getInstance();
        entrantRef = db.collection("entrants");
        authRef = db.collection("auth");
        eventRef = db.collection("events");
    }

    /**
     * Gets an entrant from the database asynchronously
     * @param id
     *      Entrant ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    /**public void getEntrant(int id, EntrantCallback callback) {
        entrantRef
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Entrant entrant = doc.toObject(Entrant.class);
                            callback.onSuccess(entrant);
                        } else {
                            callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get entrant"));
                    }
                });

        }*/

    /**
     * Gets an entrant from the database asynchronously
     * @param id
     *      Entrant device ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getEntrant(String id, EntrantCallback callback) {
        entrantRef
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                                //Object id = doc.get("deviceId");
                                Entrant entrant = doc.toObject(Entrant.class);
                                if(entrant != null) {
                                    callback.onSuccess(entrant);
                                }
                                else {
                                    callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));
                                }
                            }
                        }
                        else{
                            callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get entrant"));
                    }
                });

    }

    /**
     * Gets an event from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     * @param id
     *      Event ID to search for
     */

    public void getEvent(int id, EventCallback callback) {
        eventRef
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Event event = doc.toObject(Event.class);
                            callback.onSuccess(event);
                        } else {
                            callback.onFailure(new EventNotFound("Event not found", String.valueOf(id)));

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get event"));
                    }
                });

    }

    /**
     * Gets the next available entrant ID
     * @return
     *      The next available entrant ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getNewEntrantId(EntrantIDCallback callback) {
        entrantRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the highest ID
                            int highestId = 0;
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                int id = doc.getLong("id").intValue();
                                if (id > highestId) {
                                    highestId = id;
                                }
                            }
                            // Return the next ID
                            callback.onSuccess(highestId + 1);
                        } else {
                            callback.onSuccess(1);
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get next entrant ID"));
                    }
                });


    }

    /**
     * Writes an entrant to the database asynchronously
     * @param entrant
     *      Entrant to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void writeEntrant(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(entrant.getId())
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));

    }

    /**
     * Writes an entrant to the database asynchronously
     * @param entrant
     *      Entrant to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    /**public void writeEntrantDeviceId(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(entrant.getDeviceId())
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));

    }*/

    /**
     * Writes an event to the database asynchronously
     * @param event
     *      Event to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void writeEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write event")));

    }

    /**
     * Updates an event in the database asynchronously
     * @param event
     *      Event to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void updateEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write event")));

    }

    public void waitlistEntrant(int eventId, String entrantId, DBWriteCallback callback) {
        eventRef.document(String.valueOf(eventId))
                .update("waitlist.entrants", FieldValue.arrayUnion(entrantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to add Entrant to waitlist")));
    }


    public void leaveWaitlistEntrant(int eventId, String entrantId, DBWriteCallback callback) {
        eventRef.document(String.valueOf(eventId))
                .update("waitlist.entrants", FieldValue.arrayRemove(entrantId))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to remove Entrant from waitlist")));
    }

    /**
     * Updates an entrant in the database asynchronously
     * @param entrant
     *      Entrant to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void updateEntrant(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));
    }


    /**
     * Deletes an entrant from the database asynchronously
     * @param id
     *      Entrant ID to delete
     */
    public void deleteEntrant(String id) {
        entrantRef.document(id).delete();
    }

    /**
     * Gets the next available event ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getNewEventId(EventIDCallback callback) {
        eventRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the highest ID
                            int highestId = 0;
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                int id = doc.getLong("id").intValue();
                                if (id > highestId) {
                                    highestId = id;
                                }
                            }
                            // Return the next ID
                            callback.onSuccess(highestId + 1);
                        } else {
                            callback.onSuccess(1);
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get next event ID"));
                    }
                });

    }

    /**
     * Gets all entrants or a specific event
     * @param eventId
     *      Event ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getEntrantsForEvent(int eventId, EntrantListCallback callback) {
        eventRef
                .whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Event event = doc.toObject(Event.class);
                            if (event != null && event.getEntrants() != null) {
                                callback.onSuccess(event.getEntrants());
                            } else {
                                // Event exists but has no entrants
                                callback.onSuccess(new ArrayList<String>());
                            }
                        } else {
                            callback.onFailure(new EventNotFound("Event not found", String.valueOf(eventId)));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get entrants for event"));
                    }
                });
    }

    /**
     * Gets all events for a given entrant
     * @param id
     *      user device id to find events for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getEventsForEntrant(String id, EntrantEventCallback callback) {
        eventRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Event> events = new ArrayList<>();
                            List<Event> waitEvents = new ArrayList<>();
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Event event = doc.toObject(Event.class);
                                if(event.getEntrants().contains(id))
                                    events.add(event);
                                else if(event.getWaitlist().getEntrants().contains(id))
                                    waitEvents.add(event);
                            }
                            if (events != null) {
                                callback.onSuccess(events, waitEvents);
                            } else {
                                // Event exists but has no entrants
                                callback.onSuccess(new ArrayList<Event>(), new ArrayList<>());
                            }
                        } else {
                            callback.onFailure(new EventNotFound("No events found for ", id));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get events for an entrant"));
                    }
                });
    }

    /**
     * Deletes an event from the database asynchronously
     * @param id
     *      Event ID to delete
     */
    public void deleteEvent(String id) {
        eventRef.document(id).delete();
    }

    /**
     * Clears all entrants from the database asynchronously: Used for testing
     * @param onComplete
     *      Callback to call when the operation is complete
     *
     */
    public void clearEntrants(Runnable onComplete) {
        entrantRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(entrantRef.document(doc.getId()).delete());
                    }
                    // Wait for all deletes to finish
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> onComplete.run());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear entrants: " + e.getMessage());
                    onComplete.run();
                });
    }

    /**
     * Clears all events from the database asynchronously: Used for testing
     * @param onComplete
     *      Callback to call when the operation is complete
     */
    public void clearEvents(Runnable onComplete) {
        eventRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(eventRef.document(doc.getId()).delete());
                    }
                    // Wait for all deletes to finish
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> onComplete.run());

                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear events: " + e.getMessage());
                    onComplete.run();
                });
    }

    /**
     * Gets the next available notification ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getNotificationId(NotificationIDCallback callback) {
        notificationRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the highest ID
                            int highestId = 0;
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                int id = doc.getLong("id").intValue();
                                if (id > highestId) {
                                    highestId = id;
                                    callback.onSuccess(highestId + 1);
                                } else {
                                    callback.onSuccess(highestId + 1);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get next notification ID"));

                    }
                });

    }

    /**
     * Gets all future events from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getAllFutureEvents(EventListCallback callback) {
        eventRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Event event = doc.toObject(Event.class);
                                if(event.getEventDate().compareTo(Timestamp.now()) > 0)
                                    events.add(event);
                            }
                            callback.onSuccess(events);


                        }
                        else {
                            callback.onSuccess(new ArrayList<Event>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get Events"));
                    }
                });
    }

    /**
     * Gets all notifications from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getAllNotifications(NotificationListCallback callback) {
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


                        }
                        else {
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

    public void deleteNotification(String id) {
        notificationRef.document(id).delete();
    }

    /**
     * Writes a notification to the database
     * @param notification
     *      Notification to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void writeNotification(Notification notification, DBWriteCallback callback) {
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
    public void updateNotification(Notification notification, DBWriteCallback callback) {
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

    public void clearNotifications(Runnable onComplete) {
        notificationRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(eventRef.document(doc.getId()).delete());
                    }
                    // Wait for all deletes to finish
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> onComplete.run());

                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear events: " + e.getMessage());
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

    public void getNotificationById(int id, NotificationCallback callback) {
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
    public void getNotificationByRecipientId(String recipientId, NotificationListCallback callback) {
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
     * Gets all notifications for a single recipient from the database asynchronously
     * @param deviceId
     *      Recipient device ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void getNotificationByDeviceId(String deviceId, NotificationListCallback callback) {
        notificationRef.whereEqualTo("deviceId", deviceId)
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

    public void getNotificationBySenderId(String senderId, NotificationListCallback callback) {
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

    public void getInvitationById(int id, NotificationCallback callback) {
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
    public void getInvitationByRecipientId(String recipientId, NotificationListCallback callback) {
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

    /**
     * Gets all invitations for a single sender from the database asynchronously
     * @param senderId
     *      ID of the sender to search for
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getInvitationBySenderId(String senderId, NotificationListCallback callback) {
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
    public void getInvitationByEventId(int eventId, NotificationListCallback callback) {
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


    /**
     * Gets the next available log ID
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getLogId(LogIDCallback callback) {
        logRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the highest ID
                        int highestId = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            int id = doc.getLong("id").intValue();
                            if (id > highestId) {
                                highestId = id;
                                callback.onSuccess(highestId + 1);
                            } else {
                                callback.onSuccess(highestId + 1);
                            }
                        }
                    }
                    else {
                        callback.onSuccess(1);
                    }
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onFailure(new DBOpFailed("Failed to get next log ID"));


                        }
                    });

    }

    /**
     * Writes a log to the database
     * @param log
     *      Log to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public void writeLog(Log log, DBWriteCallback callback) {
        logRef.document(String.valueOf(log.getLogId()))
                .set(log)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write log")));
    }

    /**
     * Probably not particularly useful
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getAllLogs(LogListCallback callback) {
        logRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Log> logs = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Log log = doc.toObject(Log.class);
                                logs.add(log);
                            }
                            callback.onSuccess(logs);
                        } else {
                            callback.onSuccess(new ArrayList<Log>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get logs"));


                    }
                });
    }

    /**
     * Deletes a log from the database
     * @param id
     *      ID of the log to delete
     */

    public void deleteLog(String id) {
        logRef.document(id).delete();
    }

    /**
     * Gets all notification logs from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getNotificationLogs(LogListCallback callback) {
        logRef.whereEqualTo("type", LogType.NOTIFICATION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Log> logs = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Log log = doc.toObject(NotificationLog.class);
                                logs.add(log);
                            }
                            callback.onSuccess(logs);
                        } else {
                            callback.onSuccess(new ArrayList<Log>());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get notification logs"));
                    }


                });
    }

    /**
     * Gets all invitation logs from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public void getInvitationLogs(LogListCallback callback) {
        logRef.whereEqualTo("type", LogType.INVITATION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Log> logs = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Log log = doc.toObject(InvitationLog.class);
                                logs.add(log);
                            }
                            callback.onSuccess(logs);
                        } else {
                            callback.onSuccess(new ArrayList<Log>());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get invitation logs"));
                    }

                });

    }










}
