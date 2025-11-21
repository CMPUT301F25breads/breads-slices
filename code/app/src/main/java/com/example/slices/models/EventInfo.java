package com.example.slices.models;

import com.example.slices.interfaces.DBWriteCallback;
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
    private Timestamp regStart;

    private Timestamp regEnd;

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

    private int maxWaiting;
    private boolean entrantLoc;

    private String entrantDist;

    private String organizerID;







    public EventInfo(){

    }

    public EventInfo(String name, String description, String location, String guidelines,
                     String imgUrl, Timestamp eventDate, Timestamp regStart, Timestamp regEnd,
                     int maxEntrants, int maxWaiting, boolean entrantLoc, String entrantDist, int id, String organizerID) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regStart = regStart;
        this.regEnd = regEnd;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.id = id;
        this.guidelines = guidelines;
        this.imageUrl = imgUrl;
        this.maxWaiting = maxWaiting;
        this.entrantLoc = entrantLoc;
        this.entrantDist = entrantDist;
        this.organizerID = organizerID;

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

    public void setName(String name) {
        this.name = name;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuidelines() {
        return guidelines;
    }



    /**
     * INTERNAL USE ONLY - Firestore requires this setter
     * Do NOT call this method directly. Use the update counterpart instead.
     * @param guidelines
     *      Guidelines to set
     */
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

    public void setLocation(String location) {
        this.location = location;

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

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;

    }



    public Timestamp getRegStart() {
        return regStart;
    }

    public Timestamp getRegEnd() {
        return regEnd;
    }





    public void setRegStart(Timestamp regStart) {
        this.regStart = regStart;

    }

    public void setRegEnd(Timestamp regEnd) {
        this.regEnd = regEnd;
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
    public void setCurrentEntrants(int currentEntrants) {
        this.currentEntrants = currentEntrants;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public void setId(int id) {
        this.id = id;
    }

    public int getMaxWaiting() {
        return maxWaiting;
    }

    public void setMaxWaiting(int maxWaiting) {
        this.maxWaiting = maxWaiting;
    }

    public boolean getEntrantLoc() {
        return entrantLoc;
    }

    public void setEntrantLoc(boolean entrantLoc) {
        this.entrantLoc = entrantLoc;
    }

    public String getEntrantDist() {
        return entrantDist;
    }

    public void setEntrantDist(String entrantDist) {
        this.entrantDist = entrantDist;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }









}
