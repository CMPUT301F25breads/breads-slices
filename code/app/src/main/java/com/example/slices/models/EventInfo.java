package com.example.slices.models;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventInfoChangeListener;
import com.google.firebase.Timestamp;

public class EventInfo {
    /**
     * Name of the event
     */
    private String name;
    /**
     * Description of the event
     */
    private String description; // Probably will be it's own thing later
    /**
     * Location of the event
     */
    private String location; // Will be geolocation object later

    /**
     * Date of the event
     */
    private Timestamp eventDate;
    /**
     * Deadline for registering for the event
     */
    private Timestamp regDeadline;

    /**
     * ID of the event
     */
    private int id;
    /**
     * Maximum number of entrants in the event
     */
    private int maxEntrants;
    /**
     * Current number of entrants in the event
     */
    private int currentEntrants;
    /**
     * Image URL of the event
     */

    private String guidelines;
    private String imageUrl = "https://cdn.mos.cms.futurecdn.net/39CUYMP8vJqHAYGVzUghBX.jpg";

    private transient EventInfoChangeListener changeListener;


    public EventInfo(){

    }

    public EventInfo(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, int id) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
    }

    public EventInfo(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, int id, String imageUrl) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.imageUrl = imageUrl;
    }

    public EventInfo(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, int id, String imageUrl, String guidelines) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.imageUrl = imageUrl;
        this.guidelines = guidelines;
    }

    public void updateEventInfo(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, String imageUrl, String guidelines, DBWriteCallback callback) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (location != null) {
            this.location = location;
        }
        if (eventDate != null) {
            this.eventDate = eventDate;
        }
        if (regDeadline != null) {
            this.regDeadline = regDeadline;
        }
        if (maxEntrants != 0) {
            this.maxEntrants = maxEntrants;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (guidelines != null) {
            this.guidelines = guidelines;
        }
        notifyModified(callback);

    }

    public String getName() {
        return name;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param name
     *      Name to set
     */
    @Deprecated
    public void setName(String name) {
        this.name = name;
    }

    public void updateName(String name, DBWriteCallback callback) {
        this.name = name;
        notifyModified(callback);
    }

    public String getDescription() {
        return description;

    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param description
     *      Description to set
     */
    @Deprecated
    public void setDescription(String description) {
        this.description = description;
    }

    public void updateDescription(String description, DBWriteCallback callback) {
        this.description = description;
        notifyModified(callback);
    }

    public String getGuidelines() {
        return guidelines;
    }

    public void updateGuidelines(String guidelines, DBWriteCallback callback) {
        this.guidelines = guidelines;
        notifyModified(callback);
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param guidelines
     *      Guidelines to set
     */
    @Deprecated
    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }



    public String getLocation() {
        return location;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param location
     *      Location to set
     */
    @Deprecated
    public void setLocation(String location) {
        this.location = location;

    }

    public void updateLocation(String location, DBWriteCallback callback) {
        this.location = location;
        notifyModified(callback);
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param eventDate
     *      Event date to set
     */
    @Deprecated
    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;

    }

    public void updateEventDate(Timestamp eventDate, DBWriteCallback callback) {
        this.eventDate = eventDate;
        notifyModified(callback);
    }

    public Timestamp getRegDeadline() {
        return regDeadline;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param regDeadline
     *      Registration deadline to set
     */
    @Deprecated
    public void setRegDeadline(Timestamp regDeadline) {
        this.regDeadline = regDeadline;

    }

    public void updateRegDeadline(Timestamp regDeadline, DBWriteCallback callback) {
        this.regDeadline = regDeadline;
        notifyModified(callback);
    }


    public int getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param maxEntrants
     *      Maximum number of entrants to set
     */
    @Deprecated
    public void setMaxEntrants(int maxEntrants) {
        this.maxEntrants = maxEntrants;

    }

    public int getCurrentEntrants() {
        return currentEntrants;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param currentEntrants
     *      Current number of entrants to set
     */
    @Deprecated
    public void setCurrentEntrants(int currentEntrants) {
        this.currentEntrants = currentEntrants;
    }

    public void updateCurrentEntrants(int currentEntrants, DBWriteCallback callback) {
        this.currentEntrants = currentEntrants;
        notifyModified(callback);
    }


    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param imageUrl
     *      Image URL to set
     */
    @Deprecated
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateImageUrl(String imageUrl, DBWriteCallback callback) {
        this.imageUrl = imageUrl;
        notifyModified(callback);
    }


    public int getId() {
        return id;
    }

    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param id
     *      ID to set
     */
    @Deprecated
    public void setId(int id) {
        this.id = id;
    }

    public void updateId(int id, DBWriteCallback callback) {
        this.id = id;
        notifyModified(callback);
    }


    public void setChangeListener(EventInfoChangeListener listener) {
        this.changeListener = listener;
    }

    public void notifyModified(DBWriteCallback callback) {
        if (changeListener != null) {
            changeListener.onEventInfoChanged(this, callback);
        } else {
            callback.onFailure(new Exception("No change listener set for EventInfo"));
        }
    }



}
