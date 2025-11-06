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
 * @version 0.1
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


    // Constructors for testing stuff
    public Event(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
    public Event(String name) {
        this.name = name;
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
    public List<Entrant> getEntrants() {
        return entrants;
    }
    public Waitlist getWaitlist() {
        return waitlist;
    }
    public String getImageUrl() {
        return imageUrl;
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

    public boolean addEntrant(Entrant entrant, DBWriteCallback callback) {
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
        eventModified(callback);
        //Return true
        return true;
    }

    public void addEntrantToWaitlist(Entrant entrant, DBWriteCallback callback) {
        //Add the entrant to the waitlist
        waitlist.addEntrant(entrant);
        //Increment the current entrants
        currentEntrants++;
        eventModified(callback);
    }

    public void removeEntrantFromWaitlist(Entrant entrant, DBWriteCallback callback) {
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
     *      other Event to compare to
     * @return
     *      returns <1, 0, or >1 if other event is before, same time, or after the current event
     */
    @Override
    public int compareTo(Event other) {
        return this.eventDate.compareTo(other.eventDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }



    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
    }


}
