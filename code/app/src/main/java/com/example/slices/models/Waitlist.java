package com.example.slices.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a waitlist for an event
 * @author Ryan Haubrich
 * @version 1.5
 */

@IgnoreExtraProperties
public class Waitlist {
    /**
     * List of entrants on the waitlist
     */
    private List<Entrant> entrants;

    private List<Integer> entrantIds;

    /**
     * Maximum capacity of the waitlist
     */
    private int maxCapacity;

    /**
     * Current number of entrants on the waitlist
     */
    private int currentEntrants;

    /**
     * Default constructor for Waitlist
     * Initializes the waitlist with default maximum capacity (32768) and empty entrant list
     */
    public Waitlist() {
        this.entrants = new ArrayList<>();
        this.currentEntrants = 0;
        this.maxCapacity = 32768;
        this.entrantIds = new ArrayList<>();
    }

    /**
     * Constructor for Waitlist with custom maximum capacity
     * @param maxCapacity
     *      Maximum number of entrants allowed on the waitlist
     */
    public Waitlist(int maxCapacity) {
        this.entrants = new ArrayList<>();
        this.maxCapacity = maxCapacity;
        this.currentEntrants = 0;
        this.entrantIds = new ArrayList<>();
    }

    /**
     * Adds an entrant to the waitlist
     * @param entrant
     *      Entrant to add
     * @throws IllegalStateException
     *      If the waitlist is already full
     */
    public void addEntrant(Entrant entrant) {
        entrants.add(entrant);
        entrantIds.add((Integer)entrant.getId());
        currentEntrants++;
    }

    /**
     * Checks if the waitlist is full
     * @return
     *      True if the waitlist has reached maximum capacity, false otherwise
     */
    private boolean isFull() {
        return currentEntrants >= maxCapacity;
    }

    /**
     * Getter for the list of entrants on the waitlist
     * @return
     *      List of entrants
     */
    public List<Entrant> getEntrants() {
        return entrants;
    }

    /**
     * Removes an entrant from the waitlist
     * @param entrant
     *      Entrant to remove
     */
    public void removeEntrant(Entrant entrant) {
        entrants.remove(entrant);
        entrantIds.remove((Integer)entrant.getId());
        currentEntrants--;
    }

    /**
     * Getter for the current number of entrants on the waitlist
     * @return
     *      Number of entrants currently on the waitlist
     */
    public int getCurrentEntrants() {
        return currentEntrants;
    }

    /**
     * Clears all entrants from the waitlist
     */
    public void clearWaitlist() {
        entrants.clear();
        currentEntrants = 0;
    }

    /**
     * Setter for the maximum capacity of the waitlist
     * @param maxCapacity
     *      Maximum number of entrants allowed on the waitlist
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Getter for the maximum capacity of the waitlist
     * @return
     *      Maximum number of entrants allowed
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Checks if the waitlist is empty
     * @return
     *      True if no entrants are on the waitlist, false otherwise
     */
    public boolean isEmpty() {
        return entrants.isEmpty();
    }

    /**
     * Gets an entrant from the waitlist by matching an Entrant object
     * @param entrant
     *      Entrant to find
     * @return
     *      The matching Entrant object from the waitlist, or null if not found
     */
    public Entrant getEntrant(Entrant entrant) {
        if (!entrants.contains(entrant)) {
            return null;
        }
        return entrants.get(entrants.indexOf(entrant));
    }

    /**
     * Gets an entrant from the waitlist by entrant ID
     * @param id
     *      ID of the entrant to find
     * @return
     *      The matching Entrant object, or null if no entrant has the given ID
     */
    public Entrant getEntrant(int id) {
        for (Entrant entrant : entrants) {
            if (entrant.getId() == id) {
                return entrant;
            }
        }
        return null;
    }

    public List<Integer> getEntrantIds() {
        return entrantIds;
    }

    public void setEntrantIds(List<Integer> entrantIds) {
        this.entrantIds = entrantIds;
    }

    /**
     * Setter for the list of entrants on the waitlist
     * @param entrants
     *      New list of entrants
     */
    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
    }

    /**
     * Setter for the current number of entrants on the waitlist
     * @param currentEntrants
     *      New number of entrants
     */
    public void setCurrentEntrants(int currentEntrants) {
        this.currentEntrants = currentEntrants;
    }
}
