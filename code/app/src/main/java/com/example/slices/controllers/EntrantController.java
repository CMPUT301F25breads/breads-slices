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
import com.example.slices.interfaces.ProfileCallback;
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
import java.util.List;

public class EntrantController {
    private static EntrantController instance;


    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static CollectionReference entrantRef;

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
     * @return
     *      The next available entrant ID
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



    /*public void writeEntrantDeviceId(Entrant entrant, DBWriteCallback callback) {
        entrantRef.document(entrant.getDeviceId())
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write entrant")));

    }*/

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
     * Deletes an entrant from the database asynchronously
     * @param id
     *      Entrant ID to delete
     */
    public static void deleteEntrant(String id, DBWriteCallback callback) throws Exception {
        //First get the entrant
        EntrantController.getEntrant(Integer.parseInt(id), new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                //Now remove them from all events
                EventController.getEventsForEntrant(entrant, new EntrantEventCallback(){
                    @Override
                    public void onSuccess(List<Event> events, List<Event> waitEvents) {
                        //Set up storage for removed from events
                        ArrayList<Event> remEvents = new ArrayList<>();
                        ArrayList<Event> remWaitEvents = new ArrayList<>();
                        //Remove them from all events
                        for(Event event : events){
                            event.removeEntrant(entrant, new DBWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    DebugLogger.d("Event", "Entrant removed from event");
                                    remEvents.add(event);


                                }

                                @Override
                                public void onFailure(Exception e) {
                                    //Revert the changes
                                    try {
                                        EntrantController.reEnroll(entrant, remEvents, null);
                                    } catch (Exception ex) {
                                        DebugLogger.d("Event", "Failed to re-enroll entrant");
                                    }

                                    throw new EntrantNotFound("Entrant not found when attempting to remove from event", String.valueOf(id));


                                }

                                });
                        }
                        for(Event event : waitEvents){
                            event.removeEntrantFromWaitlist(entrant, new DBWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    DebugLogger.d("Event", "Entrant removed from waitlist");
                                    remWaitEvents.add(event);
                                    //If removed from all events, delete the entrant
                                    if(remEvents.size() == events.size() && remWaitEvents.size() == waitEvents.size()) {
                                        //Now delete the entrant
                                        entrantRef.document(String.valueOf(entrant.getId())).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    DebugLogger.d("Event", "Entrant deleted");
                                                    //Now check if the entrant is still in the database
                                                    EntrantController.verifyDeleteEntrant(entrant, new DBWriteCallback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            DebugLogger.d("Event", "Entrant deleted successfully");
                                                        }
                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            DebugLogger.d("Event", "Entrant not deleted");
                                                            throw new EntrantNotFound("Entrant not found when attempting to delete", id);
                                                        }
                                                    });
                                                })
                                                .addOnFailureListener(e -> {
                                                    DebugLogger.d("Event", "Failed to delete entrant");
                                                    throw new EntrantNotFound("Entrant not found when attempting to delete", id);
                                                });
                                    }

                                }

                                @Override
                                public void onFailure(Exception e) {
                                    //Revert the changes
                                    try {
                                        EntrantController.reEnroll(entrant, remWaitEvents, null);
                                    } catch (Exception ex) {
                                        DebugLogger.d("Event", "Failed to re-enroll entrant");
                                    }

                                    throw new EntrantNotFound("Entrant not found when attempting to remove from waitlist", String.valueOf(id));

                                }
                            });
                        }

                    }
                    @Override
                    public void onFailure(Exception e) {
                        throw new EventNotFound("Events not found when attempting to remove entrants from events", String.valueOf(id));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                throw new EntrantNotFound("Entrant not found when attempting to delete", String.valueOf(id));
            }

        });

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

    private static void reEnroll(Entrant entrant, ArrayList<Event> events, DBWriteCallback callback) {
        for(Event event : events) {
            event.addEntrant(entrant, callback);
        }
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






}
