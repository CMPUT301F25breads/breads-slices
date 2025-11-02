package com.example.slices.models;

import com.example.slices.controllers.DBConnector;
import com.example.slices.testing.DebugLogger;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.testing.TestUtils;

import java.util.ArrayList;
import java.util.List;

//Prototype class for the entrant
public class Entrant {
    private String id;

    private DBConnector db = new DBConnector();

    private List<Profile> profiles;

    public Entrant() {}

    /**
     * Constructor for the Entrant class for creating a primary entrant
     * @param id
     *      ID of the entrant
     * @param callback
     *      Callback to call when the entrant is created
     *
     * @deprecated
     *
     */
    public Entrant(String id, EntrantCallback callback) {
        this.id = id;

        db.writeEntrant(Entrant.this, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                DebugLogger.d("Entrant", "Entrant created successfully");
                callback.onSuccess(Entrant.this);
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Entrant", "Entrant creation failed");
                callback.onFailure(e);
            }
        });
    }



    // Testing constructor
    public Entrant(String name, String email, String phoneNumber, String id, EntrantCallback callback) {

        this.profiles = new ArrayList<Profile>();
        this.profiles.add(new Profile(name, email, phoneNumber, 0));

        db.writeEntrant(Entrant.this, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                DebugLogger.d("Entrant", "Entrant created successfully");
                callback.onSuccess(Entrant.this);
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Entrant", "Entrant creation failed");
                callback.onFailure(e);
            }
        });
    }


    public void updateProfile(String name, String email, String phoneNumber, DBWriteCallback callback) {
        profiles.get(0).setName(name);
        profiles.get(0).setEmail(email);
        profiles.get(0).setPhoneNumber(phoneNumber);
        db.updateEntrant(Entrant.this, callback);

    }

    public void updateProfile(int index, String name, String email, String phoneNumber, DBWriteCallback callback) {
        profiles.get(index).setName(name);
        profiles.get(index).setEmail(email);
        profiles.get(index).setPhoneNumber(phoneNumber);
        db.updateEntrant(Entrant.this, callback);

    }


    public String getName() {
        return profiles.get(0).getName();
    }
    public String getEmail() {
        return profiles.get(0).getEmail();
    }
    public String getPhoneNumber() {
        return profiles.get(0).getPhoneNumber();
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        profiles.get(0).setName(name);
        TestUtils.directCallWarn();
    }
    public void setEmail(String email) {
        profiles.get(0).setEmail(email);
        TestUtils.directCallWarn();


    }
    public void setPhoneNumber(String phoneNumber) {
        profiles.get(0).setPhoneNumber(phoneNumber);
        TestUtils.directCallWarn();

    }
    public void setId(String id) {
        this.id = id;
        TestUtils.directCallWarn();

    }


    public void addProfile(String name, String email, String phoneNumber, DBWriteCallback callback) {
        profiles.add(new Profile(name, email, phoneNumber, profiles.size()));
        db.updateEntrant(Entrant.this, callback);
    }


    public void removeProfile(int index, DBWriteCallback callback) {
        profiles.remove(index);
        db.updateEntrant(Entrant.this, callback);
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        TestUtils.directCallWarn();

    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entrant entrant = (Entrant) o;
        return id == entrant.id; // or whatever uniquely identifies an Entrant
    }





}
