package com.example.slices.models;

import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.testing.DebugLogger;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Class representing an event
 * @author Ryan Haubrich
 * @version 1.0
 *
 */
public class Event implements Comparable<Event> {
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
     * List of entrants in the event
     */
    private List<Entrant> entrants; // Represents the entrants in the event
    /**
     * Date of the event
     */
    private Timestamp eventDate;
    /**
     * Deadline for registering for the event
     */
    private Timestamp regDeadline;
    /**
     * Waitlist for the event
     */
    private Waitlist waitlist;
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
    private String imageUrl = "https://cdn.mos.cms.futurecdn.net/39CUYMP8vJqHAYGVzUghBX.jpg";
    /**
     * Database connector object
     */
    private final DBConnector db = new DBConnector();


    /**
     * No argument Event constructor
     */
    public Event(){}

    /**
     * Event constructor
     * @param name
     *      Name of the event
     * @param description
     *      Description of the event
     * @param location
     *      Location of the event
     * @param eventDate
     *      Date of the event
     * @param regDeadline
     *      Deadline for registering for the event
     * @param maxEntrants
     *      Maximum number of entrants in the event
     * @param callback
     *      Callback for when the event is created
     * @throws IllegalArgumentException
     *      If the event time is in the past or the registration deadline is in the past or after the event time
     */
    public Event(String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, EventCallback callback) throws IllegalArgumentException {
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
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();

        db.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                Event.this.id = id;
                db.writeEvent(Event.this, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        DebugLogger.d("Event", "Event created successfully");
                        callback.onSuccess(Event.this);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        DebugLogger.d("Event", "Event creation failed");
                        callback.onFailure(e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Event", "Event failed to get new id");
            }
        });
    }

    /**
     * Event constructor for testing
     *
     * @param name
     *      Name of the event
     * @param description
     *      Description of the event
     * @param location
     *      Location of the event
     * @param eventDate
     *      Date of the event
     * @param regDeadline
     *      Deadline for registering for the event
     * @param maxEntrants
     *      Maximum number of entrants in the event
     * @param flag
     *      Flag for testing purposes
     * @param callback
     *      Callback for when the event is created
     */
    public Event (String name, String description, String location, Timestamp eventDate, Timestamp regDeadline, int maxEntrants, boolean flag, EventCallback callback) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.regDeadline = regDeadline;
        this.maxEntrants = maxEntrants;
        this.currentEntrants = 0;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();
        db.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                Event.this.id = id;
                db.writeEvent(Event.this, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        DebugLogger.d("Event", "Event created successfully");
                        callback.onSuccess(Event.this);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        DebugLogger.d("Event", "Event creation failed");
                        callback.onFailure(e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Event", "Event failed to get new id");
            }
        });



    }


    /**
     * Event constructor for testing
     * @param name
     *      Name of the event
     * @param imageUrl
     *      Image URL of the event
     */
    public Event(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    /**
     * Event constructor for testing
     * @param name
     *      Name of the event
     */
    public Event(String name) {
        this.name = name;
    }

    /**
     * Getter for the ID of the event
     * @return
     *      ID of the event
     */
    public int getId() {
        return id;
    }
    /**
     * Getter for the name of the event
     * @return
     *      Name of the event
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the description of the event
     * @return
     *      Description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the location of the event
     * @return
     *      Location of the event
     */
    public String getLocation() {
        return location;
    }

    /**
     * Getter for the date and time of the event
     * @return
     *      Event date and time
     */
    public Timestamp getEventDate() {
        return eventDate;
    }

    /**
     * Getter for the registration deadline
     * @return
     *      Registration deadline as a timestamp
     */
    public Timestamp getRegDeadline() {
        return regDeadline;
    }

    /**
     * Getter for the maximum number of entrants allowed
     * @return
     *      Maximum number of entrants
     */
    public int getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * Getter for the current number of entrants registered
     * @return
     *      Current number of entrants
     */
    public int getCurrentEntrants() {
        return currentEntrants;
    }

    /**
     * Getter for the list of entrants currently in the event
     * @return
     *      List of entrants
     */
    public List<Entrant> getEntrants() {
        return entrants;
    }

    /**
     * Getter for the waitlist associated with this event
     * @return
     *      Waitlist object
     */
    public Waitlist getWaitlist() {
        return waitlist;
    }

    /**
     * Getter for the image URL associated with the event
     * @return
     *      URL of the event image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Setter for the name of the event
     * @param name
     *      Name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for the description of the event
     * @param description
     *      Description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Setter for the location of the event
     * @param location
     *      Location of the event
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Setter for the event date
     * Validates that the date has not already passed
     * @param eventDate
     *      Event date as a timestamp
     */
    public void setEventDate(Timestamp eventDate) {
        //Validate that date has not already passed
        this.eventDate = eventDate;
    }

    /**
     * Setter for the registration deadline
     * Validates that the deadline has not already passed
     * @param regDeadline
     *      Registration deadline as a timestamp
     */
    public void setRegDeadline(Timestamp regDeadline) {
        //Validate that deadline has not already passed
        this.regDeadline = regDeadline;
    }

    /**
     * Setter for the maximum number of entrants allowed
     * Validates that the new maximum is not less than the current number of entrants
     * @param maxEntrants
     *      Maximum number of entrants
     */
    public void setMaxEntrants(int maxEntrants) {
        //Validate that max entrants is not less than current entrants
        this.maxEntrants = maxEntrants;
    }

    /**
     * Setter for the image url
     * @param imageUrl
     *      firebase download url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Adds an entrant directly to the event
     * This should only be used internally for testing or by the lottery
     * @param entrant
     *      Entrant to add
     * @param callback
     *      Callback for database write completion
     * @return
     *      True if entrant added successfully, false otherwise
     */
    public boolean addEntrant(Entrant entrant, DBWriteCallback callback) {
        //This should never be called directly from somewhere else in the code
        //It only is used for testing and by the lottery
        //Check if the event is full
        if (currentEntrants >= maxEntrants) {
            return false;
        }
        //Check if the entrant is already in the event
        if (entrants.contains(entrant)) {
            return false;
        }
        //Add the entrant to the event
        entrants.add(entrant);
        //Increment the current entrants
        currentEntrants++;
        eventModified(callback);
        return true;
    }

    /**
     * Adds an entrant to the event's waitlist
     * @param entrant
     *      Entrant to add to the waitlist
     * @param callback
     *      Callback for database write completion
     */
    public void addEntrantToWaitlist(Entrant entrant, DBWriteCallback callback) {
        //Add the entrant to the waitlist
        waitlist.addEntrant(entrant);
        //Increment the current entrants
        currentEntrants++;
        eventModified(callback);
    }

    /**
     * Removes an entrant from the event's waitlist
     * @param entrant
     *      Entrant to remove from the waitlist
     * @param callback
     *      Callback for database write completion
     */
    public void removeEntrantFromWaitlist(Entrant entrant, DBWriteCallback callback) {
        if(!waitlist.getEntrants().contains(entrant)) {
            DebugLogger.d("Event", "Entrant not in waitlist");
            callback.onFailure(new Exception("Entrant not in waitlist"));
        }
        //Remove the entrant from the waitlist
        waitlist.removeEntrant(entrant);
        //Decrement the current entrants
        currentEntrants--;
        eventModified(callback);
    }

    /**
     * Method that does the lottery
     * May have issues with robustness specifically in restoring the lists in the event of a failure
     *
     * @param callback
     *      Callback to call when the lottery is complete
     */
    public void doLottery(DBWriteCallback callback) {
        //Create a lottery object
        Lottery lottery = new Lottery();
        //Get the winners
        List<Entrant> winners = lottery.getWinners(waitlist.getEntrants(), this.maxEntrants);
        //Add the winners to the event
        if (winners.isEmpty()) {
            DebugLogger.d("Event", "No winners");
            return;
        }
        final int[] completedCount = {0};
        final int totalWinners = winners.size();
        for (Entrant winner : winners) {
            addEntrant(winner, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    DebugLogger.d("Event", "Winner added to event");
                    completedCount[0]++;
                    waitlist.removeEntrant(winner);
                    if (completedCount[0] == totalWinners) {
                        DebugLogger.d("Event", "Lottery complete");
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    DebugLogger.d("Event", "Winner not added to event");
                    callback.onFailure(e);
                }
            });
        }
    }

    /**
     * Updates the event in the database whenever a modification occurs
     * @param callback
     *      Callback for database write completion
     */
    private void eventModified(DBWriteCallback callback) {
        //Write to database
        DebugLogger.d("Event", "Event modified");
        DBConnector db = new DBConnector();
        db.updateEvent(this, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                DebugLogger.d("Event", "Event modified successfully");
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Event", "Event modified failed");
                callback.onFailure(e);
            }
        });
    }

    /**
     * Comparison method so events can be sorted by the earliest date first
     * @param other
     *      Other event to compare to
     * @return
     *      Returns <1, 0, or >1 if the other event is before, at the same time, or after this event
     */
    @Override
    public int compareTo(Event other) {
        return this.eventDate.compareTo(other.eventDate);
    }

    /**
     * Checks if two events are equal by comparing their IDs
     * @param obj
     *      Object to compare against
     * @return
     *      True if both objects represent the same event, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return id == event.id;
    }

    /**
     * Generates a hash code for the event based on its ID
     * @return
     *      Hash code of the event
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Setter for the list of entrants
     * @param entrants
     *      List of entrants to set
     */
    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
    }
}
