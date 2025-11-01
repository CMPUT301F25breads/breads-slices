package com.example.slices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slices.models.Entrant;

import java.util.ArrayList;

/**
 * Shared model to facilitate communication of data between fragments
 */
public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Entrant> user = new MutableLiveData<>(new Entrant());
    private final MutableLiveData<ArrayList<Event>> events = new MutableLiveData<>(new ArrayList<>());

    // use a simple list of Event IDs to make life easier!
    // this (should) track which events the entrant has joined/waitlisted for
    // -Raj
    private final MutableLiveData<ArrayList<String>> waitlistedEventIds = new
            MutableLiveData<>(new ArrayList<>());

    public Entrant getUser() {
        return user.getValue();
    }

    public ArrayList<Event> getEvents() {
        return events.getValue();
    }

    public void setUser(Entrant user) {
        this.user.setValue(user);
    }

    public void setEvents(ArrayList<Event> events) {
        this.events.setValue(events);
    }

    // ---------   WAITLIST HANDLING ---------  -Raj
    // this will return the read-only of the list of specific events the specific entrant has joined
    // or waitlisted for
    public LiveData<ArrayList<String>> getWaitlistedEventIds() {
        return waitlistedEventIds;
    }

    // bool to check if a specific event ID is in the list
    public boolean isWaitlisted(String eventId) {
        ArrayList<String> list = waitlistedEventIds.getValue();
        return list != null && list.contains(eventId);
    }

    // completely replaces the ENTIRE list of waitlisted events
    // should be used when you load up the user's waitlist from Firestore for the first time btw
    // since it resets waitlistedEventIds to the given IDs -Raj
    public void replaceWaitlistedIds(ArrayList<String> ids) {
        if (ids == null) {
            waitlistedEventIds.setValue(new ArrayList<>());
        } else {
            waitlistedEventIds.setValue(new ArrayList<>(ids));
        }
    }

    /** adds a single eventId to the waitlist if its not already there -Raj */
    public void addWaitlistedId(String eventId) {
        if (eventId == null) {
            return;
        }
        ArrayList<String> current = waitlistedEventIds.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }

        if (current.contains(eventId)) {
            return;
        }

        ArrayList<String> copy = new ArrayList<>(current);
        copy.add(eventId);
        waitlistedEventIds.setValue(copy);
    }

    // removes one event ID from the list if it exists
    public void removeWaitlistedId(String eventId) {
        if (eventId == null) {
            return;
        }
        ArrayList<String> list = waitlistedEventIds.getValue();
        if (list == null || !list.contains(eventId)) {
            return;
        }
        ArrayList<String> copy = new ArrayList<>(list);
        if (copy.remove(eventId)) {
            waitlistedEventIds.setValue(copy);
        }
    }

}