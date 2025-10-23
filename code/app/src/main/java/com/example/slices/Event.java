package com.example.slices;

import com.google.type.DateTime;

import java.util.ArrayList;

/**
 * Class representing an entrant
 *
 */
public class Event {
    private String name;

    private String description; // Probably will be it's own thing later

    private String location; // Will be geolocation object later

    private ArrayList<Entrant> entrants; // Represents the entrants in the event

    private DateTime eventDate;
    private DateTime regDeadline;

    private Waitlist waitlist;

    private int id;

    private int maxEntrants;

    private int currentEntrants;

    private DBConnector db = new DBConnector();


    public Event(String name, String description, String location, DateTime eventDate, DateTime regDeadline, int maxEntrants) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();
        this.id = db.getNewEventId();

    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getLocation() {
        return location;
    }
    public DateTime getEventDate() {
        return eventDate;
    }
    public DateTime getRegDeadline() {
        return regDeadline;
    }
    public int getMaxEntrants() {
        return maxEntrants;
    }
    public int getCurrentEntrants() {
        return currentEntrants;
    }
    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }
    public Waitlist getWaitlist() {
        return waitlist;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setEventDate(DateTime eventDate) {
        //Validate that date has not already passed
        this.eventDate = eventDate;
    }
    public void setRegDeadline(DateTime regDeadline) {
        //Validate that deadline has not already passed
        this.regDeadline = regDeadline;
    }
    public void setMaxEntrants(int maxEntrants) {
        //Validate that max entrants is not less than current entrants
        this.maxEntrants = maxEntrants;
    }
















}
