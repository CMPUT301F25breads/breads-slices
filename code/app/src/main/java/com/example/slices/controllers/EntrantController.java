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

    public static EntrantController getInstance() {
        if (instance == null) {
            instance = new EntrantController();
        }
        if (entrantRef == null) {
            entrantRef = firestore.collection("entrants");
        }
        return instance;
    }
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
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                callback.onSuccess(entrant);
                            }
                            else{
                                callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));
                            }

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

    }

    /**
     * Function to get a list of entrants based on a list of IDs
     * @param ids
     *      List of IDs to search for
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getEntrants(List<Integer> ids, EntrantListCallback callback) {

        //If empty input, return empty list immediately
        if (ids == null || ids.isEmpty()) {
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
                        callback.onSuccess(results);
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
                                    callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(deviceId)));
                                }
                            }
                        }
                        else{
                            callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(deviceId)));
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
     * Gets the next available entrant ID
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void getNewEntrantId(EntrantIDCallback callback) {
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
    public static void writeEntrant(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
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
    public static void writeEntrantDeviceId(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(String.valueOf(entrant.getId()))
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));
    }


    /**
     * Deletes an entrant from the database asynchronously.
     *
     * This method:
     *  1. Retrieves the entrant using their ID
     *  2. Removes them from all events and waitlists they belong to
     *  3. Deletes the entrant document from Firestore
     *  4. Verifies that deletion succeeded
     *
     * The deletion only occurs after all event removals succeed.
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

        // First get the entrant
        EntrantController.getEntrant(entrantId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {

                // Now get all events they belong to
                EventController.getAllEventsForEntrant(entrant, new EntrantEventCallback() {
                    @Override
                    public void onSuccess(List<Event> events, List<Event> waitEvents) {

                        //Total async operations to complete (events + waitlists)
                        List<Consumer<DBWriteCallback>> operations = new ArrayList<>();

                        for (Event event : events) {
                            operations.add(batchCallback ->
                                    EventController.removeEntrantFromEvent(event, entrant, batchCallback));
                        }

                        for (Event event : waitEvents) {
                            operations.add(batchCallback ->
                                    EventController.removeEntrantFromWaitlist(event, entrant, batchCallback));
                        }

                        //If they are not in any events, delete directly
                        if (operations.isEmpty()) {
                            deleteEntrantDoc(entrant, callback);
                            return;
                        }

                        //Run batch removals using AsyncBatchExecutor
                        AsyncBatchExecutor.runBatch(operations, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                // All removals succeeded — delete the entrant
                                deleteEntrantDoc(entrant, callback);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Attempt to re-enroll entrant into original events/waitlists
                                List<Event> allEvents = new ArrayList<>();
                                allEvents.addAll(events);
                                allEvents.addAll(waitEvents);

                                reEnroll(entrant, allEvents, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.onFailure(new Exception("Failed to remove entrant from all events"));
                                    }

                                    @Override
                                    public void onFailure(Exception ex) {
                                        DebugLogger.d("Event", "Failed to re-enroll entrant after batch removal failure");
                                        callback.onFailure(new Exception("Failed to remove entrant from all events and re-enroll"));
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(
                                new EventNotFound("Events not found when attempting to remove entrants", id)
                        );
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(
                        new EntrantNotFound("Entrant not found when attempting to delete", id)
                );
            }
        });
    }


    /**
     * Deletes the entrant document from Firestore and verifies deletion.
     *
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

                    // Now verify deletion
                    EntrantController.verifyDeleteEntrant(entrant, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            DebugLogger.d("Event", "Entrant deleted successfully");
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            DebugLogger.d("Event", "Entrant deletion verification failed");
                            callback.onFailure(
                                    new EntrantNotFound(
                                            "Entrant not found after delete() call",
                                            String.valueOf(entrant.getId())
                                    )
                            );
                        }
                    });

                })
                .addOnFailureListener(e ->
                        callback.onFailure(
                                new EntrantNotFound(
                                        "Failed to delete entrant",
                                        String.valueOf(entrant.getId())
                                )
                        )
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
                            .addOnSuccessListener(aVoid -> onComplete.run());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear entrants: " + e.getMessage());
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
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        List<Consumer<DBWriteCallback>> operations = new ArrayList<>();

        for (Event event : events) {
            operations.add(batchCallback ->
                    EventController.addEntrantToEvent(event, entrant, batchCallback));
        }

        AsyncBatchExecutor.runBatch(operations, callback);
    }


    private static void verifyDeleteEntrant(Entrant entrant, DBWriteCallback callback) {
        //Check if entrant or profile is in the database
        entrantRef.whereEqualTo("id", entrant.getId()).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(new DBOpFailed("Entrant still in database"));
                    }
                });

    }

    public static void createEntrant(String deviceId, EntrantCallback callback) {
        //First check if the entrant already exists
        EntrantController.getEntrantByDeviceId(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
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
                                callback.onSuccess(newEntrant);
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
        });
    }



    public static void createEntrant(String name, String email, String phoneNumber, EntrantCallback callback) {
        //Start by getting a new id
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                Entrant newEntrant = new Entrant(name, email, phoneNumber, id);
                EntrantController.writeEntrant(newEntrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(newEntrant);
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

    public static void updateProfile(Entrant entrant, Profile profile, DBWriteCallback callback) {
        entrant.setProfile(profile);
        EntrantController.updateEntrant(entrant, callback);
    }


    public static void createSubEntrant(Entrant parent, String name, String email, String phoneNumber, EntrantCallback callback) {
        //First check that the parent is not already a sub-entrant
        if(parent.getParent() != 0) {
            callback.onFailure(new DBOpFailed("Parent is already a sub-entrant"));
        }
        //Lets make a new entrant then add them to the parent
        EntrantController.createEntrant(name, email, phoneNumber, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                parent.addSubEntrant(entrant);
                updateEntrant(parent, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(entrant);
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

    public static void deleteSubEntrant(Entrant parent, Entrant subEntrant, DBWriteCallback callback) {
        //First delete them normally then remove them from the parent
        try {
            EntrantController.deleteEntrant(String.valueOf(subEntrant.getId()), new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    parent.removeSubEntrant(subEntrant);
                    updateEntrant(parent, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

}
