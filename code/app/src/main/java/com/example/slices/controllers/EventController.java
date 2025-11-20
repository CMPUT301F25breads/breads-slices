package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventController {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static CollectionReference eventRef;

    private static EventController instance;

    private EventController() {

    }

    public static EventController getInstance() {
        if (instance == null) {
            instance = new EventController();
        }
        if (eventRef == null) {
            eventRef = db.collection("events");
        }

        return instance;
    }

    public static void setTesting(boolean testing) {
        if (testing) {
            eventRef = db.collection("test_events");
        } else {
            eventRef = db.collection("events");
        }
    }

    /**
     * Gets an event from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     * @param id
     *      Event ID to search for
     */


    public static void getEvent(int id, EventCallback callback) {
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
     * Writes an event to the database asynchronously
     * @param event
     *      Event to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void writeEvent(Event event, DBWriteCallback callback) {
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

    public static void updateEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write event")));

    }

    /**
     * Gets the next available event ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getNewEventId(EventIDCallback callback) {
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
     * Gets all events from the database asynchronously (both past and future)
     * @param callback Callback to call when the operation is complete
     * @Author Sasieni
     */
    public static void getAllEvents(EventListCallback callback) {
        eventRef
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(new DBOpFailed("Failed to get events"))
                );
    }

    /**
     * Gets all entrants or a specific event
     * @param eventId
     *      Event ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrantsForEvent(int eventId, EntrantListCallback callback) {
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
                                callback.onSuccess(new ArrayList<Entrant>());
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
     * @param entrant
     *      user to find events for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {
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
                                if(event.getEntrants().contains(entrant))
                                    events.add(event);
                                else if(event.getWaitlist().getEntrants().contains(entrant))
                                    waitEvents.add(event);
                            }
                            if (events != null) {
                                callback.onSuccess(events, waitEvents);
                            } else {

                                callback.onSuccess(new ArrayList<Event>(), new ArrayList<>());
                            }
                        } else {
                            callback.onFailure(new EventNotFound("No events found for ", entrant.getName()));
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
    public static void deleteEvent(String id) {
        eventRef.document(id).delete();
    }



    /**
     * Clears all events from the database asynchronously: Used for testing
     * @param onComplete
     *      Callback to call when the operation is complete
     */
    public static void clearEvents(Runnable onComplete) {
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
     * Gets all future events from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getAllFutureEvents(EventListCallback callback) {
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
}
