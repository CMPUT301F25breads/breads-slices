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

    public LiveData<Entrant> getUser() {
        return user;
    }

    /**public void setEntrant(Entrant entrant) {
        user.setValue(entrant);
        ArrayList<Entrant> updated = user.getValue();
        if(updated != null) {
            updated.add(a);
            stuff.setValue(updated);
        }
    }*/
}