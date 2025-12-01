package com.example.slices.models;


import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.google.firebase.Timestamp;

/**
 * Class representing a not selected notification
 * @author Bhupinder
 */
public class NotSelected extends Notification {
    /**
     * Whether the entrant stayed in the waitlist
     */
    private boolean stayed;

    /**
     * Whether the entrant declined
     */
    private boolean declined;

    /**
     * Default constructor for the Invitation class, needed for serialization
     */
    public NotSelected() {}

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
    public NotSelected(String title, String body, String notificationId, int recipient, int sender, int eventId) {
        this.title = title;
        this.body = body;
        this.id = notificationId;
        this.recipientId = recipient;
        this.senderId = sender;
        this.eventId = eventId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.NOT_SELECTED;
        this.stayed = false;
        this.declined = false;
    }


    /**
     * Getter for whether the entrant stayed in the waitlist
     * @return stayed
     *      True if the entrant stayed, false otherwise
     */
    public boolean isStayed() {
        return stayed;
    }

    /**
     * Setter for whether the entrant stayed in the waitlist
     * @param stayed
     */
    public void setStayed(boolean stayed) {
        this.stayed = stayed;
    }

    /**
     * Getter for whether the entrant declined
     * @return declined
     *      True if the entrant declined, false otherwise
     */
    public boolean isDeclined() {
        return declined;
    }

    /**
     * Setter for whether the entrant declined
     * @param declined
     */
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
}
