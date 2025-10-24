package com.example.slices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Shared model to facilitate communication of data between fragments
 */
public class SharedViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<String>> stuff = new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<String>> getStuff() {
        return stuff;
    }

    public void addStuff(String a) {
        ArrayList<String> updated = stuff.getValue();
        if(updated != null) {
            updated.add(a);
            stuff.setValue(updated);
        }
    }
}