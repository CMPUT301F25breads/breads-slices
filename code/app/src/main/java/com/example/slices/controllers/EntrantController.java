package com.example.slices.controllers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.exceptions.EventNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.ProfileCallback;
import com.example.slices.models.AsyncBatchExecutor;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Profile;
import com.example.slices.testing.DebugLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EntrantController {
    private static EntrantController instance;


    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static CollectionReference entrantRef = firestore.collection("entrants");

    private EntrantController() {}

    public static void setTesting(boolean testing) {
        if (testing) {
            entrantRef = firestore.collection("test_entrants");
        } else {
            entrantRef = firestore.collection("entrants");
        }
    }

    /**
     * Gets an entrant from the database asynchronously
     * @param id
     *      Entrant ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrant(int id, EntrantCallback callback) {
        entrantRef
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant != null) {
                            Logger.logSystem("Fetched entrant id=" + id, null);
                            callback.onSuccess(entrant);
                        }
                        else{
                            Logger.logError("Entrant object null after fetch id=" + id, null);
                            callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));
                        }

                    } else {
                        Logger.logError("Entrant not found id=" + id, null);
                        callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));

                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to fetch entrant id=" + id, null);
                    callback.onFailure(new DBOpFailed("Failed to get entrant"));
                });

    }

    /**
     * Function to get a list of entrants based on a list of IDs
     * @param ids
     *      List of IDs to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrants(List<Integer> ids, EntrantListCallback callback) {
        if (ids == null || ids.isEmpty()) {
            Logger.logSystem("getEntrants called with empty ID list", null);
            callback.onSuccess(new ArrayList<>());
            return;
        }
        List<Entrant> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger completed = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);
        int total = ids.size();

        for (int id : ids) {
            getEntrant(id, new EntrantCallback() {
                @Override
                public void onSuccess(Entrant entrant) {
                    if (failed.get()) {
                        return;
                    }
                    results.add(entrant);
                    if (completed.incrementAndGet() == total) {
                        Logger.logSystem("Fetched " + total + " entrants by ID list", null);
                        callback.onSuccess(results);
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (failed.compareAndSet(false, true)) {
                        Logger.logError("Failed to fetch entrant during batch getEntrants", null);
                        callback.onFailure(e);
                    }
                }
            });
        }
    }

    /**
     * Gets an entrant from the database asynchronously
     * @param deviceId
     *      Entrant device ID to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrantByDeviceId(String deviceId, EntrantCallback callback) {
        entrantRef
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            Entrant entrant = doc.toObject(Entrant.class);
                            if(entrant != null) {
                                Logger.logSystem("Fetched entrant by deviceId=" + deviceId, null);
                                callback.onSuccess(entrant);
                            }
                            else {
                                Logger.logError("Entrant null after fetch by deviceId=" + deviceId, null);
                                callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(deviceId)));
                            }
                        }
                    }
                    else{
                        Logger.logError("Entrant not found by deviceId=" + deviceId, null);
                        callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(deviceId)));
                    }
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to fetch entrant by deviceId=" + deviceId, null);
                    callback.onFailure(new DBOpFailed("Failed to get entrant"));
                });

    }

    /**
     * Gets the next available entrant ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getNewEntrantId(EntrantIDCallback callback) {
        entrantRef
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int highestId = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            int id = doc.getLong("id").intValue();
                            if (id > highestId) {
                                highestId = id;
                            }
                        }
                        Logger.logSystem("Generated new entrant ID=" + (highestId + 1), null);
                        callback.onSuccess(highestId + 1);
                    } else {
                        Logger.logSystem("Generated new entrant ID=1 (first entrant)", null);
                        callback.onSuccess(1);
                    }

                }).addOnFailureListener(e -> {
                    Logger.logError("Failed to generate new entrant ID", null);
                    callback.onFailure(new DBOpFailed("Failed to get next entrant ID"));
                });


    }
    /**
     * Writes an entrant to the database asynchronously
     * @param entrant
     *      Entrant to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void writeEntrant(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    Logger.logEntrantUpdate(entrant.getId(), -1, null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to write entrant id=" + entrant.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write entrant"));
                });

    }

    /**
     * Writes an entrant to the database asynchronously
     * @param entrant
     *      Entrant to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void writeEntrantDeviceId(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    Logger.logEntrantUpdate(entrant.getId(), -1, null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to write entrant deviceId for id=" + entrant.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write entrant"));
                });
    }




    /**
     * Updates an entrant in the database asynchronously
     * @param entrant
     *      Entrant to update in the database
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void updateEntrant(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    Logger.logEntrantUpdate(entrant.getId(), -1, null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Logger.logError("Failed to update entrant id=" + entrant.getId(), null);
                    callback.onFailure(new DBOpFailed("Failed to write entrant"));
                });
    }


    /**
     * Deletes an entrant from the database asynchronously.
     *
     * @param id
     *      Entrant ID to delete
     * @param callback
     *      Callback called when the delete operation completes
     *
     * @throws Exception
     *      If the initial fetch call cannot be made
     */
    public static void deleteEntrant(String id, DBWriteCallback callback) throws Exception {
        int entrantId = Integer.parseInt(id);
        //Get the entrant
        EntrantController.getEntrant(entrantId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                //If entrant found
                Logger.logSystem("Starting delete pipeline for entrant id=" + entrantId, null);
                //Get all events and waitlists the entrant belongs to
                EventController.getAllEventsForEntrant(entrant, new EntrantEventCallback() {
                    @Override
                    public void onSuccess(List<Event> events, List<Event> waitEvents) {
                        List<Consumer<DBWriteCallback>> operations = getOperations(events, waitEvents, entrant);
                        //If no operations, delete directly
                        if (operations.isEmpty()) {
                            Logger.logSystem("Entrant id=" + entrantId + " not in any events; deleting directly", null);
                            deleteEntrantDoc(entrant, callback);
                            return;
                        }
                        //Otherwise, delete in batches
                        AsyncBatchExecutor.runBatch(operations, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                Logger.logSystem("Entrant id=" + entrantId + " removed from all events; proceeding to delete", null);
                                //Delete the entrant document
                                deleteEntrantDoc(entrant, callback);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Logger.logError("Failed removing entrant id=" + entrantId + " from all events", null);
                                List<Event> allEvents = new ArrayList<>();
                                allEvents.addAll(events);
                                allEvents.addAll(waitEvents);
                                //Re-enroll the entrant in all events
                                reEnroll(entrant, allEvents, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Logger.logError("Re-enroll after failed removal for entrant id=" + entrantId, null);
                                        callback.onFailure(new Exception("Failed to remove entrant from all events"));
                                    }

                                    @Override
                                    public void onFailure(Exception ex) {
                                        Logger.logError("Failed to re-enroll entrant id=" + entrantId + " after batch removal failure", null);
                                        callback.onFailure(new Exception("Failed to remove entrant from all events and re-enroll"));
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to fetch events for entrant id=" + entrantId + " during delete pipeline", null);
                        callback.onFailure(
                                new EventNotFound("Events not found when attempting to remove entrants", id)
                        );
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Entrant not found during delete pipeline id=" + entrantId, null);
                callback.onFailure(
                        new EntrantNotFound("Entrant not found when attempting to delete", id)
                );
            }
        });
    }

    /**
     * Gets a list of operations to perform when deleting an entrant
     * @param events
     *      List of events the entrant belongs to
     * @param waitEvents
     *      List of waitlist events the entrant belongs to
     * @param entrant
     *      Entrant to delete
     * @return
     *      List of operations to perform when deleting the entrant
     */
    private static List<Consumer<DBWriteCallback>> getOperations(List<Event> events, List<Event> waitEvents, Entrant entrant) {
        List<Consumer<DBWriteCallback>> operations = new ArrayList<>();
        for (Event event : events) {
            operations.add(batchCallback ->
                    EventController.removeEntrantFromEvent(event, entrant, batchCallback));
        }

        for (Event event : waitEvents) {
            operations.add(batchCallback ->
                    EventController.removeEntrantFromWaitlist(event, entrant, batchCallback));
        }
        return operations;
    }


    /**
     * Deletes the entrant document from Firestore and verifies deletion.
     * @param entrant
     *      Entrant to delete
     * @param callback
     *      Callback to call when deletion completes
     */
    private static void deleteEntrantDoc(Entrant entrant, DBWriteCallback callback) {

        entrantRef.document(String.valueOf(entrant.getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {

                    DebugLogger.d("Event", "Entrant deleted");
                    Logger.logSystem("Entrant id=" + entrant.getId() + " deleted from DB; verifying", null);

                    // Now verify deletion
                    EntrantController.verifyDeleteEntrant(entrant, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            DebugLogger.d("Event", "Entrant deleted successfully");
                            Logger.logSystem("Entrant deletion verified id=" + entrant.getId(), null);
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            DebugLogger.d("Event", "Entrant deletion verification failed");
                            Logger.logError("Entrant deletion verification failed id=" + entrant.getId(), null);
                            callback.onFailure(new EntrantNotFound("Entrant not found after delete() call", String.valueOf(entrant.getId())));
                        }
                    });
                })
                .addOnFailureListener(e -> {
                            Logger.logError("Failed to delete entrant id=" + entrant.getId(), null);
                            callback.onFailure(new EntrantNotFound("Failed to delete entrant", String.valueOf(entrant.getId())));
                        }
                );
    }


    /**
     * Clears all entrants from the database asynchronously: Used for testing
     * @param onComplete
     *      Callback to call when the operation is complete
     *
     */
    public static void clearEntrants(Runnable onComplete) {
        entrantRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(entrantRef.document(doc.getId()).delete());
                    }
                    // Wait for all deletes to finish
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> {
                                Logger.logSystem("Cleared all entrants", null);
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear entrants: " + e.getMessage());
                    Logger.logError("Failed to clear entrants", null);
                    onComplete.run();
                });
    }


    /**
     * Re-enrolls an entrant into multiple events asynchronously.
     * This method attempts to add the entrant back into each event provided.
     * The callback is only invoked once all operations complete.
     * If any add operation fails, the callback will return a failure.
     *
     * @param entrant
     *      Entrant to re-enroll
     * @param events
     *      List of events the entrant should be re-added to
     * @param callback
     *      Callback to call when the re-enroll operation completes
     */
    private static void reEnroll(Entrant entrant, List<Event> events, DBWriteCallback callback) {

        // If no events, nothing to do
        if (events == null || events.isEmpty()) {
            Logger.logSystem("reEnroll: no events to re-add entrant id=" + entrant.getId(), null);
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        Logger.logSystem("reEnroll: re-adding entrant id=" + entrant.getId() + " to "
                + events.size() + " events", null);

        List<Consumer<DBWriteCallback>> operations = new ArrayList<>();

        for (Event event : events) {
            operations.add(batchCallback ->
                    EventController.addEntrantToEvent(event, entrant, batchCallback));
        }

        AsyncBatchExecutor.runBatch(operations, callback);
    }

    /**
     * Verifies that the entrant is not in the database
     * @param entrant
     *      Entrant to verify
     * @param callback
     *      Callback to call when the verification is complete
     */
    private static void verifyDeleteEntrant(Entrant entrant, DBWriteCallback callback) {
        //Check if entrant or profile is in the database
        entrantRef.whereEqualTo("id", entrant.getId()).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess();
                    } else {
                        Logger.logError("verifyDeleteEntrant: entrant still in DB id=" + entrant.getId(), null);
                        callback.onFailure(new DBOpFailed("Entrant still in database"));
                    }
                });

    }

    /**
     * Creates an entrant asynchronously
     * @param deviceId
     *      Device ID of the entrant
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void createEntrant(String deviceId, EntrantCallback callback) {
        //First check if the entrant already exists
        EntrantController.getEntrantByDeviceId(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                Logger.logError("Attempted to create duplicate entrant with deviceId=" + deviceId, null);
                callback.onFailure(new DBOpFailed("Entrant already exists"));
            }
            @Override
            public void onFailure(Exception e) {
                if(e instanceof EntrantNotFound) {
                    //If it doesn't exist, create it
                    //Start by getting a new id
                    EntrantController.getNewEntrantId(new EntrantIDCallback() {
                        @Override
                        public void onSuccess(int id) {
                            Entrant newEntrant = new Entrant(deviceId, id);
                            EntrantController.writeEntrant(newEntrant, new DBWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    Logger.logSystem("Created entrant id=" + id + " from deviceId=" + deviceId, null);
                                    callback.onSuccess(newEntrant);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Logger.logError("Failed to create entrant from deviceId=" + deviceId, null);
                                    callback.onFailure(e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Logger.logError("Failed to generate ID while creating entrant from deviceId=" + deviceId, null);
                            callback.onFailure(e);
                        }
                    });
                }
            }
        });
    }

    /**
     * Creates an entrant asynchronously
     * @param name
     *      Name of the entrant
     * @param email
     *      Email of the entrant
     * @param phoneNumber
     *      Phone number of the entrant
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void createEntrant(String name, String email, String phoneNumber, EntrantCallback callback) {
        //Start by getting a new id
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                Entrant newEntrant = new Entrant(name, email, phoneNumber, id);
                EntrantController.writeEntrant(newEntrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        Logger.logSystem("Created entrant id=" + id + " name=" + name, null);
                        callback.onSuccess(newEntrant);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to create entrant name=" + name, null);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to generate ID while creating entrant name=" + name, null);
                callback.onFailure(e);
            }

        });
    }

    /**
     * Updates an entrant profile asynchronously
     * @param entrant
     *      Entrant to update
     * @param profile
     *      Profile to update with
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void updateProfile(Entrant entrant, Profile profile, DBWriteCallback callback) {
        entrant.setProfile(profile);
        Logger.logEntrantUpdate(entrant.getId(), -1, null);
        EntrantController.updateEntrant(entrant, callback);
    }

    /**
     * Creates a sub-entrant asynchronously
     * @param parent
     *      Parent entrant
     * @param name
     *      Name of the sub-entrant
     * @param email
     *      Email of the sub-entrant
     * @param phoneNumber
     *      Phone number of the sub-entrant
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void createSubEntrant(Entrant parent, String name, String email, String phoneNumber, EntrantCallback callback) {
        //First check that the parent is not already a sub-entrant
        if(parent.getParent() != 0) {
            Logger.logError("Attempt to create sub-entrant under sub-entrant parent id=" + parent.getId(), null);
            callback.onFailure(new DBOpFailed("Parent is already a sub-entrant"));
        }
        //Lets make a new entrant then add them to the parent
        EntrantController.createEntrant(name, email, phoneNumber, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                parent.addSubEntrant(entrant);
                Logger.logSystem("Created sub-entrant id=" + entrant.getId() + " under parent id=" + parent.getId(), null);
                updateEntrant(parent, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(entrant);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Logger.logError("Failed to update parent after sub-entrant creation id=" + parent.getId(), null);
                        callback.onFailure(e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Logger.logError("Failed to create sub-entrant under parent id=" + parent.getId(), null);
                callback.onFailure(e);
            }
        });
    }

    /**
     * Deletes a sub-entrant asynchronously
     * @param parent
     *      Parent entrant
     * @param subEntrant
     *      Sub-entrant to delete
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void deleteSubEntrant(Entrant parent, Entrant subEntrant, DBWriteCallback callback) {
        //First delete them normally then remove them from the parent
        try {
            EntrantController.deleteEntrant(String.valueOf(subEntrant.getId()), new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    parent.removeSubEntrant(subEntrant);
                    Logger.logSystem("Deleted sub-entrant id=" + subEntrant.getId() + " from parent id=" + parent.getId(), null);
                    updateEntrant(parent, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    Logger.logError("Failed to delete sub-entrant id=" + subEntrant.getId(), null);
                    callback.onFailure(e);
                }
            });
        } catch (Exception e) {
            Logger.logError("Exception deleting sub-entrant id=" + subEntrant.getId(), null);
            callback.onFailure(e);
        }
    }


}
