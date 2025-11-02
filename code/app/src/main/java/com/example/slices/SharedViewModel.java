package com.example.slices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slices.models.Entrant;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared model to facilitate communication of data between fragments
 */
public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Entrant> user = new MutableLiveData<>(new Entrant());
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>(new Event());
    private final MutableLiveData<List<Event>> waitlistedEvents = new MutableLiveData<>(new ArrayList<>());

    public Entrant getUser() {
        return user.getValue();
    }

    public List<Event> getEvents() {
        return events.getValue();
    }
    public List<Event> getWaitlistedEvents() {
        return waitlistedEvents.getValue();
    }

    public void setUser(Entrant user) {
        this.user.setValue(user);
    }

    public void setEvents(List<Event> events) {
        this.events.setValue(events);
    }
    public void setWaitlistedEvents(List<Event> waitlistedEvents) {
        this.waitlistedEvents.setValue(waitlistedEvents);
    }

    private final MutableLiveData<ArrayList<String>> waitlistedEventIds = new
            MutableLiveData<>(new ArrayList<>());

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