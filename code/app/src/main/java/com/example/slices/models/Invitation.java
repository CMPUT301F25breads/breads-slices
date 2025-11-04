package com.example.slices.models;

import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.google.firebase.Timestamp;

public class Invitation extends Notification {
    //private int eventId;

    private boolean accepted;
    private boolean declined;

    public Invitation() {
    }

    public Invitation(String title, String body, int notificationId, int recipient, int sender, int eventId) {
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.recipientId = recipient;
        this.senderId = sender;
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
                db.getEntrant(recipientId, new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        event.getWaitlist().removeEntrant(entrant);
                        event.addEntrant(entrant, new DBWriteCallback() {
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
                db.getEntrant(recipientId, new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        event.removeEntrantFromWaitlist(entrant, new DBWriteCallback() {
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
            @Override
            public void onFailure(Exception e) {
            }
        });
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
