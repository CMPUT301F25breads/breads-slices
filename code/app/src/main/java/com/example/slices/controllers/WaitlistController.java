
package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

/**
 * controller for managing waitlist join/leave without touching DBConnector controller.
 *
 * format on firebase looks like this:
 *   events/{eventId}/waitlist/{entrantid}
 *   entrants/{userid}/{id}
 * @Author Raj Prasad
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
    /*
    Ryan - Gonna rewrite this to work with the intended path for the waitlist
     */
    /*public static void join(@NonNull String eventId,
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

    }*/
    public static void join(@NonNull String eventId,
                            @NonNull String userid,
                            @NonNull DBWriteCallback callback) {
        EventController.getEvent(Integer.parseInt(eventId), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                //If we successfully get the event, get the entrant
                EntrantController.getEntrant(Integer.parseInt(userid), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        //If we successfully get the entrant, add them to the waitlist
                        event.addEntrantToWaitlist(entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
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
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });

    }


    // leave an event waitlist: delete both marker docs in the map in a single batch
    /*public static void leave(@NonNull String eventId,
                             @NonNull String userid,
                             @NonNull Runnable onOk,
                             @NonNull Consumer<Exception> onErr) {
        WriteBatch batch = db().batch();
        batch.delete(eventWaitlistRef(eventId, userid));
        batch.commit().addOnSuccessListener(v -> onOk.run())
                .addOnFailureListener(onErr::accept);
    }*/

    public static void leave(@NonNull String eventId,
                             @NonNull String userid,
                             @NonNull DBWriteCallback callback) {
        EventController.getEvent(Integer.parseInt(eventId), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                //Now get the entrant
                EntrantController.getEntrant(Integer.parseInt(userid), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        //Now remove them from the waitlist
                        event.removeEntrantFromWaitlist(entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
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

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
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
