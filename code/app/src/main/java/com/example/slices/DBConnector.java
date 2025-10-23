package com.example.slices;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class DBConnector {
    private FirebaseFirestore db;
    private CollectionReference entrantRef;
    private CollectionReference authRef;
    private CollectionReference eventRef;







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
    public void getEntrant(int id, EntrantCallback callback) {
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

    public boolean writeEntrant(Entrant entrant) {
        try {
            entrantRef.document(String.valueOf(entrant.getId())).set(entrant);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteEntrant(String id) {
        entrantRef.document(id).delete();
    }

    /**
     * Gets the next available event ID
     * @return
     *      The next available event ID
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

    public void writeEvent(Event event) {
        eventRef.document(String.valueOf(event.getId())).set(event);
    }

    public void deleteEvent(String id) {
        eventRef.document(id).delete();
    }




    





}
