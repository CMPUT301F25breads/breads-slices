package com.example.slices.models;

import com.google.firebase.Timestamp;

public class SearchSettings {
    private String name = null;
    private String loc = null;
    private int maxEntrants = -1;
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

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    public int getMaxEntrants() {
        return maxEntrants;
    }

    public void setMaxEntrants(int maxEntrants) {
        this.maxEntrants = maxEntrants;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
