package com.example.slices.controllers;


import android.util.Log;

import com.example.slices.interfaces.DBWriteCallback;

import com.example.slices.interfaces.LogListCallback;

import com.example.slices.models.LogEntry;
import com.example.slices.models.LogType;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private enum Mode {
        LOCAL,
        DATABASE
    }

    private static Mode mode = Mode.LOCAL;




    /**
     * Private constructor to prevent external instantiation
     */
    private Logger() {
        // Private constructor to prevent instantiation from outside the class
    }

    public static void setTesting(boolean testing) {
        if (testing) {
            logRef = db.collection("test_logs");
        } else {
            logRef = db.collection("logs");
        }
    }

    public static void setMode(boolean local){
        if (local){
            mode = Mode.LOCAL;
        } else {
            mode = Mode.DATABASE;
        }
    }

    public static void logAction(LogType type, String description, Map<String, Object> data, DBWriteCallback callback) {
        // If in LOCAL mode → write to Logcat only
        if (mode == Mode.LOCAL) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(type).append("] ").append(description);

            if (data != null && !data.isEmpty()) {
                sb.append(" | data=");
                sb.append(data.toString());
            }
            Log.d("Logger", sb.toString());
            if (callback != null) callback.onSuccess();
            return;
        }
        DocumentReference ref = logRef.document();
        String id = ref.getId();
        LogEntry entry = new LogEntry(id, Timestamp.now(), description, type, data);
        ref.set(entry)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public static void logEntrantJoin(int entrantId, int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("entrantId", entrantId, "eventId", eventId);
        logAction(LogType.ENTRANT_JOINED, "Entrant joined event", data, callback);
    }

    public static void logEntrantLeft(int entrantId, int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("entrantId", entrantId, "eventId", eventId);
        logAction(LogType.ENTRANT_LEFT, "Entrant left event", data, callback);
    }

    public static void logEntrantUpdate(int entrantId, int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("entrantId", entrantId, "eventId", eventId);
        logAction(LogType.ENTRANT_UPDATED, "Entrant updated", data, callback);
    }

    public static void logEventUpdate(int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId);
        logAction(LogType.EVENT_UPDATED, "Event updated", data, callback);
    }

    public static void logEventDelete(int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId);
        logAction(LogType.EVENT_DELETED, "Event deleted", data, callback);
    }

    public static void logEventCreate(int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId);
        logAction(LogType.EVENT_CREATED, "Event created", data, callback);
    }

    public static void logLotteryRun(int eventId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId);
        logAction(LogType.LOTTERY_RUN, "Lottery run", data, callback);
    }

    public static void logInvSent(int eventId, int entrantId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId, "entrantId", entrantId);
        logAction(LogType.INVITATION_SENT, "Invitation sent from " + eventId + " to " + entrantId, data, callback);
    }

    public static void logInvAccepted(int eventId, int entrantId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId, "entrantId", entrantId);
        logAction(LogType.INVITATION_ACCEPTED, "Invitation accepted", data, callback);
    }

    public static void logInvDeclined(int eventId, int entrantId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("eventId", eventId, "entrantId", entrantId);
        logAction(LogType.INVITATION_DECLINED, "Invitation declined", data, callback);
    }

    public static void logSystem(String message, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("message", message);
        logAction(LogType.SYSTEM, "System message", data, callback);
    }

    public static void logError(String message, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("message", message);
        logAction(LogType.ERROR, "Error message", data, callback);
    }

    public static void logNotification(String message, int recipientId, int senderId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("message", message, "recipientId", recipientId, "senderId", senderId);
        logAction(LogType.NOTIFICATION_SENT, "Notification", data, callback);
    }

    public static void logWaitlistModified(String message, int eventId, int entrantId, DBWriteCallback callback) {
        Map<String, Object> data = Map.of("message", message, "eventId", eventId, "entrantId", entrantId);
        logAction(LogType.WAITLIST_MODIFIED, "Waitlist modified", data, callback);
    }


    public static void getLogsOfType(LogType type, LogListCallback cb) {
        logRef.whereEqualTo("type", type)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(q -> {
                    List<LogEntry> list = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        list.add(d.toObject(LogEntry.class));
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public static void getLogsForEvent(int eventId, LogListCallback cb) {
        logRef.whereEqualTo("eventId", eventId)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(q -> {
                    List<LogEntry> list = new ArrayList<>();
                    for (DocumentSnapshot d : q.getDocuments()) {
                        list.add(d.toObject(LogEntry.class));
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onFailure);
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
