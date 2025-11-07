
package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** @Author: Raj Prasad
 * controller for managing waitlist join/leave without touching DBConnector controller.
 *
 * format on firebase looks like this:
 *   events/{eventId}/waitlist/{entrantid}
 *   updated a batch update: waitlist.entrants: [userid1, userid2, ... ]
 */
public class WaitlistController {

    private static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

    private static DocumentReference eventWaitlistRef(@NonNull String eventId,
                                                      @NonNull String userid) {
        return db().collection("events").document(eventId)
                .collection("waitlist").document(userid);
    }

    //  mirror docs under both paths in a single batch
    public static void join(@NonNull String eventId,
                            @NonNull String userid,
                            @NonNull Runnable onOk,
                            @NonNull Consumer<Exception> onErr) {
        WriteBatch batch = db().batch();
        Map<String, Object> marker = new HashMap<>(); // empty marker doc
        batch.set(db().collection("entrants").document(userid), new HashMap<>(),
                SetOptions.merge());
        batch.set(eventWaitlistRef(eventId, userid), marker);
        // NEW! updating fields inside each event doc on DB
        batch.update(
                db().collection("events").document(eventId),
                "waitlist.entrants",
                FieldValue.arrayUnion(userid));
        batch.commit().addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // leave an event waitlist: delete both marker docs in the map in a single batch
    public static void leave(@NonNull String eventId,
                             @NonNull String userid,
                             @NonNull Runnable onOk,
                             @NonNull Consumer<Exception> onErr) {
        WriteBatch batch = db().batch();
        batch.delete(eventWaitlistRef(eventId, userid));
        // NEW! updating the batch in each event doc on db
        batch.update(
                db().collection("events").document(eventId),
                "waitlist.entrants",
                FieldValue.arrayRemove(userid));
        batch.commit().addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // check to see if a specific userid is on the waitlist for a given eventID just in case
    // that user decides to leave the waitlist at the exact moment they may receive an invite
    public static void isOnWaitlist(@NonNull String eventId,
                                    @NonNull String userid,
                                    @NonNull Consumer<Boolean> cb) {
        Task<DocumentSnapshot> t = eventWaitlistRef(eventId, userid).get();
        t.addOnSuccessListener(snap -> cb.accept(snap.exists()))
         .addOnFailureListener(e -> cb.accept(false));
    }
}
