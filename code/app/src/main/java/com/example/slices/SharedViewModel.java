package com.example.slices;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.SearchSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared model to facilitate communication of data between fragments
 * @author Brad Erdely
 */
public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Entrant> user = new MutableLiveData<>(new Entrant());
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>(new Event());
    private final MutableLiveData<List<Event>> waitlistedEvents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<SearchSettings> search = new MutableLiveData<>(new SearchSettings());
    private final MutableLiveData<List<Event>> pastEvents = new MutableLiveData<>(new ArrayList<>());

    public List<Event> getPastEvents() {
        return pastEvents.getValue();
    }
    public void setPastEvents(List<Event> pastEvents) {
        this.pastEvents.setValue(pastEvents);
    }

    public SearchSettings getSearch() {
        return search.getValue();
    }

    public void setSearch(SearchSettings search) {
        this.search.setValue(search);
    }

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

    public void setSelectedEvent(Event event) {
        this.selectedEvent.setValue(event);
    }
    public Event getSelectedEvent() {
        return selectedEvent.getValue();
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
        Log.d("SharedViewModel", "Added waitlisted ID: " + eventId + " (total: " + copy.size() + ")");
    }

    // removes one event ID from the list if it exists
    public void removeWaitlistedId(String eventId) {
        if (eventId == null) {
            return;
        }
        ArrayList<String> list = waitlistedEventIds.getValue();
        if (list == null || !list.contains(eventId)) {
            Log.d("SharedViewModel", "Attempted to remove non-existent waitlisted ID: " + eventId);
            return;
        }
        ArrayList<String> copy = new ArrayList<>(list);
        if (copy.remove(eventId)) {
            waitlistedEventIds.setValue(copy);
            Log.d("SharedViewModel", "Removed waitlisted ID: " + eventId + " (remaining: " + copy.size() + ")");
        }
    }

    /**
     * Clears all waitlisted event IDs
     * Used when refreshing the complete waitlist state from the database
     */
    public void clearWaitlistedIds() {
        ArrayList<String> current = waitlistedEventIds.getValue();
        int previousSize = (current != null) ? current.size() : 0;
        waitlistedEventIds.setValue(new ArrayList<>());
        Log.d("SharedViewModel", "Cleared all waitlisted IDs (was: " + previousSize + ")");
    }


    // ---- participant handling/removal ------ -Raj
    // Crafted to track the events the entrant is fully participating in
    private final MutableLiveData<ArrayList<String>> participatingEventIds =
            new MutableLiveData<>(new ArrayList<>());

    // Returns read-only list of events the user is participating in
    public LiveData<ArrayList<String>> getParticipatingEventIds() {
        return participatingEventIds;
    }

    // check user is participating or not in an event
    public boolean isParticipating(String eventId) {
        ArrayList<String> list = participatingEventIds.getValue();
        return list != null && list.contains(eventId);
    }

    // add a participating eventId if not already there
    public void addParticipatingId(String eventId) {
        if (eventId == null) {
            return;
        }

        ArrayList<String> current = participatingEventIds.getValue();
        if (current == null) current = new ArrayList<>();

        if (current.contains(eventId)) {
            return;
        }

        ArrayList<String> copy = new ArrayList<>(current);
        copy.add(eventId);
        participatingEventIds.setValue(copy);
        Log.d("SharedViewModel", "Added participating ID: " +
                eventId + " (total: " + copy.size() + ")");
    }

    // remove a participating eventId
    public void removeParticipatingId(String eventId) {
        if (eventId == null) {
            return;
        }

        ArrayList<String> list = participatingEventIds.getValue();
        if (list == null || !list.contains(eventId)) {
            Log.d("SharedViewModel",
                    "Attempted to remove non-existent participating ID: " + eventId);
            return;
        }

        ArrayList<String> copy = new ArrayList<>(list);
        if (copy.remove(eventId)) {
            participatingEventIds.setValue(copy);
            Log.d("SharedViewModel",
                    "Removed participating ID: " +
                            eventId + " (remaining: " + copy.size() + ")");
        }
    }

    // clear EVERYTHING from participating IDs -> might be useful
    public void clearParticipatingIds() {
        ArrayList<String> current = participatingEventIds.getValue();
        int prev = (current != null) ? current.size() : 0;

        participatingEventIds.setValue(new ArrayList<>());
        Log.d("SharedViewModel", "Cleared all participating IDs (was: " + prev + ")");
    }

}