package com.example.slices;

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
}