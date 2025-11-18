package com.example.slices.controllers;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogIDCallback;
import com.example.slices.interfaces.LogListCallback;
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
    private static int largestId = 0;

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
        //log the notification to the database
        //Check if largestID is greater than 0
        if (largestId > 0) {
            //Increment largestID by 1
            largestId++;
        }
        //Otherwise we have to go get the largestID from the database
        else {
            getLogId(new LogIDCallback() {
                @Override
                public void onSuccess(int id) {
                    largestId = id;
                    log(notification, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get log ID: " + e.getMessage());
                    callback.onFailure(e);
                }

            });

        }
        writeLog(new NotificationLogEntry(notification, largestId), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Logged notification successfully");
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to log notification: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }
    /**
     * Gets the next available log ID
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getLogId(LogIDCallback callback) {
        logRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the highest ID
                            int highestId = 0;
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                if (doc.getLong("id") != null) {
                                    int id = doc.getLong("id").intValue();
                                    if (id > highestId) {
                                        highestId = id;
                                    }

                                }
                            }
                            // Return the next ID
                            if (highestId != 0) {
                                callback.onSuccess(highestId + 1);
                            } else {
                                callback.onSuccess(1);
                            }
                        }
                        else {
                            callback.onSuccess(1);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get next log ID"));


                    }
                });

    }

    /**
     * Writes a log to the database
     * @param log
     *      Log to write to the database
     * @param callback
     *      Callback to call when the operation is complete
     */
    public static void writeLog(LogEntry log, DBWriteCallback callback) {
        logRef.document(String.valueOf(log.getLogId()))
                .set(log)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(new DBOpFailed("Failed to write log")));
    }

    /**
     * Probably not particularly useful
     * @param callback
     *      Callback to call when the operation is complete
     */

    public static void getAllNotificationLogs(LogListCallback callback) {
        logRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<LogEntry> logs = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                NotificationLogEntry log = doc.toObject(NotificationLogEntry.class);
                                logs.add(log);
                            }
                            callback.onSuccess(logs);
                        } else {
                            callback.onSuccess(new ArrayList<LogEntry>());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(new DBOpFailed("Failed to get logs"));


                    }
                });
    }

    /**
     * Deletes a log from the database
     * @param id
     *      ID of the log to delete
     */

    public static void deleteLog(String id) {
        logRef.document(id).delete();
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
