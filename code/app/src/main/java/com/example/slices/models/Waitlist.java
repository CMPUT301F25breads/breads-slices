package com.example.slices.models;

import android.location.Location;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a waitlist for an event
 * @author Ryan Haubrich
 * @version 1.6
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
     * Map of entrant IDs to their join locations (latitude, longitude)
     * Stored as Map<String, Map<String, Double>> for Firestore compatibility
     * Entrant IDs are converted to Strings as Firestore requires String keys
     * Inner map has keys: "latitude" and "longitude"
     */
    private Map<String, Map<String, Double>> entrantLocations;

    /**
     * Default constructor for Waitlist
     * Initializes the waitlist with default maximum capacity (32768) and empty entrant list
     */
    public Waitlist() {
        this.entrants = new ArrayList<>();
        this.currentEntrants = 0;
        this.maxCapacity = 32768;
        this.entrantIds = new ArrayList<>();
        this.entrantLocations = new HashMap<>();
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
        this.entrantLocations = new HashMap<>();
    }

    /**
     * Adds an entrant to the waitlist
     * @param entrant
     *      Entrant to add
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
    
    /**
     * Stores the location where an entrant joined the waitlist
     * @param entrantId
     *      ID of the entrant
     * @param location
     *      Location where they joined (can be null if location not required)
     */
    public void setEntrantLocation(int entrantId, Location location) {
        if (location != null) {
            // Initialize map if null (for backward compatibility with existing events)
            if (entrantLocations == null) {
                entrantLocations = new HashMap<>();
            }
            Map<String, Double> locationMap = new HashMap<>();
            locationMap.put("latitude", location.getLatitude());
            locationMap.put("longitude", location.getLongitude());
            // Convert Integer ID to String key for Firestore compatibility
            entrantLocations.put(String.valueOf(entrantId), locationMap);
        }
    }
    
    /**
     * Gets the location where an entrant joined the waitlist
     * @param entrantId
     *      ID of the entrant
     * @return
     *      Location object, or null if no location was stored
     */
    public Location getEntrantLocation(int entrantId) {
        // Return null if map is not initialized (backward compatibility)
        if (entrantLocations == null) {
            return null;
        }
        // Convert Integer ID to String key for lookup
        Map<String, Double> locationMap = entrantLocations.get(String.valueOf(entrantId));
        if (locationMap != null && locationMap.containsKey("latitude") && locationMap.containsKey("longitude")) {
            Location location = new Location("stored");
            location.setLatitude(locationMap.get("latitude"));
            location.setLongitude(locationMap.get("longitude"));
            return location;
        }
        return null;
    }
    
    /**
     * Gets all entrant locations as a map
     * @return
     *      Map of entrant IDs (as Strings) to their join locations (never null, returns empty map if not initialized)
     */
    public Map<String, Map<String, Double>> getEntrantLocations() {
        // Return empty map if not initialized (backward compatibility)
        if (entrantLocations == null) {
            return new HashMap<>();
        }
        return entrantLocations;
    }
    
    /**
     * Setter for entrant locations (for Firestore deserialization)
     * @param entrantLocations
     *      Map of entrant locations with String keys
     */
    public void setEntrantLocations(Map<String, Map<String, Double>> entrantLocations) {
        this.entrantLocations = entrantLocations != null ? entrantLocations : new HashMap<>();
    }
}
