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

    public LiveData<Entrant> getUser() {
        return user;
    }
    public LiveData<ArrayList<Event>> getEvents() {
        return events;
    }

    public void setUser(Entrant user) {
        this.user.setValue(user);
    }

    public void setEvents(ArrayList<Event> events) {
        this.events.setValue(events);
    }

}