package com.example.slices.controllers;

import android.annotation.SuppressLint;
import android.location.Location;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.interfaces.StringListCallback;
import com.example.slices.models.AsyncBatchExecutor;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.example.slices.models.Image;
import com.example.slices.models.NotificationType;
import com.example.slices.models.SearchSettings;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class EventController {

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static CollectionReference eventRef = db.collection("events");

    private static EventController instance;

    private EventController() {

    }

    /**
     * Sets the testing flag
     * @param testing
     *      True if testing, false otherwise
     */
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
                            Logger.logSystem("Fetched event id=" + id, null);
                            callback.onSuccess(event);
                        } else {
                            Logger.logError("Event not found id=" + id, null);
                            callback.onFailure(new EventNotFound("Event not found", String.valueOf(id)));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logger.logError("Failed to fetch event id=" + id, null);
                        callback.onFailure(new DBOpFailed("Failed to get event"));
                    }
                });

    }

    /**
     * Writes an event to the database asynchronously
     *
     * @param event
     *      Event to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void writeEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Logger.logEventCreate(event.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to write event id=" + event.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write event"));
                });

    }

    /**
     * Updates an event in the database asynchronously
     *
     * @param event
     *      Event to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void updateEvent(Event event, DBWriteCallback callback) {
        eventRef.document(String.valueOf(event.getId()))
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Logger.logEventUpdate(event.getId(), null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to update event id=" + event.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write event"));
                });

    }

    /**
     * Gets the next available event ID
     *
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

                            int highestId = 0;
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                int id = doc.getLong("id").intValue();
                                if (id > highestId) {
                                    highestId = id;
                                }
                            }

                            Logger.logSystem("Generated new event ID=" + (highestId + 1), null);
                            callback.onSuccess(highestId + 1);
                        } else {
                            Logger.logSystem("Generated new event ID=1 (first event)", null);
                            callback.onSuccess(1);
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logger.logError("Failed to generate new event ID", null);
                        callback.onFailure(new DBOpFailed("Failed to get next event ID"));
                    }
                });

    }

    /**
     * Gets all events from the database asynchronously (both past and future)
     *
     * @param callback
     *      Callback to call when the operation is complete
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
                    Logger.logSystem("Fetched all events count=" + events.size(), null);
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to get events", null);
                    callback.onFailure(new DBOpFailed("Failed to get events"));
                });
    }

    /**
     * Gets all entrants or a specific event
     *
     * @param eventId
     *      Event ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrantsForEvent(int eventId, EntrantListCallback callback) {
        eventRef
                .whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Event event = query.getDocuments().get(0).toObject(Event.class);

                        if (event != null && event.getEntrants() != null) {
                            Logger.logSystem("Fetched entrants for event id=" + eventId, null);
                            callback.onSuccess(event.getEntrants());
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        Logger.logError("Event not found while fetching entrants id=" + eventId, null);
                        callback.onFailure(new EventNotFound("Event not found", String.valueOf(eventId)));
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to fetch entrants for event id=" + eventId, null);
                    callback.onFailure(new DBOpFailed("Failed to get entrants for event"));
                });
    }
    /**
     * Gets the waitlist for a specific event.
     *
     * @param eventId
     *      Event ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getWaitlistForEvent(int eventId, EntrantListCallback callback) {
        eventRef
                .whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Event event = query.getDocuments().get(0).toObject(Event.class);
                        if (event != null && event.getWaitlist() != null) {
                            Logger.logSystem("Fetched waitlist for event id=" + eventId, null);
                            callback.onSuccess(event.getWaitlist().getEntrants());
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        Logger.logError("Event not found while fetching waitlist id=" + eventId, null);
                        callback.onFailure(new EventNotFound("Event not found", String.valueOf(eventId)));
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to fetch waitlist for event id=" + eventId, null);
                    callback.onFailure(new DBOpFailed("Failed to get entrants for event"));
                });

    }

    /**
     * Gets all events for a given entrant
     *
     * @param entrant
     *      user to find events for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query q = eventRef.whereGreaterThan("eventInfo.eventDate", Timestamp.now());

        Query eventsQuery = q.whereArrayContains("entrantIds", entrant.getId());
        Query waitlistQuery = q.whereArrayContains("waitlist.entrantIds", entrant.getId());

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

                Logger.logSystem("Fetched future events + waitlist events for entrant id=" + entrant.getId(), null);

                callback.onSuccess(events, waitEvents);

            }).addOnFailureListener(e -> {
                Logger.logError("Failed to fetch waitlist events for entrant id=" + entrant.getId(), null);
                callback.onFailure(new DBOpFailed("Failed to get waitlist events"));
            });

        }).addOnFailureListener(e -> {
            Logger.logError("Failed to fetch events for entrant id=" + entrant.getId(), null);
            callback.onFailure(new DBOpFailed("Failed to get events for entrant"));
        });

    }

    /**
     * Gets all events for a given entrant
     *
     * @param entrant
     *      user to find events for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getAllEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query eventsQuery = eventRef.whereArrayContains("entrantIds", entrant.getId());
        Query waitlistQuery = eventRef.whereArrayContains("waitlist.entrantIds", entrant.getId());

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

                Logger.logSystem("Fetched ALL events (past + future) for entrant id=" + entrant.getId(), null);

                callback.onSuccess(events, waitEvents);

            }).addOnFailureListener(e -> {
                Logger.logError("Failed to fetch waitlist events for entrant id=" + entrant.getId(), null);
                callback.onFailure(new DBOpFailed("Failed to get waitlist events"));
            });

        }).addOnFailureListener(e -> {
            Logger.logError("Failed to fetch events for entrant id=" + entrant.getId(), null);
            callback.onFailure(new DBOpFailed("Failed to get events for entrant"));
        });

    }

    /**
     * Gets all past events for a given entrant
     *
     * @param entrant
     *      user to find events for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getPastEventsForEntrant(Entrant entrant, EntrantEventCallback callback) {

        Query q = eventRef.whereLessThan("eventInfo.eventDate", Timestamp.now());

        Query eventsQuery = q.whereArrayContains("entrantIds", entrant.getId());
        Query waitlistQuery = q.whereArrayContains("waitlist.entrantIds", entrant.getId());

        eventsQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) events.add(event);
            }

            waitlistQuery.get().addOnSuccessListener(waitlistSnapshots -> {
                List<Event> waitEvents = new ArrayList<>();
                for (DocumentSnapshot doc : waitlistSnapshots.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) waitEvents.add(event);
                }

                Logger.logSystem("Fetched past events for entrant id=" + entrant.getId(), null);

                callback.onSuccess(events, waitEvents);

            }).addOnFailureListener(e -> {
                Logger.logError("Failed to fetch past waitlist events for entrant id=" + entrant.getId(), null);
                callback.onFailure(new DBOpFailed("Failed to get waitlist events"));
            });

        }).addOnFailureListener(e -> {
            Logger.logError("Failed to fetch past events for entrant id=" + entrant.getId(), null);
            callback.onFailure(new DBOpFailed("Failed to get events for entrant"));
        });
    }

    /**
     * Gets all events for a given organizer
     * @param id
     *      Organizer ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
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

            Logger.logSystem("Fetched events for organizer id=" + id, null);

        }).addOnFailureListener(e -> {
            Logger.logError("Failed to fetch events for organizer id=" + id, null);
            callback.onFailure(new DBOpFailed("Failed to get events for organizer"));
        });

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

        getEvent(Integer.parseInt(id), new EventCallback() {
            @Override
            public void onSuccess(Event event) {

                Logger.logSystem("Starting delete pipeline for event id=" + id, null);

                List<Entrant> entrants = event.getEntrants();

                if (entrants == null || entrants.isEmpty()) {
                    eventRef.document(id).delete()
                            .addOnSuccessListener(unused -> {
                                Logger.logEventDelete(Integer.parseInt(id), null);
                                verifyDeleteEvent(id, callback);
                            })
                            .addOnFailureListener(e -> {
                                Logger.logError("Failed to delete event id=" + id, null);
                                callback.onFailure(new DBOpFailed("Failed to delete event"));
                            });
                    return;
                }

                List<Entrant> entrantSnapshot = new ArrayList<>(entrants);

                List<Consumer<DBWriteCallback>> removalOps = new ArrayList<>();
                for (Entrant entrant : entrantSnapshot) {
                    removalOps.add(cb -> removeEntrantFromEvent(event, entrant, cb));
                }

                AsyncBatchExecutor.runBatch(removalOps, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {

                        List<Consumer<DBWriteCallback>> notifyOps = new ArrayList<>();

                        for (Entrant entrant : entrantSnapshot) {
                            notifyOps.add(cb -> NotificationManager.sendNotification(
                                    "Event Deleted", "Your event has been deleted", entrant.getId(), event.getId(), cb));
                        }

                        AsyncBatchExecutor.runBatch(notifyOps, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {

                                eventRef.document(id)
                                        .delete()
                                        .addOnSuccessListener(unused -> {
                                            Logger.logEventDelete(Integer.parseInt(id), null);
                                            verifyDeleteEvent(id, callback);
                                        })
                                        .addOnFailureListener(e -> {
                                            Logger.logError("Failed to delete event id=" + id, null);
                                            callback.onFailure(new DBOpFailed("Failed to delete event"));
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed to notify entrants during event deletion id=" + id, null);
                                callback.onFailure(new DBOpFailed("Failed to notify all entrants"));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to remove entrants from event id=" + id, null);
                        callback.onFailure(new DBOpFailed("Failed to remove all entrants from the event"));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Event not found during delete pipeline id=" + id, null);
                callback.onFailure(new EventNotFound("Event not found", id));
            }
        });
    }

    /**
     * Creates an event from the given parameters
     * @param name
     *      Event name
     * @param description
     *      Event description
     * @param location
     *      Event location
     * @param guidelines
     *      Event guidelines
     * @param imgUrl
     *      Event image URL
     * @param eventDate
     *      Event date
     * @param regStart
     *      Event registration start
     * @param regEnd
     *      Event registration end
     * @param maxEntrants
     *      Event max entrants
     * @param maxWaiting
     *      Event max waiting list size
     * @param entrantLoc
     *      Event entrant location
     * @param entrantDist
     *      Event entrant distance
     * @param organizerID
     *      Event organizer ID
     * @param callback
     *      Callback invoked when the event is created
     */
    public static void createEvent(String name, String description, String address, Location location, String guidelines, String imgUrl,
                                   Timestamp eventDate, Timestamp regStart, Timestamp regEnd, int maxEntrants,
                                   int maxWaiting, boolean entrantLoc, String entrantDist, int organizerID, Image image, EventCallback callback) {
        try {
            verifyEventTimes(regStart, regEnd, eventDate);

            getNewEventId(new EventIDCallback() {
                @Override
                public void onSuccess(int id) {

                    Event event = new Event(name, description, address, guidelines, imgUrl,
                            eventDate, regStart, regEnd, maxEntrants, maxWaiting, entrantLoc, entrantDist, id, organizerID, image);
                    
                    // Set location if provided
                    if (location != null) {
                        event.getEventInfo().setLocation(location);
                        Logger.logSystem("Event created with location: lat=" + location.getLatitude() + 
                                       ", lon=" + location.getLongitude() + ", eventId=" + id, null);
                    } else if (entrantLoc) {
                        Logger.logSystem("WARNING: Geolocation event created without location, eventId=" + id, null);
                    } else {
                        Logger.logSystem("Event created without geolocation, eventId=" + id, null);
                    }
                    
                    writeEvent(event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            if (location != null) {
                                Logger.logSystem("Event location stored: lat=" + event.getEventInfo().getEventLatitude() + 
                                               ", lon=" + event.getEventInfo().getEventLongitude() + ", eventId=" + id, null);
                            }
                            Logger.logEventCreate(id, null);
                            callback.onSuccess(event);

                        }
                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Failed to create event id=" + id, null);
                            callback.onFailure(e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Logger.logError("Failed generating ID for new event", null);
                    callback.onFailure(e);
                }
            });
        }
        catch(IllegalArgumentException e) {
            Logger.logError("Event creation failed date validation", null);
            callback.onFailure(e);
        }
    }

    /**
     * Creates an event from an EventInfo object.
     * @param eventInfo
     *      EventInfo containing event fields
     * @param callback
     *      Callback invoked when the event is created
     */
    public static void createEvent(EventInfo eventInfo, EventCallback callback) {

        getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {

                eventInfo.setId(id);

                Event event = new Event(eventInfo);

                // Log geolocation status
                if (eventInfo.getEntrantLoc() && eventInfo.getEventLatitude() != null && eventInfo.getEventLongitude() != null) {
                    Logger.logSystem("Event created with geolocation: lat=" + eventInfo.getEventLatitude() + 
                                   ", lon=" + eventInfo.getEventLongitude() + ", eventId=" + id, null);
                } else if (eventInfo.getEntrantLoc()) {
                    Logger.logSystem("WARNING: Geolocation event created without coordinates, eventId=" + id, null);
                } else {
                    Logger.logSystem("Event created without geolocation, eventId=" + id, null);
                }

                writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        if (eventInfo.getEntrantLoc() && eventInfo.getEventLatitude() != null) {
                            Logger.logSystem("Event location stored: lat=" + eventInfo.getEventLatitude() + 
                                           ", lon=" + eventInfo.getEventLongitude() + ", eventId=" + id, null);
                        }
                        Logger.logEventCreate(id, null);
                        callback.onSuccess(event);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to create event id=" + id, null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed generating ID for new event", null);
                callback.onFailure(e);
            }
        });
    }



    /**
     * Verifies that the event times are valid
     * @param regStart
     *      Event registration start
     * @param regEnd
     *      Event registration end
     * @param eventDate
     *      Event date
     */
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

    /**
     * Creates an event from an EventInfo object without checking the event times
     * @param eventInfo
     *      EventInfo containing event fields
     * @param callback
     *      Callback invoked when the event is created
     */
    public static void createEventNoCheck(EventInfo eventInfo, EventCallback callback) {
        getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                eventInfo.setId(id);
                Event event = new Event(eventInfo);
                writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        Logger.logEventCreate(id, null);
                        callback.onSuccess(event);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to create event id=" + id, null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed generating ID for new event", null);
                callback.onFailure(e);

            }
        });
    }

    /**
     * Updates an event in the database asynchronously
     * @param event
     *      Event to update in the database
     * @param eventInfo
     *      EventInfo containing event fields
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void updateEventInfo(Event event, EventInfo eventInfo, DBWriteCallback callback) {
        event.setEventInfo(eventInfo);
        Logger.logEventUpdate(event.getId(), null);
        updateEvent(event, callback);
    }

    /**
     * Removes an entrant from an event
     * @param event
     *      Event to remove entrant from
     * @param entrant
     *      Entrant to remove
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void removeEntrantFromEvent(Event event, Entrant entrant, DBWriteCallback callback) {
        boolean removed = event.removeEntrant(entrant);
        if (removed) {
            Logger.logEntrantLeft(entrant.getId(), event.getId(), null);
            updateEvent(event, callback);
        }
        else {
            Logger.logError("Entrant not in event id=" + event.getId() + ", entrant=" + entrant.getId(), null);
            callback.onFailure(new Exception("Entrant not in event"));
        }
    }

    /**
     * Removes an entrant from a waitlist
     * @param event
     *      Event to remove entrant from
     * @param entrant
     *      Entrant to remove
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void removeEntrantFromWaitlist(Event event, Entrant entrant, DBWriteCallback callback) {
        boolean removed = event.removeEntrantFromWaitlist(entrant);
        if (removed) {
            Logger.logWaitlistModified("Removed from waitlist", event.getId(), entrant.getId(), null);
            updateEvent(event, callback);
        }
        else {
            Logger.logError("Entrant not in waitlist event id=" + event.getId() + ", entrant=" + entrant.getId(), null);
            callback.onFailure(new Exception("Entrant not in event"));
        }
    }

    /**
     * Adds an entrant to a waitlist
     * @param event
     *      Event to add entrant to
     * @param entrant
     *      Entrant to add
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void addEntrantToWaitlist(Event event, Entrant entrant, DBWriteCallback callback) {
        if (event.getEntrants() != null && event.getEntrants().contains(entrant)) {
            Logger.logError("Attempted to add entrant already in event to waitlist event id=" + event.getId(), null);
            callback.onFailure(new Exception("Entrant already in event"));
            return;
        }

        if (event.getWaitlist() == null) {
            Logger.logError("Waitlist null for event id=" + event.getId(), null);
            callback.onFailure(new Exception("Event waitlist is not initialized"));
            return;
        }


        try {
            boolean added = event.addEntrantToWaitlist(entrant);
            if (added) {
                // Increment currentEntrants after successful add
                Logger.logWaitlistModified("Added to waitlist", event.getId(), entrant.getId(), null);
                updateEvent(event, callback);
            } else {
                Logger.logError("Failed to add entrant to waitlist event id=" + event.getId(), null);
                callback.onFailure(new Exception("Failed to add entrant to waitlist"));
            }
        } catch (Exception e) {
            Logger.logError("Exception adding entrant to waitlist event id=" + event.getId(), null);
            callback.onFailure(e);
        }
    }

    public static void addEntrantToWaitlist(Event event, Entrant entrant, Location loc, DBWriteCallback callback ) {
        //Run a check for the locations
        if (!checkLocs(event, loc)) {
            Logger.logError("Location not in event id=" + event.getId(), null);
            callback.onFailure(new Exception("This event isn't available in your area"));
            return;
        }
        
        if (event.getEntrants() != null && event.getEntrants().contains(entrant)) {
            Logger.logError("Attempted to add entrant already in event to waitlist event id=" + event.getId(), null);
            callback.onFailure(new Exception("Entrant already in event"));
            return;
        }

        if (event.getWaitlist() == null) {
            Logger.logError("Waitlist null for event id=" + event.getId(), null);
            callback.onFailure(new Exception("Event waitlist is not initialized"));
            return;
        }

        try {
            // Add entrant with location
            boolean added = event.addEntrantToWaitlist(entrant, loc);
            if (added) {
                // Increment currentEntrants after successful add
                Logger.logWaitlistModified("Added to waitlist with location", event.getId(), entrant.getId(), null);
                updateEvent(event, callback);
            } else {
                Logger.logError("Failed to add entrant to waitlist event id=" + event.getId(), null);
                callback.onFailure(new Exception("Failed to add entrant to waitlist"));
            }
        } catch (Exception e) {
            Logger.logError("Exception adding entrant to waitlist event id=" + event.getId(), null);
            callback.onFailure(e);
        }
    }


    private static boolean checkLocs(Event event, Location entrantLoc) {
        EventInfo info = event.getEventInfo();
        
        // Is location required?
        if (!info.getEntrantLoc()) {
            return true; // No location required, allow join
        }
        
        // Does the event have a location?
        Location eventLoc = info.getLocation();
        if (eventLoc == null) {
            Logger.logError("Geolocation event has no location set, event id=" + event.getId(), null);
            return false; // Event requires location but doesn't have one
        }
        
        // Did the entrant provide a location?
        if (entrantLoc == null) {
            Logger.logError("Geolocation event requires entrant location but none provided, event id=" + event.getId(), null);
            return false; // Location required but not provided
        }
        
        // Parse max distance
        int maxDistanceMeters;
        try {
            maxDistanceMeters = Integer.parseInt(info.getEntrantDist());
        } catch (Exception e) {
            Logger.logError("Invalid distance format for event id=" + event.getId(), null);
            return false;
        }
        
        // Calculate distance between event and entrant
        float distance = eventLoc.distanceTo(entrantLoc);
        
        Logger.logSystem("Distance check: event id=" + event.getId() + 
                       ", distance=" + distance + "m, max=" + maxDistanceMeters + "m", null);
        
        if (distance <= maxDistanceMeters) {
            Logger.logSystem("Entrant within range, allowing join", null);
            return true;
        } else {
            Logger.logSystem("Entrant too far (" + distance + "m > " + maxDistanceMeters + "m), rejecting join", null);
            return false;
        }
    }



    /**
     * Adds an entrant to an event
     * @param event
     *      Event to add entrant to
     * @param entrant
     *      Entrant to add
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void addEntrantToEvent(Event event, Entrant entrant, DBWriteCallback callback) {
        boolean added = event.addEntrant(entrant);
        if (added) {
            Logger.logEntrantJoin(entrant.getId(), event.getId(), null);
            updateEvent(event, callback);
        } else {
            Logger.logError("Entrant already in event id=" + event.getId() + ", entrant=" + entrant.getId(), null);
            callback.onFailure(new Exception("Entrant already in event"));
        }
    }

    public static void removeEntrantsFromEvent(Event event, List<Entrant> entrants, DBWriteCallback callback) {
        boolean failFlag = true;
        for (Entrant entrant : entrants) {
            boolean success = event.removeEntrant(entrant);
            if (!success) {
                failFlag = false;
            } else {
                Logger.logEntrantLeft(entrant.getId(), event.getId(), null);
            }
        }
        if (failFlag) {
            updateEvent(event, callback);
        }
        else {
            Logger.logError("One or more entrants not in event id=" + event.getId(), null);
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
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> {
                                Logger.logSystem("Cleared all events", null);
                                onComplete.run();
                            });

                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to clear events", null);
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
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        ArrayList<Event> events = new ArrayList<>();
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }

                        Logger.logSystem("Fetched all future events count=" + events.size(), null);
                        callback.onSuccess(events);

                    } else {
                        callback.onSuccess(new ArrayList<Event>());
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to fetch future events", null);
                    callback.onFailure(new DBOpFailed("Failed to get Events"));
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

        // Date filters
        if (search.getAvailStart() != null)
            q = q.whereGreaterThanOrEqualTo("eventInfo.eventDate", search.getAvailStart());

        if (search.getAvailEnd() != null)
            q = q.whereLessThan("eventInfo.eventDate", search.getAvailEnd());

        // Location filter
        if (search.getAddress() != null && !search.getAddress().trim().isEmpty())
            q = q.whereEqualTo("eventInfo.address", search.getAddress().trim());

        q.get().addOnSuccessListener(query -> {
            ArrayList<Event> events = new ArrayList<>();

            for (DocumentSnapshot doc : query.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event == null)
                    continue;

                boolean include = true;

                if (search.getName() != null && !search.getName().isEmpty()) {
                    String eventName = event.getEventInfo().getName().toLowerCase();
                    String searchName = search.getName().toLowerCase();
                    if (!eventName.contains(searchName)) {
                        include = false;
                    }
                }
                
                if (search.isEnrolled()) {
                    int id = search.getId();

                    boolean inEntrants = event.getEntrants() != null &&
                            event.getEntrantIds().contains(id);

                    boolean inWaitlist = event.getWaitlist() != null &&
                            event.getWaitlist().getEntrantIds() != null &&
                            event.getWaitlist().getEntrantIds().contains(id);

                    if (!inEntrants && !inWaitlist) {
                        include = false;
                    }
                }

                if (include) {
                    events.add(event);
                }
            }

            Logger.logSystem("QueryEvents returned " + events.size() + " results", null);
            callback.onSuccess(events);

        }).addOnFailureListener(e -> {
            Logger.logError("Failed to query events", null);
            callback.onFailure(new DBOpFailed("Failed to get Events"));
        });
    }

    private static void verifyDeleteEvent(String id, DBWriteCallback callback) {
        eventRef.document(id).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onSuccess();
                    } else {
                        Logger.logError("Event still exists after deletion id=" + id, null);
                        callback.onFailure(new DBOpFailed("Event still in database"));
                    }
                });
    }

    public static void doLottery(Event event, DBWriteCallback callback) {

        int spots = event.getEventInfo().getMaxEntrants() - event.getEntrants().size();
        if (spots <= 0) {
            Logger.logError("Lottery failed: no spots available event id=" + event.getId(), null);
            callback.onFailure(new Exception("No spots available"));
            return;
        }

        getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    Logger.logError("Lottery failed: no entrants in waitlist event id=" + event.getId(), null);
                    callback.onFailure(new Exception("No entrants in waitlist"));
                    return;
                }

                if (entrants.size() <= spots) {
                    notifyWinners(entrants, event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.logLotteryRun(event.getId(), null);
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Lottery notifyWinners failed event id=" + event.getId(), null);
                            callback.onFailure(e);
                        }
                    });
                    return;
                }

                List<Entrant> pool = new ArrayList<>(entrants);
                List<Entrant> winners = new ArrayList<>();

                for (int i = 0; i < spots; i++) {
                    int randomIndex = (int)(Math.random() * pool.size());
                    winners.add(pool.remove(randomIndex));
                }


                notifyWinners(winners, event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        notifyLosers(pool, event, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logLotteryRun(event.getId(), null);
                                callback.onSuccess();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Lottery notify losers failed event id=" + event.getId(), null);
                                callback.onFailure(e);
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Lottery notify winners failed event id=" + event.getId(), null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Lottery failed fetching waitlist event id=" + event.getId(), null);
                callback.onFailure(e);
            }
        });
    }

    private static void notifyWinners(List<Entrant> winners, Event event, DBWriteCallback callback) {
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
        String title = "Sorry!";
        String body = "You have lost the lottery for " + event.getEventInfo().getName() + "!";
        int sender = event.getEventInfo().getOrganizerID();
        List<Integer> recipients = new ArrayList<>();
        for (Entrant e : losers) {
            recipients.add(e.getId());
        }
        NotificationManager.sendBulkNotification(title, body, recipients, sender, NotificationType.NOT_SELECTED, callback);
    }

    public static void addEntrantsToEvent(Event event, List<Entrant> entrants, DBWriteCallback callback) {
        List<Consumer<DBWriteCallback>> ops = new ArrayList<>();
        for (Entrant e : entrants) {
            ops.add(cb -> addEntrantToEvent(event, e, cb));
            // removeEntrantFromWaitlist already decrements currentEntrants
            ops.add(cb -> removeEntrantFromWaitlist(event, e, cb));
        }
        AsyncBatchExecutor.runBatch(ops, callback);
    }

    public static void getAllImages(StringListCallback callback) {
        eventRef.get()
                .addOnSuccessListener(query -> {
                    List<String> images = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments())
                        images.add(doc.getString("imageUrl"));
                    callback.onSuccess(images);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void removeImage(Event e, DBWriteCallback callback) {
        e.getEventInfo().setImageUrl(null);
        eventRef.document(String.valueOf(e.getId()))
                .set(e)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e1 -> callback.onFailure(new Exception("Failed to remove image")));
    }


}

