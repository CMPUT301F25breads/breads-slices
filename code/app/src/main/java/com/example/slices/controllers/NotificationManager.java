package com.example.slices.controllers;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.NotificationIDCallback;
import com.example.slices.models.Invitation;
import com.example.slices.models.LogType;
import com.example.slices.models.Notification;

public class NotificationManager {

    private static NotificationManager instance ;
    private static int largestId = 0;
    private DBConnector db = new DBConnector();


    private NotificationManager() {
        // Private constructor to prevent instantiation from outside the class
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void sendNotification(String title, String body, int recipientId, int senderId) {
        //Check if largestID is greater than 0
        if (largestId > 0) {
            //Increment largestID by 1
            largestId++;
        }
        //Otherwise we have to go get the largestID from the database
        else {
            db.getNotificationId(new NotificationIDCallback() {
                @Override
                public void onSuccess(int id) {
                    largestId = id;
                    sendNotification(title, body, recipientId, senderId);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get notification ID: " + e.getMessage());
                }

            });

        }
        //Create a new notification object
        Notification notification = new Notification(title, body, largestId, recipientId, senderId);
        //Write the notification to the database
        db.writeNotification(notification, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                //Send a copy to the logger
                Logger.log(notification);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to write notification: " + e.getMessage());
            }

        });
    }

    public void sendInvitation(String title, String body, int recipientId, int senderId, int eventId) {
        //Check if largestID is greater than 0
        if (largestId > 0) {
            //Increment largestID by 1
            largestId++;
        }
        //Otherwise we have to go get the largestID from the database
        else {
            db.getNotificationId(new NotificationIDCallback() {
                @Override
                public void onSuccess(int id) {
                    largestId = id;
                    sendInvitation(title, body, recipientId, senderId, eventId);
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println("Failed to get notification ID: " + e.getMessage());
                }
            });
        }
        //Create a new notification object
        Invitation invitation = new Invitation(title, body, largestId, recipientId, senderId, eventId);
        //Write the notification to the database
        db.writeNotification(invitation, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                //Send a copy to the logger
                Logger.log(invitation);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Failed to write notification: " + e.getMessage());
            }
        });
    }

}
