package com.example.slices.controllers;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.models.Invitation;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;

/**
 * Singleton class to manage sending notifications and invitations.
 * Handles creating notifications, assigning IDs, writing to the database, and logging.
 * Uses DBConnector for database operations.
 *
 * @author Ryan
 * @version 0.1
 */
public class NotificationManager {

    /**
     * Singleton instance of NotificationManager
     */
    private static NotificationManager instance;

    /**
     * Largest notification ID assigned so far
     */
    private static int largestId = 0;

    /**
     * Database connector used to read/write notifications
     */
    private static DBConnector db = new DBConnector();

    /**
     * Private constructor to prevent external instantiation
     */
    private NotificationManager() {}

    /**
     * Returns the singleton instance of NotificationManager
     * @return NotificationManager instance
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Sends a standard notification to a recipient.
     * Creates a Notification object, writes it to the database, and logs it.
     *
     * @param title Title of the notification
     * @param body Body text of the notification
     * @param recipientId ID of the recipient entrant
     * @param senderId ID of the sender entrant
     * @param callback Callback for success/failure of database write
     */
    public static void sendNotification(String title, String body, int recipientId, int senderId, DBWriteCallback callback) {
        if (largestId > 0) {
            largestId++;
        } else {
            db.getNotificationId(new NotificationIDCallback() {
                @Override
                public void onSuccess(int id) {
                    largestId = id;
                    sendNotification(title, body, recipientId, senderId, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get notification ID: " + e.getMessage());
                }
            });
            return;
        }

        Notification notification = new Notification(title, body, largestId, recipientId, senderId);

        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.log(notification, callback);
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to write notification: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }

    /**
     * Sends an invitation notification for an event.
     * Creates an Invitation object, writes it to the database, and logs it.
     *
     * @param title Title of the invitation
     * @param body Body text of the invitation
     * @param recipientId ID of the recipient entrant
     * @param senderId ID of the sender entrant
     * @param eventId ID of the associated event
     */
    public static void sendInvitation(String title, String body, int recipientId, int senderId, int eventId, DBWriteCallback callback) {
        if (largestId > 0) {
            largestId++;
        } else {
            db.getNotificationId(new NotificationIDCallback() {
                @Override
                public void onSuccess(int id) {
                    largestId = id;
                    sendInvitation(title, body, recipientId, senderId, eventId, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get notification ID: " + e.getMessage());
                    callback.onFailure(e);
                }
            });
            return;
        }

        Invitation invitation = new Invitation(title, body, largestId, recipientId, senderId, eventId);

        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Logger.log(invitation, callback);
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to write notification: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }
}
