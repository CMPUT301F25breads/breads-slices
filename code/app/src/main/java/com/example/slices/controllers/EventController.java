package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.AsyncBatchExecutor;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.example.slices.models.SearchSettings;
import com.example.slices.testing.DebugLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EventController {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static CollectionReference eventRef = db.collection("events");

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
     *
     * @param callback Callback to call when the operation is complete
     * @param id       Event ID to search for
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
     *
     * @param event    Event to write to the database
     * @param callback Callback to call when the operation is complete
     */

    public static void writeEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write event")));

    }

    /**
     * Updates an event in the database asynchronously
     *
     * @param event    Event to update in the database
     * @param callback Callback to call when the operation is complete
     */

    public static void updateEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write event")));

    }

    /**
     * Gets the next available event ID
     *
     * @param callback Callback to call when the operation is complete
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
     *
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
     *
     * @param eventId  Event ID to search for
     * @param callback Callback to call when the operation is complete
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

    public static void getWaitlistForEvent(int eventId, EntrantListCallback callback) {
        eventRef
                .whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Event event = doc.toObject(Event.class);
                            if (event != null && event.getWaitlist() != null) {
                                callback.onSuccess(event.getWaitlist().getEntrants());
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
     *
     * @param entrant  user to find events for
     * @param callback Callback to call when the operation is complete
     */
    public static void getEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query q = eventRef.whereGreaterThan("eventInfo.eventDate", Timestamp.now());

        Query eventsQuery = q.whereArrayContains("entrants", entrant);
        Query waitlistQuery = q.whereArrayContains("waitlist.entrants", entrant);

        eventsQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);

                if (event != null)
                    events.add(event);
            }


            waitlistQuery.get().addOnSuccessListener(waitlistSnapshots -> {
                List<Event> waitEvents = new ArrayList<>();
                for (DocumentSnapshot doc : waitlistSnapshots.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null)
                        waitEvents.add(event);
                }

                if(!events.isEmpty() || !waitEvents.isEmpty())
                    callback.onSuccess(events, waitEvents);
                else
                    callback.onSuccess(new ArrayList<>(), new ArrayList<>());



            }).addOnFailureListener(e ->
                    callback.onFailure(new DBOpFailed("Failed to get waitlist events"))
            );

        }).addOnFailureListener(e ->
                callback.onFailure(new DBOpFailed("Failed to get events for entrant"))
        );


    }


    /**
     * Gets all events for a given entrant
     *
     * @param entrant  user to find events for
     * @param callback Callback to call when the operation is complete
     */
    public static void getAllEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query eventsQuery = eventRef.whereArrayContains("entrants", entrant);
        Query waitlistQuery = eventRef.whereArrayContains("waitlist.entrants", entrant);

        eventsQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);

                if (event != null)
                    events.add(event);
            }


            waitlistQuery.get().addOnSuccessListener(waitlistSnapshots -> {
                List<Event> waitEvents = new ArrayList<>();
                for (DocumentSnapshot doc : waitlistSnapshots.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null)
                        waitEvents.add(event);
                }

                if(!events.isEmpty() || !waitEvents.isEmpty())
                    callback.onSuccess(events, waitEvents);
                else
                    callback.onSuccess(new ArrayList<>(), new ArrayList<>());



            }).addOnFailureListener(e ->
                    callback.onFailure(new DBOpFailed("Failed to get waitlist events"))
            );

        }).addOnFailureListener(e ->
                callback.onFailure(new DBOpFailed("Failed to get events for entrant"))
        );
    }


    /**
     * Gets all events for a given entrant
     *
     * @param entrant  user to find events for
     * @param callback Callback to call when the operation is complete
     */
    public static void getPastEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query q = eventRef.whereLessThan("eventInfo.eventDate", Timestamp.now());

        Query eventsQuery = q.whereArrayContains("entrants", entrant);
        Query waitlistQuery = q.whereArrayContains("waitlist.entrants", entrant);

        // First fetch main events

        eventsQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) events.add(event);
            }

            // Then fetch waitlist events
            waitlistQuery.get().addOnSuccessListener(waitlistSnapshots -> {
                List<Event> waitEvents = new ArrayList<>();
                for (DocumentSnapshot doc : waitlistSnapshots.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) waitEvents.add(event);
                }

                callback.onSuccess(events, waitEvents);


            }).addOnFailureListener(e ->
                    callback.onFailure(new DBOpFailed("Failed to get waitlist events"))
            );

        }).addOnFailureListener(e ->
                callback.onFailure(new DBOpFailed("Failed to get events for entrant"))
        );
    }

    public static void getEventsForOrganizer(int id, EventListCallback callback) {
        Query query = eventRef
                .whereEqualTo("eventInfo.organizerID", id);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null)
                    events.add(event);
                callback.onSuccess(events);
            }

        }).addOnFailureListener(e ->
                callback.onFailure(new DBOpFailed("Failed to get events for organizer"))
        );

    }

    /**
     * This method deletes an event from the database
     * 1. Gets the event
     * 2. Gets all entrants
     * 3. Removes all entrants from the event
     * 4. Notifies all entrants
     * 5. Deletes the event
     *
     * @param id
     *      ID of the event to delete
     * @param callback
     *      Callback invoked when the delete completes
     */
    public static void deleteEvent(String id, DBWriteCallback callback) {

        // First get the event
        getEvent(Integer.parseInt(id), new EventCallback() {
            @Override
            public void onSuccess(Event event) {

                // Now we know the event exists — get all entrants
                List<Entrant> entrants = event.getEntrants();

                // If event has no entrants, delete immediately
                if (entrants == null || entrants.isEmpty()) {
                    eventRef.document(id).delete()
                            .addOnSuccessListener(unused -> verifyDeleteEvent(id, callback))
                            .addOnFailureListener(e ->
                                    callback.onFailure(new DBOpFailed("Failed to delete event")));
                    return;
                }

                // Make a copy of the list of entrants
                List<Entrant> entrantSnapshot = new ArrayList<>(entrants);
                // Remove all entrants from the event
                List<Consumer<DBWriteCallback>> removalOps = new ArrayList<>();
                for (Entrant entrant : entrantSnapshot) {
                    removalOps.add(cb -> removeEntrantFromEvent(event, entrant, cb));
                }

                AsyncBatchExecutor.runBatch(removalOps, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        //Create notificaiton operations
                        List<Consumer<DBWriteCallback>> notifyOps = new ArrayList<>();

                        for (Entrant entrant : entrantSnapshot) {
                            notifyOps.add(cb -> NotificationManager.sendNotification(
                                    "Event Deleted",
                                    "Your event has been deleted",
                                    entrant.getId(),
                                    event.getId(),
                                    cb
                            ));
                        }

                        // Send notifications
                        AsyncBatchExecutor.runBatch(notifyOps, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                // Delete the event
                                eventRef.document(id)
                                        .delete()
                                        .addOnSuccessListener(unused -> verifyDeleteEvent(id, callback))
                                        .addOnFailureListener(e ->
                                                callback.onFailure(new DBOpFailed("Failed to delete event")));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(new DBOpFailed("Failed to notify all entrants"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to remove all entrants from the event"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new EventNotFound("Event not found", id));
            }
        });
    }


    public static void createEvent(String name, String description, String location, String guidelines, String imgUrl,
                                   Timestamp eventDate, Timestamp regStart, Timestamp regEnd, int maxEntrants,
                                   int maxWaiting, boolean entrantLoc, String entrantDist, int organizerID, EventCallback callback) {
        try {
            verifyEventTimes(regStart, regEnd, eventDate);
            //First get an id
            getNewEventId(new EventIDCallback() {
                @Override
                public void onSuccess(int id) {

                    Event event = new Event(name, description, location, guidelines, imgUrl,
                            eventDate, regStart, regEnd, maxEntrants, maxWaiting, entrantLoc, entrantDist, id, organizerID);
                    writeEvent(event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(event);

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
        catch(IllegalArgumentException e) {
            callback.onFailure(e);
        }
    }
    /**
     * Creates an event from an EventInfo object.
     *
     * @param eventInfo EventInfo containing event fields
     * @param callback  Callback invoked when the event is created
     */
    public static void createEvent(EventInfo eventInfo, EventCallback callback) {

        // First get an id
        getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {

                eventInfo.setId(id);

                // Build the event using the new constructor
                Event event = new Event(eventInfo);

                writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(event);
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


    private static void verifyEventTimes(Timestamp regStart, Timestamp regEnd, Timestamp eventDate) {
        if (eventDate.compareTo(regStart) < 0) {
            throw new IllegalArgumentException("Event date must be after registration start");
        }
        if (eventDate.compareTo(regEnd) < 0) {
            throw new IllegalArgumentException("Event date must be after registration end");
        }
        if (regStart.compareTo(regEnd) > 0) {
            throw new IllegalArgumentException("Registration start must be before registration end");
        }
        //get now
        Timestamp now = Timestamp.now();
        if (now.compareTo(regStart) > 0) {
            throw new IllegalArgumentException("Registration start must be in the future");
        }
        if (now.compareTo(regEnd) > 0) {
            throw new IllegalArgumentException("Registration end must be in the future");
        }
        if (now.compareTo(eventDate) > 0) {
            throw new IllegalArgumentException("Event date must be in the future");
        }
    }

    public static void createEventNoCheck(EventInfo eventInfo, EventCallback callback) {
        //First get an id
        getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                eventInfo.setId(id);
                Event event = new Event(eventInfo);
                writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(event);
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

    public static void updateEventInfo(Event event, EventInfo eventInfo, DBWriteCallback callback) {
        event.setEventInfo(eventInfo);
        updateEvent(event, callback);
    }

    public static void removeEntrantFromEvent(Event event, Entrant entrant, DBWriteCallback callback) {
        //Remove the entrant from the event
        boolean removed = event.removeEntrant(entrant);
        if (removed) {
            //Write to database
            updateEvent(event, callback);
        }
        else {
            callback.onFailure(new Exception("Entrant not in event"));
        }
    }

    public static void removeEntrantFromWaitlist(Event event, Entrant entrant, DBWriteCallback callback) {
        //Remove the entrant from the event
        boolean removed = event.removeEntrantFromWaitlist(entrant);
        if (removed) {
            //Write to database
            updateEvent(event, callback);
        }
        else {
            callback.onFailure(new Exception("Entrant not in event"));
        }
    }

    public static void addEntrantToWaitlist(Event event, Entrant entrant, DBWriteCallback callback) {
        //First make sure that the entrant is not already in the event
        if (event.getEntrants() != null && event.getEntrants().contains(entrant)) {
            callback.onFailure(new Exception("Entrant already in event"));
            return;
        }
        
        // Safety check: ensure waitlist exists
        if (event.getWaitlist() == null) {
            callback.onFailure(new Exception("Event waitlist is not initialized"));
            return;
        }
        
        try {
            //Add the entrant to the waitlist
            boolean added = event.addEntrantToWaitlist(entrant);
            if (added) {
                //Write to database
                updateEvent(event, callback);
            } else {
                callback.onFailure(new Exception("Failed to add entrant to waitlist"));
            }
        } catch (Exception e) {
            // Catch any exceptions thrown by addEntrantToWaitlist (WaitlistFull, DuplicateEntry, etc.)
            callback.onFailure(e);
        }
    }

    public static void addEntrantToEvent(Event event, Entrant entrant, DBWriteCallback callback) {
        //Add the entrant to the event
        boolean added = event.addEntrant(entrant);
        if (added) {
            //Write to database
            updateEvent(event, callback);
        } else {
            callback.onFailure(new Exception("Entrant already in event"));
        }
    }



    public static void removeEntrantsFromEvent(Event event, List<Entrant> entrants, DBWriteCallback callback) {
        //Remove the entrants from the event
        boolean failFlag = true;
        for (Entrant entrant : entrants) {
            boolean success = event.removeEntrant(entrant);
            if (!success) {
                failFlag = false;
            }
        }
        if (failFlag) {
            //Write to database
            updateEvent(event, callback);
        }
        else {
            callback.onFailure(new Exception("Entrant not in event"));
        }
    }

    /**
     * Testing method that creates some event times
     * @return
     *      List of event times
     */
    public static List<Timestamp> getTestEventTimes() {
        List<Timestamp> times = new ArrayList<>();

        long now = System.currentTimeMillis();

        long regStartMs = now + 60_000;   // 1 min from now
        long regEndMs   = regStartMs + 60_000;  // +1 minute
        long eventMs    = regEndMs + 60_000;    // +1 minute

        times.add(new Timestamp(regStartMs / 1000, 0));
        times.add(new Timestamp(regEndMs / 1000, 0));
        times.add(new Timestamp(eventMs   / 1000, 0));

        return times;
    }




    /**
     * Clears all events from the database asynchronously: Used for testing
     *
     * @param onComplete Callback to call when the operation is complete
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
     *
     * @param callback Callback to call when the operation is complete
     */
    public static void getAllFutureEvents(EventListCallback callback) {

        eventRef.whereGreaterThan("eventInfo.eventDate", Timestamp.now())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Event event = doc.toObject(Event.class);
                                events.add(event);
                            }
                            callback.onSuccess(events);


                        } else {
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
     * Queries the Firestore and gets all event that match the parameters
     * inputted from the SearchSettings asynchronously
     * @param search
     *      SearchSettings object that contains all desired parameters
     * @param callback
     *      Callback to call when the operation is complete
     * @author
     *      Brad
     */
    public static void queryEvents(SearchSettings search, EventListCallback callback) {
        Query q = eventRef.whereGreaterThanOrEqualTo("eventInfo.eventDate", Timestamp.now());


        if (search.getAvailStart() != null)
            q = q.whereGreaterThanOrEqualTo("eventInfo.eventDate", search.getAvailStart());

        if (search.getAvailEnd() != null)
            q = q.whereLessThanOrEqualTo("eventInfo.eventDate", search.getAvailEnd());

        if (search.getLoc() != null && !search.getLoc().trim().isEmpty())
            q = q.whereEqualTo("eventInfo.location", search.getLoc());

        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Event event = doc.toObject(Event.class);
                                if (event == null)
                                    continue;

                                boolean matchesName = true;

                                if (search.getName() != null && !search.getName().trim().isEmpty()) {
                                    String filter = search.getName().toLowerCase();
                                    String eventName = event.getEventInfo().getName().toLowerCase();
                                    matchesName = eventName.contains(filter);
                                }

                                if (matchesName)
                                    events.add(event);
                            }
                            callback.onSuccess(events);

                        } else {
                            callback.onSuccess(new ArrayList<>());
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

    private static void verifyDeleteEvent(String id, DBWriteCallback callback) {
        //Check if event is in the database
        eventRef.document(id).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new DBOpFailed("Event still in database"));
                    }
                });

    }

    public static void doLottery(Event event, DBWriteCallback callback) {
        //Get the number of available spots
        int spots = event.getEventInfo().getMaxEntrants() - event.getEntrants().size();
        if (spots <= 0) {
            callback.onFailure(new Exception("No spots available"));
            return;
        }

        //First get all of the entrants from the waitlist
        getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    callback.onFailure(new Exception("No entrants in waitlist"));
                    return;
                }

                //If everyone fits, no lottery required
                if (entrants.size() <= spots) {
                    addEntrantsToEvent(event, entrants, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                    return;
                }
                //More entrants than spots: run a lottery
                List<Entrant> pool = new ArrayList<>(entrants); //work on a copy
                List<Entrant> winners = new ArrayList<>();

                for (int i = 0; i < spots; i++) {
                    int randomIndex = (int)(Math.random() * pool.size());
                    winners.add(pool.remove(randomIndex));
                }

                addEntrantsToEvent(event, winners, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        //Notify all winners and losers
                        notifyWinners(winners, event, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                notifyLosers(pool, event, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        DebugLogger.d("E", "Notify Losers failed");
                                        callback.onFailure(e);
                                    }
                                });
                            }
                            @Override
                            public void onFailure(Exception e) {
                                DebugLogger.d("E", "Notify winners failed");
                                callback.onFailure(e);
                            }});

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

    private static void notifyWinners(List<Entrant> winners, Event event, DBWriteCallback callback) {
        //Setup message
        String title = "Congratulations!";
        String body = "You have won the lottery for " + event.getEventInfo().getName() + "!";
        int sender = event.getEventInfo().getOrganizerID();
        List<Integer> recipients = new ArrayList<>();
        for (Entrant e : winners) {
            recipients.add(e.getId());
        }
        NotificationManager.sendBulkInvitation(title, body, recipients, sender, event.getId(), callback);
    }

    private static void notifyLosers(List<Entrant> losers, Event event, DBWriteCallback callback) {
        //Setup message
        String title = "Sorry!";
        String body = "You have lost the lottery for " + event.getEventInfo().getName() + "!";
        int sender = event.getEventInfo().getOrganizerID();
        List<Integer> recipients = new ArrayList<>();
        for (Entrant e : losers) {
            recipients.add(e.getId());
        }
        NotificationManager.sendBulkNotification(title, body, recipients, sender, callback);
    }

    public static void addEntrantsToEvent(Event event, List<Entrant> entrants, DBWriteCallback callback) {
        //Build batch operations
        List<Consumer<DBWriteCallback>> ops = new ArrayList<>();
        for (Entrant e : entrants) {
            ops.add(cb -> addEntrantToEvent(event, e, cb));
            ops.add(cb -> removeEntrantFromWaitlist(event, e, cb));
        }
        //Execute everything
        AsyncBatchExecutor.runBatch(ops, callback);
    }




}
