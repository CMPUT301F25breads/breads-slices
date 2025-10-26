package com.example.slices;

import java.util.ArrayList;
import java.util.List;

public class Waitlist {
    private List<Entrant> entrants;
    private int maxCapacity;
    private int currentEntrants;


    public Waitlist() {
        // Initialize the waitlist with an empty list of entrants
        this.entrants = new ArrayList<>();
        this.currentEntrants = 0;
        this.maxCapacity = 32768;
    }
    public Waitlist(int maxCapacity) {
        // Initialize the waitlist with an empty list of entrants
        this.entrants = new ArrayList<>();
        this.maxCapacity = maxCapacity;
        this.currentEntrants = 0;
    }

    public boolean addEntrant(Entrant entrant) {
        if (isFull()) {
            return false;
        }
        // Add the entrant to the waitlist
        entrants.add(entrant);
        currentEntrants++;
        return true;
    }

    private boolean isFull() {
        return currentEntrants >= maxCapacity;
    }

    public List<Entrant> getEntrants() {
        return entrants;
    }
    public void removeEntrant(Entrant entrant) {
        entrants.remove(entrant);
    }
    public int getCurrentEntrants() {
        return currentEntrants;
    }
    public void clearWaitlist() {
        entrants.clear();
        currentEntrants = 0;
    }
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public boolean isEmpty() {
        return entrants.isEmpty();
    }
    public Entrant getEntrant(Entrant entrant) {
        return entrants.get(entrants.indexOf(entrant));
    }
    public Entrant getEntrant(int id) {
        for (Entrant entrant : entrants) {
            if (entrant.getId() == id) {
                return entrant;
            }
        }
        return null;
    }




}
