package com.example.slices.models;

import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.google.firebase.Timestamp;

public class Invitation extends Notification {
    //private int eventId;

    private boolean accepted;
    private boolean declined;


    public Invitation(String title, String body, int notificationId, int recipient, int sender, int eventId) {
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.eventId = eventId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.INVITATION;
        this.accepted = false;
        this.declined = false;

    }
    public Invitation(String title, String body, int notificationId, Entrant recipient, Entrant sender, int eventId) {
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.eventId = eventId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.INVITATION;
        this.accepted = false;
        this.declined = false;

    }

    public void onAccept() {
        DBConnector db = new DBConnector();
        db.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                event.getWaitlist().removeEntrant(recipient);
                event.addEntrant(recipient, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void onDecline() {
        DBConnector db = new DBConnector();
        db.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                event.removeEntrantFromWaitlist(recipient, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public int getEventId() {
        return eventId;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public boolean isAccepted() {
        return accepted;
    }
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    public boolean isDeclined() {
        return declined;
    }
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }




}
