package com.example.slices.models;


import com.example.slices.controllers.EventController;
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
     * List of entrants in the event
     */
    private List<Entrant> entrants; // Represents the entrants in the event
    /**
     * Waitlist for the event
     */
    private Waitlist waitlist;
    /**
     * ID of the event
     */
    private int id;

    private EventInfo eventInfo;




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

        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist();

        EventController.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                Event.this.id = id;
                Event.this.eventInfo = new EventInfo(name, description, location, eventDate, regDeadline, maxEntrants, id);
                Event.this.eventInfo.setChangeListener((info, callback) -> eventModified(callback));

                EventController.writeEvent(Event.this, new DBWriteCallback() {
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
     * Getter for the ID of the event
     * @return
     *      ID of the event
     */
    public int getId() {
        return id;
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
     * Adds an entrant directly to the event
     * This should only be used internally for testing or by the lottery
     * @param entrant
     *      Entrant to add
     * @param callback
     *      Callback for database write completion
     */
    public void addEntrant(Entrant entrant, DBWriteCallback callback) {
        //This should never be called directly from somewhere else in the code
        //It only is used for testing and by the lottery
        //Check if the event is full
        if (eventInfo.getCurrentEntrants() >= eventInfo.getMaxEntrants()) {
            return;
        }
        //Check if the entrant is already in the event
        if (entrants.contains(entrant)) {
            return;
        }
        //Add the entrant to the event
        entrants.add(entrant);
        //Increment the current entrants
        eventInfo.updateCurrentEntrants(eventInfo.getCurrentEntrants() + 1, callback);

    }

    public void removeEntrant(Entrant entrant, DBWriteCallback callback) {
        if (!entrants.contains(entrant)) {
            DebugLogger.d("Event", "Entrant not in event");
            callback.onFailure(new Exception("Entrant not in event"));
        }
        entrants.remove(entrant);
        eventInfo.updateCurrentEntrants(eventInfo.getCurrentEntrants() - 1, callback);




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
        eventInfo.updateCurrentEntrants(eventInfo.getCurrentEntrants() + 1, callback);
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
        eventInfo.updateCurrentEntrants(eventInfo.getCurrentEntrants() - 1, callback);
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
        List<Entrant> winners = lottery.getWinners(waitlist.getEntrants(), eventInfo.getMaxEntrants());
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

        EventController.updateEvent(this, new DBWriteCallback() {
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
        return this.eventInfo.getEventDate().compareTo(other.eventInfo.getEventDate());
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

    public EventInfo getEventInfo() {
        return eventInfo;
    }
    public void setEventInfo(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
        this.eventInfo.setChangeListener((info, callback) -> eventModified(callback));
    }
}
