package com.example.slices.models;

import android.location.Location;

import com.google.firebase.Timestamp;

public class SearchSettings {
    private String name = null;
    private String address = null;
    private Location loc;
    private Timestamp availStart = null;
    private Timestamp availEnd = null;
    private int id;

    public SearchSettings(){}

    public Timestamp getAvailStart() {
        return availStart;
    }

    public void setAvailStart(Timestamp availStart) {
        this.availStart = availStart;
    }

    public Timestamp getAvailEnd() {
        return availEnd;
    }

    public void setAvailEnd(Timestamp availEnd) {

        this.availEnd = availEnd;
    }

    public int getId() {
        return id;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(int id) {
        this.id = id;
    }

    private boolean enrolled = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim().toLowerCase();
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void checkEnrolled() {
        enrolled = !enrolled;
    }


    public String getAddress() {
        return address;
    }
}
