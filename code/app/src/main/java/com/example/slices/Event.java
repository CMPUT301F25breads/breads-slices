package com.example.slices;

import android.os.Build;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class representing an entrant
 *
 */
public class Event {
    private String name;

    private String description; // Probably will be it's own thing later

    private String location; // Will be geolocation object later

    private ArrayList<Entrant> entrants; // Represents the entrants in the event

    private Timestamp eventDate;
    private Timestamp regDeadline;

    private Waitlist waitlist;

    private int id;

    private int maxEntrants;

    private int currentEntrants;



    public Event(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();


    }
    public Event(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, int id) throws IllegalArgumentException {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();
        this.id = id;

        //Check if the eventTime is in the past
        //Get the current timestamp
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        Timestamp currentTime = new Timestamp(cal.getTime());

        if (eventDate.compareTo(currentTime) < 0) {
            //Throw exception
            DebugLogger.d("Event", "Event time is in the past");
            throw new IllegalArgumentException("Event time is in the past");
        }

        //Check if the registration deadline is in the past
        if (regDeadline.compareTo(currentTime) < 0) {
            //Throw exception
            DebugLogger.d("Event", "Registration deadline is in the past");
            throw new IllegalArgumentException("Registration deadline is in the past");
        }

        //Check if the registration deadline is after the event time
        if (regDeadline.compareTo(eventDate) > 0) {
            //Throw exception
            DebugLogger.d("Event", "Registration deadline is after event time");
            throw new IllegalArgumentException("Registration deadline is after event time");
        }

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
    public Timestamp getEventDate() {
        return eventDate;
    }
    public Timestamp getRegDeadline() {
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
    public void setEventDate(Timestamp eventDate) {
        //Validate that date has not already passed
        this.eventDate = eventDate;
    }
    public void setRegDeadline(Timestamp regDeadline) {
        //Validate that deadline has not already passed
        this.regDeadline = regDeadline;
    }
    public void setMaxEntrants(int maxEntrants) {
        //Validate that max entrants is not less than current entrants
        this.maxEntrants = maxEntrants;
    }

    public boolean addEntrant(Entrant entrant) {
        //This should never be called directly from somewhere else in the code
        //It only is used for testing and by the lottery
        //Check if the event is full
        if (currentEntrants >= maxEntrants) {
            //Return false
            return false;
        }
        //Check if the entrant is already in the event
        if (entrants.contains(entrant)) {
            //Return false
            return false;
        }
        //Add the entrant to the event
        entrants.add(entrant);
        //Increment the current entrants
        currentEntrants++;
        eventModified();
        //Return true
        return true;
    }

    public void addWaitlist(Entrant entrant) {
        //Add the entrant to the waitlist
        if (waitlist.addEntrant(entrant)) {
            eventModified();
        } else {
            DebugLogger.d("Event", "Entrant already in waitlist");
            throw new IllegalArgumentException("Entrant already in waitlist");
        }
    }

    private void eventModified() {
        //Write to database
        DebugLogger.d("Event", "Event modified");
        DBConnector db = new DBConnector();
        db.updateEvent(this, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                DebugLogger.d("Event", "Event modified successfully");
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Event", "Event modified failed");
            }
        });
    }


















}
