package com.example.slices.controllers;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogIDCallback;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;

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
    /**
     * Database connector used to read/write logs
     */
    private static DBConnector db = new DBConnector();


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
            DBConnector db = new DBConnector();
            db.getLogId(new LogIDCallback() {
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
        db.writeLog(new NotificationLogEntry(notification, largestId), new DBWriteCallback() {
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

}
