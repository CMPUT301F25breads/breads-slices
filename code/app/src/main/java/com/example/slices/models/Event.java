package com.example.slices.models;


import android.location.Location;

import com.example.slices.exceptions.DuplicateEntry;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.exceptions.EventFull;
import com.example.slices.exceptions.WaitlistFull;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Class representing an event
 * @author Ryan Haubrich
 * @version 1.5
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
    private List<Integer> entrantIds;

    private ArrayList<Location> entrantLocs;




    /**
     * No argument Event constructor
     */
    public Event(){}



    public Event(String name, String description, String address, String guidelines, String imgUrl,
                 Timestamp eventDate, Timestamp regStart, Timestamp regEnd, int maxEntrants,
                 int maxWaiting, boolean entrantLoc, String entrantDist, int id, int organizerID,
                 Location location)  {
        this.eventInfo = new EventInfo(name, description, address,  guidelines, imgUrl,
                eventDate, regStart, regEnd, maxEntrants, maxWaiting, entrantLoc, entrantDist, id, organizerID, location);
        this.id = id;
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist(maxWaiting);
        this.entrantIds = new ArrayList<>();
    }

    public Event(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
        this.id = eventInfo.getId();
        this.entrants = new ArrayList<Entrant>();
        this.waitlist = new Waitlist(eventInfo.getMaxWaiting());
        this.entrantIds = new ArrayList<>();
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
     */
    public boolean addEntrant(Entrant entrant) {
        //This should never be called directly from somewhere else in the code
        //It only is used for testing and by the lottery
        //Check if the event is full
        if (eventInfo.getCurrentEntrants() >= eventInfo.getMaxEntrants()) {
            throw new EventFull("Event is full");
        }
        //Check if the entrant is already in the event
        if (entrants.contains(entrant)) {
            throw new DuplicateEntry("Entrant is already in the event");
        }
        //Add the entrant to the event
        entrants.add(entrant);
        entrantIds.add((Integer)entrant.getId());
        //Increment the current entrants
        eventInfo.setCurrentEntrants(eventInfo.getCurrentEntrants() + 1);
        return true;
    }

    public boolean removeEntrant(Entrant entrant) {
        if (!entrants.contains(entrant)) {
            throw new EntrantNotFound("Entrant not in event", String.valueOf(entrant.getId()));
        }
        entrants.remove(entrant);
        entrantIds.remove((Integer)entrant.getId());
        eventInfo.setCurrentEntrants(eventInfo.getCurrentEntrants() - 1);
        return true;
    }

    public List<Integer> getEntrantIds() {
        return entrantIds;
    }

    public void setEntrantIds(List<Integer> entrantIds) {
        this.entrantIds = entrantIds;
    }

    /**
     * Adds an entrant to the event's waitlist
     * @param entrant
     *      Entrant to add to the waitlist
     */
    public boolean addEntrantToWaitlist(Entrant entrant) {
        //Check if the waitlist is full
        if (waitlist.getEntrants().size() >= waitlist.getMaxCapacity()) {
            throw new WaitlistFull("Waitlist is full");
        }
        //Check if the entrant is already in the waitlist
        if (waitlist.getEntrants().contains(entrant)) {
            throw new DuplicateEntry("Entrant is already in the waitlist");
        }
        //Otherwise add the entrant to the waitlist
        waitlist.addEntrant(entrant);
        return true;
    }

    /**
     * Removes an entrant from the event's waitlist
     * @param entrant
     *      Entrant to remove from the waitlist
     */
    public boolean removeEntrantFromWaitlist(Entrant entrant) {
        if(!waitlist.getEntrants().contains(entrant)) {
            return false;
        }
        //Should not be possible to get to this one
        if(waitlist.getEntrants().isEmpty()) {
            return false;
        }
        //Otherwise remove the entrant from the waitlist
        waitlist.removeEntrant(entrant);
        return true;
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
    }
}
