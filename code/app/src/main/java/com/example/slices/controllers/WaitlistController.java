
package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * controller for managing waitlist join/leave without touching DBConnector controller.
 *
 * format on firebase looks like this:
 *   events/{eventId}/waitlist/{entrantid}
 *   entrants/{userid}/{id}
 *
 *   Note: I don't know what I'm doing with the DB, so feel free to jump in! -Raj
 *   Note 2: I like hashmaps and dictionaries. -Raj
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

//    Commented this one out because creating a new "waitlist" collection in firebase might be a
//    little weird - possibly causing issues later down the path
//
//    private static DocumentReference userWaitlistRef(@NonNull String userid,
//                                                     @NonNull String eventId) {
//        // this will create a "waitlist" field on the Firebase DB on first run
//        return db().collection("entrants").document(userid)
//                .collection("waitlist").document(eventId);
//    }

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
        batch.commit().addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }

    // check to see if a specific userid is on the waitlist for a given eventID just in case
    // that user decides to leave the waitlist at the exact moment they may receive an invite
    public static void isOnWaitlist(@NonNull String eventId,
                                    @NonNull String userid,
                                    @NonNull Consumer<Boolean> cb) {
        Task<DocumentSnapshot> t = eventWaitlistRef(userid, eventId).get();
        t.addOnSuccessListener(snap -> cb.accept(snap.exists()))
         .addOnFailureListener(e -> cb.accept(false));
    }
}
