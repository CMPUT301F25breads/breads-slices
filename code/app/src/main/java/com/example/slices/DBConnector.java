package com.example.slices;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        entrantRef.document(String.valueOf(id)).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Entrant entrant = documentSnapshot.toObject(Entrant.class);
                callback.onSuccess(entrant);
            } else {
                callback.onFailure(new EntrantNotFound("Entrant not found", String.valueOf(id)));
            }
            }).addOnFailureListener(e -> {
                callback.onFailure(new DBOpFailed("Failed to get entrant"));

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

    





}
