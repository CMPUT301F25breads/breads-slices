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

    public interface EntrantCallback {
        void onSuccess(Entrant entrant);
        void onFailure(Exception e);
    }


    public DBConnector() {
        db = FirebaseFirestore.getInstance();
        entrantRef = db.collection("entrants");
        authRef = db.collection("auth");
        eventRef = db.collection("events");
    }

    // Method to get a single entrant by their ID
    public Entrant getEntrant(String id, final EntrantCallback callback) {
        entrantRef.document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    callback.onSuccess(entrant);
                } else {
                    callback.onFailure(new EntrantNotFound("Entrant not found", id));
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
        return null;

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
