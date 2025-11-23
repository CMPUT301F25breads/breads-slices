package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogIDCallback;
import com.example.slices.interfaces.LogListCallback;
import com.example.slices.models.Invitation;
import com.example.slices.models.InvitationLogEntry;
import com.example.slices.models.LogEntry;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to log notifications.
 * Handles creating log entries, assigning IDs, writing to the database, and logging.
 * Uses DBConnector for database operations.
 * @author Ryan Haubrich
 * @version 1.0
 */
public class Logger {
    /**
     * Singleton instance of Logger
     */
    private static Logger instance;
    /**
     * Largest log ID assigned so far
     */

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CollectionReference logRef = db.collection("logs");



    /**
     * Private constructor to prevent external instantiation
     */
    private Logger() {
        // Private constructor to prevent instantiation from outside the class
    }

    /**
     * Returns the singleton instance of Logger
     * @return
     *      Logger instance
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static void setTesting(boolean testing) {
        if (testing) {
            logRef = db.collection("test_logs");
        } else {
            logRef = db.collection("logs");
        }
    }

    /**
     * Logs a notification.
     * Creates a NotificationLogEntry object, writes it to the database, and logs it.
     * @param notification
     *      Notification to log
     */
    public static void log(Notification notification, DBWriteCallback callback) {
        //Create Firestore doc with auto-generated ID.
        DocumentReference ref = logRef.document();
        String id = ref.getId();

        //Create Log Entry
        LogEntry entry = new NotificationLogEntry(notification, id);

        ref.set(entry)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Logged notification successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to log notification: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }

    public static void log(Invitation inv, DBWriteCallback callback) {
        //Create Firestore doc with auto-generated ID.
        DocumentReference ref = logRef.document();
        String id = ref.getId();

        //Create Log Entry
        LogEntry entry = new InvitationLogEntry(inv, id);

        ref.set(entry)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Logged invitation successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to log invitation: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }




    /**
     * Probably not particularly useful
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getAllNotificationLogs(LogListCallback callback) {
        logRef.whereEqualTo("type", LogType.NOTIFICATION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<LogEntry> logs = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            LogEntry log = doc.toObject(NotificationLogEntry.class);
                            logs.add(log);
                        }
                        callback.onSuccess(logs);
                    } else {
                        callback.onSuccess(new ArrayList<LogEntry>());
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(new DBOpFailed("Failed to get notification logs"));
                });
    }

    /**
     * Deletes a log from the database
     * @param id
     *      ID of the log to delete
     */

    public static void deleteLog(String id, DBWriteCallback callback) {
        logRef.document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Deleted log successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to delete log: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }




    /**
     * Gets all invitation logs from the database asynchronously
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getAllInvitationLogs(LogListCallback callback) {
        logRef.whereEqualTo("type", LogType.INVITATION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<LogEntry> logs = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                LogEntry log = doc.toObject(InvitationLogEntry.class);
                                logs.add(log);
                            }
                            callback.onSuccess(logs);
                        } else {
                            callback.onSuccess(new ArrayList<LogEntry>());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get invitation logs"));
                    }

                });

    }

    public static void clearLogs(Runnable onComplete) {
        logRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(logRef.document(doc.getId()).delete());
                    }
                    // Wait for all deletes to finish
                    Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener(aVoid -> onComplete.run());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to clear logs: " + e.getMessage());
                    onComplete.run();
                });
    }

}
