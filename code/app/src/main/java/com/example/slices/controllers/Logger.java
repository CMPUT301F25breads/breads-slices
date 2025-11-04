package com.example.slices.controllers;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.LogIDCallback;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationLogEntry;

public class Logger {
    private static Logger instance;
    private static int largestId = 0;
    private static DBConnector db = new DBConnector();



    private Logger() {
        // Private constructor to prevent instantiation from outside the class
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static void log(Notification notification) {
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
                    log(notification);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get log ID: " + e.getMessage());
                }

            });

        }
        db.writeLog(new NotificationLogEntry(notification, largestId), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Logged notification successfully");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to log notification: " + e.getMessage());
            }
        });
    }





}
