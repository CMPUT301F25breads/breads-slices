package com.example.slices.models;

import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class SearchConditions {
    private String name;
    private String loc;
    private int maxEntrants;
    private Timestamp availStart;
    private Timestamp availEnd;

    public Timestamp getAvailStart() {
        return availStart;
    }

    public void setAvailStart(Date availStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(availStart);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        this.availEnd = new Timestamp(availStart);
    }

    public Timestamp getAvailEnd() {
        return availEnd;
    }

    public void setAvailEnd(Date availEnd) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(availEnd);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        this.availEnd = new Timestamp(availEnd);
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
