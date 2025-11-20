package com.example.slices.models;


import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.google.firebase.Timestamp;

/**
 * Class representing an invitation
 * @author Ryan Haubrich
 * @version 1.0
 */
public class Invitation extends Notification {
    //private int eventId;

    /**
     * Whether the invitation has been accepted
     */
    private boolean accepted;

    /**
     * Whether the invitation has been declined
     */
    private boolean declined;

    /**
     * Default constructor for the Invitation class, needed for serialization
     */
    public Invitation() {}

    /**
     * Constructor for the Invitation class
     * @param title
     *      Title of the notification
     * @param body
     *      Body text of the notification
     * @param notificationId
     *      ID of the notification
     * @param recipient
     *      ID of the recipient entrant
     * @param sender
     *      ID of the sender entrant
     * @param eventId
     *      ID of the event associated with the invitation
     */
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

    /**
     * Handles the acceptance of the invitation by the recipient
     * Removes the entrant from the event waitlist and adds them to the event
     */
    public void onAccept(EventCallback callback) {
        EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                EntrantController.getEntrant(recipientId, new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        event.getWaitlist().removeEntrant(entrant);
                        event.addEntrant(entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(event);
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

    /**
     * Handles the decline of the invitation by the recipient
     * Removes the entrant from the event waitlist
     */
    public void onDecline(EventCallback callback) {

        EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                EntrantController.getEntrant(recipientId, new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        event.removeEntrantFromWaitlist(entrant, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(event);
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

    /**
     * Getter for whether the invitation has been accepted
     * @return
     *      True if the invitation has been accepted, false otherwise
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Setter for the accepted status of the invitation
     * @param accepted
     *      Whether the invitation is accepted
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * Getter for whether the invitation has been declined
     * @return
     *      True if the invitation has been declined, false otherwise
     */
    public boolean isDeclined() {
        return declined;
    }

    /**
     * Setter for the declined status of the invitation
     * @param declined
     *      Whether the invitation is declined
     */
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
}
