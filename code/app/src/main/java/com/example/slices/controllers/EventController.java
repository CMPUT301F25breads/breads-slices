package com.example.slices.controllers;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

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
import com.example.slices.controllers.ImageController;
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
import java.util.function.Consumer;

/**
 * Controller class for the event model
 * @author Ryan Haubrich
 * @version 1.0
 */

public class EventController {
    /**
     * Reference to the database
     */
    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Reference to the events collection in the database
     */
    private static CollectionReference eventRef = db.collection("events");

    /**
     * Private constructor to prevent instantiation
     */
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

                // Delete event image if it exists
                if(event.getEventInfo().getImage() != null) {
                    ImageController.deleteImage(event.getEventInfo().getImage().getPath(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Image Controller", "Successfully deleted image for event");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Failed to delete image for event id: " + id, null);
                            // Log error but don't fail the entire deletion - continue with event deletion
                            Logger.logSystem("Continuing with event deletion despite image deletion failure", null);
                        }
                    });
                }

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
    public static void createEvent(Event event, EventCallback callback) {
        if (event == null || event.getEventInfo() == null) {
            callback.onFailure(new IllegalArgumentException("Event or EventInfo is null"));
            return;
        }
        createEvent(event.getEventInfo(), callback);
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
        if (regStart.compareTo(regEnd) > 0) {
            throw new IllegalArgumentException("Registration start must be before registration end");
        }
        // Registration period must not extend past the event date
        if (regEnd.compareTo(eventDate) > 0) {
            throw new IllegalArgumentException("Registration end must be on or before the event date");
        }
        // Event must occur after registration opens
        if (eventDate.compareTo(regStart) < 0) {
            throw new IllegalArgumentException("Event date must be after registration start");
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
        // Disallow join if registration period has ended
        if (event.getEventInfo() != null && event.getEventInfo().getRegEnd() != null) {
            if (Timestamp.now().compareTo(event.getEventInfo().getRegEnd()) > 0) {
                Logger.logError("Attempt to join waitlist after registration closed event id=" + event.getId(), null);
                callback.onFailure(new Exception("Registration period has ended"));
                return;
            }
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
            boolean added = event.addEntrantToWaitlist(entrant);
            if (added) {
                // Remove from cancelled list if they were previously cancelled
                List<Integer> cancelledIds = event.getCancelledIds();
                if (cancelledIds != null && cancelledIds.contains(entrant.getId())) {
                    cancelledIds.remove(Integer.valueOf(entrant.getId()));
                    Logger.logSystem("Removed entrant from cancelled list on rejoin: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                }
                
                // Remove from invited list if they were previously invited (fresh start)
                List<Integer> invitedIds = event.getInvitedIds();
                if (invitedIds != null && invitedIds.contains(entrant.getId())) {
                    invitedIds.remove(Integer.valueOf(entrant.getId()));
                    Logger.logSystem("Removed entrant from invited list on rejoin: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                }
                
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

    /**
     * Adds an entrant to a waitlist
     * @param event
     *      Event to add entrant to
     * @param entrant
     *      Entrant to add
     * @param loc
     *      Location of the entrant
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void addEntrantToWaitlist(Event event, Entrant entrant, Location loc, DBWriteCallback callback ) {
        // Disallow join if registration period has ended
        if (event.getEventInfo() != null && event.getEventInfo().getRegEnd() != null) {
            if (Timestamp.now().compareTo(event.getEventInfo().getRegEnd()) > 0) {
                Logger.logError("Attempt to join waitlist after registration closed event id=" + event.getId(), null);
                callback.onFailure(new Exception("Registration period has ended"));
                return;
            }
        }

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
                // Remove from cancelled list if they were previously cancelled
                List<Integer> cancelledIds = event.getCancelledIds();
                if (cancelledIds != null && cancelledIds.contains(entrant.getId())) {
                    cancelledIds.remove(Integer.valueOf(entrant.getId()));
                    Logger.logSystem("Removed entrant from cancelled list on rejoin: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                }
                
                // Remove from invited list if they were previously invited (fresh start)
                List<Integer> invitedIds = event.getInvitedIds();
                if (invitedIds != null && invitedIds.contains(entrant.getId())) {
                    invitedIds.remove(Integer.valueOf(entrant.getId()));
                    Logger.logSystem("Removed entrant from invited list on rejoin: entrantId=" + entrant.getId() + ", eventId=" + event.getId(), null);
                }
                
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

    /**
     * Checks if the entrant is in range of the event
     * @param event
     *      Event to check
     * @param entrantLoc
     *      Location of the entrant
     * @return
     *      True if the entrant is in range, false otherwise
     */
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

    /**
     * Removes entrants from an event
     * @param event
     *      Event to remove entrants from
     * @param entrants
     *      Entrants to remove
     * @param callback
     *      Callback to call when the operation is complete
     */
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

        Query q = eventRef.whereGreaterThanOrEqualTo("eventInfo.regEnd", Timestamp.now());

        // Date filters
        if (search.getAvailStart() != null)
            q = q.whereGreaterThanOrEqualTo("eventInfo.eventDate", search.getAvailStart());

        if (search.getAvailEnd() != null)
            q = q.whereLessThan("eventInfo.eventDate", search.getAvailEnd());

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

                    if (inEntrants || inWaitlist) {
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

    /**
     * Verifies the deletion of an event from the database
     * @param id
     *      ID of the event to delete
     * @param callback
     *      Callback to call when the operation is complete
     */
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

    /**
     * Runs the lottery for an event
     * @param event
     *      Event to run the lottery for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void doLottery(Event event, DBWriteCallback callback) {

        int currentEntrants = getCurrentEntrantCount(event);
        int spots = event.getEventInfo().getMaxEntrants() - currentEntrants;
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

    /**
     * Performs a replacement lottery draw for an event.
     * This method draws from the waitlist to fill available spots, excluding:
     * - Users who have already been invited (invitedIds)
     * - Users who have declined invitations (cancelledIds)
     * 
     * @param event The event to draw replacement lottery for
     * @param callback Callback invoked when the operation completes
     */
    public static void doReplacementLottery(Event event, DBWriteCallback callback) {
        // Calculate available spots
        int currentEntrants = getCurrentEntrantCount(event);
        int spots = event.getEventInfo().getMaxEntrants() - currentEntrants;
        if (spots <= 0) {
            Logger.logError("Replacement lottery failed: no spots available event id=" + event.getId(), null);
            callback.onFailure(new Exception("No spots available"));
            return;
        }

        getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                if (entrants.isEmpty()) {
                    Logger.logError("Replacement lottery failed: no entrants in waitlist event id=" + event.getId(), null);
                    callback.onFailure(new Exception("No entrants in waitlist"));
                    return;
                }

                // Get exclusion lists
                List<Integer> cancelledIds = event.getCancelledIds();
                List<Integer> invitedIds = event.getInvitedIds();
                
                if (cancelledIds == null) {
                    cancelledIds = new ArrayList<>();
                }
                if (invitedIds == null) {
                    invitedIds = new ArrayList<>();
                }

                // Filter out excluded entrants
                List<Entrant> eligiblePool = new ArrayList<>();
                for (Entrant entrant : entrants) {
                    int entrantId = entrant.getId();
                    if (!cancelledIds.contains(entrantId) && !invitedIds.contains(entrantId)) {
                        eligiblePool.add(entrant);
                    }
                }

                if (eligiblePool.isEmpty()) {
                    Logger.logError("Replacement lottery failed: no eligible entrants after exclusions event id=" + event.getId(), null);
                    callback.onFailure(new Exception("No eligible entrants in waitlist"));
                    return;
                }

                // If eligible pool is smaller than or equal to available spots, invite everyone
                if (eligiblePool.size() <= spots) {
                    notifyWinners(eligiblePool, event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.logLotteryRun(event.getId(), null);
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Replacement lottery notifyWinners failed event id=" + event.getId(), null);
                            callback.onFailure(e);
                        }
                    });
                    return;
                }

                // Randomly select winners from eligible pool
                List<Entrant> pool = new ArrayList<>(eligiblePool);
                List<Entrant> winners = new ArrayList<>();

                for (int i = 0; i < spots; i++) {
                    int randomIndex = (int)(Math.random() * pool.size());
                    winners.add(pool.remove(randomIndex));
                }

                // Send invitations to winners (no need to notify losers in replacement lottery)
                notifyWinners(winners, event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        Logger.logLotteryRun(event.getId(), null);
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Replacement lottery notify winners failed event id=" + event.getId(), null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Replacement lottery failed fetching waitlist event id=" + event.getId(), null);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Safely derive the current entrant count using the in-memory list when available,
     * otherwise fall back to the persisted counter in EventInfo. This keeps behavior
     * consistent in tests that manipulate the entrant list directly.
     */
    private static int getCurrentEntrantCount(Event event) {
        if (event.getEntrants() != null && !event.getEntrants().isEmpty()) {
            return event.getEntrants().size();
        }
        return event.getEventInfo().getCurrentEntrants();
    }

    private static void notifyWinners(List<Entrant> winners, Event event, DBWriteCallback callback) {
        String title = "Congratulations!";
        String body = "You have won the lottery for " + event.getEventInfo().getName() + "!";
        int sender = event.getEventInfo().getOrganizerID();
        List<Integer> recipients = new ArrayList<>();
        for (Entrant e : winners) {
            recipients.add(e.getId());
        }

        // DO NOT add winners to entrants list yet - they must accept the invitation first
        // Winners are only added to entrants when they call acceptInvitation()

        // Add winners to invitedIds list (tracking who was invited)
        List<Integer> invitedIds = event.getInvitedIds();
        if (invitedIds == null) {
            invitedIds = new ArrayList<>();
            event.setInvitedIds(invitedIds);
        }
        for (Integer winnerId : recipients) {
            if (!invitedIds.contains(winnerId)) {
                invitedIds.add(winnerId);
            }
        }

        // Keep winners in waitlist - they'll be removed when they accept the invitation
        // This allows them to decline and stay on the waitlist if they want

        // Update event first, then send invitations
        updateEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                // Event updated, now send invitations
                NotificationManager.sendBulkInvitation(title, body, recipients, sender, event.getId(), callback);
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to update event with invitedIds during lottery, eventId=" + event.getId(), null);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Helper method that notifies the losers of the lottery
     * @param losers
     *      List of losers
     * @param event
     *      Event that the losers were not enrolled in
     * @param callback
     *      Callback to call when the operation is complete
     */
    private static void notifyLosers(List<Entrant> losers, Event event, DBWriteCallback callback) {
        String title = "Sorry!";
        String body = "You have lost the lottery for " + event.getEventInfo().getName() + "!\n" +
                "You can still stay registered in case somebody declines their invitation.";
        int sender = event.getEventInfo().getOrganizerID();
        List<Integer> recipients = new ArrayList<>();
        for (Entrant e : losers) {
            recipients.add(e.getId());
        }
        NotificationManager.sendBulkNotSelected(title, body, recipients, sender, event.getId(), callback);
    }

    /**
     * Adds a list of entrants to an event
     * @param event
     *      Event to add entrants to
     * @param entrants
     *      List of entrants to add
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void addEntrantsToEvent(Event event, List<Entrant> entrants, DBWriteCallback callback) {
        List<Consumer<DBWriteCallback>> ops = new ArrayList<>();
        for (Entrant e : entrants) {
            ops.add(cb -> addEntrantToEvent(event, e, cb));
            // removeEntrantFromWaitlist already decrements currentEntrants
            ops.add(cb -> removeEntrantFromWaitlist(event, e, cb));
        }
        AsyncBatchExecutor.runBatch(ops, callback);
    }

    /**
     * Method to get all images from the database
     * @param callback
     *      Callback to call when the operation is complete
     */
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

    /**
     * Method to remove an image from the database
     * @param e
     *      Event to remove image from
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void removeImage(Event e, DBWriteCallback callback) {
        e.getEventInfo().setImageUrl(null);
        eventRef.document(String.valueOf(e.getId()))
                .set(e)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e1 -> callback.onFailure(new Exception("Failed to remove image")));
    }

    /**
     * Exports enrolled entrants to a text file in the Downloads folder
     * 
     * @param event Event containing entrants to export
     * @param context Android context for file operations
     * @param callback Callback with file path or error
     */
    public static void exportEntrantsToCSV(Event event, android.content.Context context, com.example.slices.interfaces.CSVExportCallback callback) {
        try {
            // Get enrolled entrants
            List<Entrant> entrants = event.getEntrants();
            if (entrants == null || entrants.isEmpty()) {
                Logger.logError("Export failed: No enrolled entrants", null);
                callback.onFailure(new Exception("No enrolled entrants to export"));
                return;
            }
            
            Logger.logSystem("Starting entrants export for event id=" + event.getId() + ", " + entrants.size() + " entrants", null);
            
            // Get Downloads directory
            java.io.File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            );
            
            // Create filename with event name and timestamp - use .txt for easy viewing
            String eventName = event.getEventInfo().getName().replaceAll("[^a-zA-Z0-9]", "_");
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(new java.util.Date());
            String fileName = eventName + "_entrants_" + timestamp + ".txt";
            
            java.io.File txtFile = new java.io.File(downloadsDir, fileName);
            Logger.logSystem("Export file path: " + txtFile.getAbsolutePath(), null);
            
            // Write content in a readable format
            java.io.FileWriter writer = new java.io.FileWriter(txtFile);
            
            // Write header
            writer.append("Event: " + event.getEventInfo().getName() + "\n");
            writer.append("Enrolled Participants: " + entrants.size() + "\n");
            writer.append("Exported: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date()) + "\n");
            writer.append("=" .repeat(60) + "\n\n");
            
            // Write data rows
            int rowCount = 0;
            for (Entrant entrant : entrants) {
                rowCount++;
                
                // Get profile data with null checks
                String name = "N/A";
                String email = "N/A";
                String phone = "N/A";
                
                if (entrant.getProfile() != null) {
                    name = entrant.getProfile().getName() != null ? entrant.getProfile().getName() : "N/A";
                    email = entrant.getProfile().getEmail() != null ? entrant.getProfile().getEmail() : "N/A";
                    phone = entrant.getProfile().getPhoneNumber() != null ? entrant.getProfile().getPhoneNumber() : "N/A";
                } else {
                    Logger.logSystem("Warning: Entrant " + entrant.getId() + " has null profile", null);
                }
                
                // Write in readable format
                writer.append("Participant #" + rowCount + "\n");
                writer.append("  ID: " + entrant.getId() + "\n");
                writer.append("  Name: " + name + "\n");
                writer.append("  Email: " + email + "\n");
                writer.append("  Phone: " + phone + "\n");
                writer.append("\n");
            }
            
            writer.flush();
            writer.close();
            
            Logger.logSystem("Export file written successfully: " + rowCount + " participants", null);
            Logger.logSystem("File exists: " + txtFile.exists() + ", size: " + txtFile.length() + " bytes", null);
            
            // Notify media scanner so file appears in Downloads immediately
            android.media.MediaScannerConnection.scanFile(
                context,
                new String[]{txtFile.getAbsolutePath()},
                new String[]{"text/plain"},
                null
            );
            
            Logger.logSystem("Export successful for event id=" + event.getId(), null);
            callback.onSuccess(txtFile.getAbsolutePath());
            
        } catch (Exception e) {
            Logger.logError("Export failed for event id=" + event.getId() + ": " + e.getMessage(), null);
            e.printStackTrace();
            callback.onFailure(e);
        }
    }
    
    /**
     * Helper method to escape CSV special characters
     * 
     * @param value String value to escape
     * @return Escaped string safe for CSV
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * Sends a preset notification to a cancelled entrant.
     * This method is called automatically when an entrant is moved to the cancelled list.
     * 
     * @param entrantId ID of the cancelled entrant
     * @param eventName Name of the event for message formatting
     */
    public static void sendCancelledNotification(int entrantId, String eventName) {
        String title = "We're Sorry to See You Go";
        String message = String.format(
            "Thank you for your interest in %s. We hope to see you at future events!",
            eventName
        );
        
        // Send notification with sender ID 0 (system notification)
        NotificationManager.sendNotification(title, message, entrantId, 0, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.logSystem("Cancelled notification sent to entrantId=" + entrantId + " for event=" + eventName, null);
            }
            
            @Override
            public void onFailure(Exception e) {
                // Log error but don't throw exception - notification failure shouldn't block cancellation
                Logger.logError("Failed to send cancelled notification to entrantId=" + entrantId + " for event=" + eventName + ": " + e.getMessage(), null);
            }
        });
    }

    /**
     * Cancels a single invited entrant and removes them completely from the event.
     * This method:
     * 1. Verifies the entrant is in invitedIds
     * 2. Removes the entrant from invitedIds
     * 3. Removes the entrant from waitlist (if present)
     * 4. Removes the entrant from enrolled entrants (if they accepted)
     * 5. Adds the entrant to cancelledIds
     * 6. Sends a cancellation notification with preset message
     * 7. Updates the event in the database
     *
     * After removal, the entrant will not see this event in any fragments.
     *
     * @param event The event to process
     * @param entrantId ID of the entrant to cancel
     * @param callback Callback invoked when the operation completes
     */
    public static void cancelSingleEntrant(Event event, int entrantId, DBWriteCallback callback) {
        // Get the lists (initialize if null)
        List<Integer> invitedIds = event.getInvitedIds();
        List<Integer> entrantIds = event.getEntrantIds();
        List<Integer> cancelledIds = event.getCancelledIds();

        if (invitedIds == null) {
            invitedIds = new ArrayList<>();
            event.setInvitedIds(invitedIds);
        }
        if (entrantIds == null) {
            entrantIds = new ArrayList<>();
            event.setEntrantIds(entrantIds);
        }
        if (cancelledIds == null) {
            cancelledIds = new ArrayList<>();
            event.setCancelledIds(cancelledIds);
        }

        // Verify entrant is in invitedIds
        if (!invitedIds.contains(entrantId)) {
            Logger.logError("Cannot cancel entrant: not in invited list, entrantId=" + entrantId + ", eventId=" + event.getId(), null);
            callback.onFailure(new Exception("Entrant not found or already cancelled"));
            return;
        }

        // Check if entrant has accepted (in enrolled list)
        boolean hasAccepted = entrantIds.contains(entrantId);

        // If already accepted, do not cancel
        if (hasAccepted) {
            Logger.logError("Cannot cancel entrant: already accepted, entrantId=" + entrantId + ", eventId=" + event.getId(), null);
            callback.onFailure(new Exception("Entrant already accepted"));
            return;
        }

        // Remove from invitedIds
        invitedIds.remove(Integer.valueOf(entrantId));

        // Remove from waitlist if present
        if (event.getWaitlist() != null && event.getWaitlist().getEntrants() != null) {
            event.getWaitlist().getEntrants().removeIf(e -> e.getId() == entrantId);
        }
        if (event.getWaitlist() != null && event.getWaitlist().getEntrantIds() != null) {
            event.getWaitlist().getEntrantIds().remove(Integer.valueOf(entrantId));
        }

        // Add to cancelledIds
        if (!cancelledIds.contains(entrantId)) {
            cancelledIds.add(entrantId);
        }
        // Remove from invitedIds to ensure cancelled entrants are not listed as invited
        if (invitedIds.contains(entrantId)) {
            invitedIds.remove(Integer.valueOf(entrantId));
        }

        // Create and send cancellation notification
        String title = "Invitation Expired";
        String message = String.format(
            "Your invitation to %s has been cancelled by the organizer. Thank you for your interest.",
            event.getEventInfo().getName()
        );

        // Send notification with organizer as sender
        int senderId = event.getEventInfo().getOrganizerID();
        NotificationManager.sendNotification(title, message, entrantId, senderId, event.getId(), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.logSystem("Cancellation notification sent to entrantId=" + entrantId + " for event=" + event.getEventInfo().getName(), null);
            }

            @Override
            public void onFailure(Exception e) {
                // Log error but don't block the cancellation
                Logger.logError("Failed to send cancellation notification to entrantId=" + entrantId + ": " + e.getMessage(), null);
            }
        });

        // Update event in database
        updateEvent(event, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.logSystem("Successfully cancelled and removed entrant, entrantId=" + entrantId + ", eventId=" + event.getId(), null);
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to update event after cancelling entrant, entrantId=" + entrantId + ", eventId=" + event.getId(), null);
                callback.onFailure(new Exception("Failed to cancel entrant: database update failed"));
            }
        });
    }

    /**
     * Cancels multiple invited entrants and removes them completely from the event.
     * This method processes all provided entrant IDs in parallel using AsyncBatchExecutor.
     * For each entrant:
     * 1. Verifies the entrant is in invitedIds
     * 2. Removes the entrant from invitedIds
     * 3. Removes the entrant from waitlist (if present)
     * 4. Removes the entrant from enrolled entrants (if they accepted)
     * 5. Adds the entrant to cancelledIds
     * 6. Sends a cancellation notification with preset message
     *
     * After all individual operations complete, updates the event once in the database.
     * After removal, cancelled entrants will not see this event in any fragments.
     *
     * @param event The event to process
     * @param entrantIds List of entrant IDs to cancel
     * @param callback Callback invoked when all operations complete
     */
    public static void cancelMultipleEntrants(Event event, List<Integer> entrantIds, DBWriteCallback callback) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            callback.onSuccess();
            return;
        }

        // Get the lists (initialize if null)
        List<Integer> invitedIds = event.getInvitedIds();
        List<Integer> enrolledIds = event.getEntrantIds();
        List<Integer> cancelledIds = event.getCancelledIds();

        if (invitedIds == null) {
            invitedIds = new ArrayList<>();
            event.setInvitedIds(invitedIds);
        }
        if (enrolledIds == null) {
            enrolledIds = new ArrayList<>();
            event.setEntrantIds(enrolledIds);
        }
        if (cancelledIds == null) {
            cancelledIds = new ArrayList<>();
            event.setCancelledIds(cancelledIds);
        }

        // Track successfully cancelled entrants
        List<Integer> successfullyCancelled = new ArrayList<>();
        List<Integer> failedCancellations = new ArrayList<>();

        // Process each entrant
        for (Integer entrantId : entrantIds) {
            // Verify entrant is in invitedIds
            if (!invitedIds.contains(entrantId)) {
                Logger.logError("Cannot cancel entrant: not in invited list, entrantId=" + entrantId + ", eventId=" + event.getId(), null);
                failedCancellations.add(entrantId);
                continue;
            }

            // Check if entrant has accepted (in enrolled list)
            boolean hasAccepted = enrolledIds.contains(entrantId);

            // Skip accepted entrants entirely
            if (hasAccepted) {
                Logger.logSystem("Skipping cancellation for accepted entrantId=" + entrantId + ", eventId=" + event.getId(), null);
                continue;
            }

            // Remove from invitedIds
            invitedIds.remove(Integer.valueOf(entrantId));

            // Remove from waitlist if present
            if (event.getWaitlist() != null && event.getWaitlist().getEntrants() != null) {
                event.getWaitlist().getEntrants().removeIf(e -> e.getId() == entrantId);
            }
            if (event.getWaitlist() != null && event.getWaitlist().getEntrantIds() != null) {
                event.getWaitlist().getEntrantIds().remove(Integer.valueOf(entrantId));
            }

            // Add to cancelledIds
            if (!cancelledIds.contains(entrantId)) {
                cancelledIds.add(entrantId);
            }
            // Remove from invitedIds to ensure cancelled entrants are not listed as invited
            if (invitedIds.contains(entrantId)) {
                invitedIds.remove(Integer.valueOf(entrantId));
            }

            successfullyCancelled.add(entrantId);
        }

        // Send notifications to all successfully cancelled entrants in parallel
        if (!successfullyCancelled.isEmpty()) {
            String eventName = event.getEventInfo().getName();
            int senderId = event.getEventInfo().getOrganizerID();

            // Create list of operations for AsyncBatchExecutor
            List<Consumer<DBWriteCallback>> operations = new ArrayList<>();

            for (Integer entrantId : successfullyCancelled) {
                operations.add(opCallback -> {
                    String title = "Invitation Expired";
                    String message = String.format(
                        "Your invitation to %s has been cancelled by the organizer. Thank you for your interest.",
                        eventName
                    );

                    NotificationManager.sendNotification(title, message, entrantId, senderId, event.getId(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.logSystem("Expiration notification sent to entrantId=" + entrantId + " for event=" + eventName, null);
                            opCallback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Log error but don't block the cancellation
                            Logger.logError("Failed to send expiration notification to entrantId=" + entrantId + ": " + e.getMessage(), null);
                            opCallback.onSuccess(); // Still mark as success to continue
                        }
                    });
                });
            }

            // Run all notification operations in parallel
            AsyncBatchExecutor.runBatch(operations, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    // All notifications sent (or attempted), now update the event
                    updateEvent(event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.logSystem("Successfully cancelled " + successfullyCancelled.size() + " non-responsive entrants, eventId=" + event.getId(), null);
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Failed to update event after bulk cancellation, eventId=" + event.getId(), null);
                            callback.onFailure(new Exception("Failed to cancel entrants: database update failed"));
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    // Some notifications failed, but still update event
                    Logger.logError("Some notifications failed during bulk cancellation: " + e.getMessage(), null);
                    updateEvent(event, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            Logger.logSystem("Cancelled " + successfullyCancelled.size() + " entrants (some notifications failed), eventId=" + event.getId(), null);
                            callback.onSuccess(); // Still consider it a success since entrants were cancelled
                        }

                        @Override
                        public void onFailure(Exception updateError) {
                            Logger.logError("Failed to update event after bulk cancellation, eventId=" + event.getId(), null);
                            callback.onFailure(new Exception("Failed to cancel entrants: database update failed"));
                        }
                    });
                }
            });
        } else {
            // No entrants were successfully cancelled
            if (failedCancellations.size() == entrantIds.size()) {
                callback.onFailure(new Exception("None of the provided entrants could be cancelled"));
            } else {
                callback.onSuccess();
            }
        }
    }

}


