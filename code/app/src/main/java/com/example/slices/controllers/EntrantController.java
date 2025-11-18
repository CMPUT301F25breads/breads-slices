package com.example.slices.controllers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.models.Entrant;
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
    public static void deleteEntrant(String id) {
        entrantRef.document(id).delete();
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




}
