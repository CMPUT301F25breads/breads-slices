package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class testEvent {
    private Timestamp GoodEventTime;
    private Timestamp GoodRegEndTime;
    private Timestamp BadEventTime;
    private Timestamp BadRegEndTime;

    private Timestamp PastRegEndTime;






    @Before
    public void setup() {
        Calendar cal = Calendar.getInstance();
        //Set good event date and time - must be in future
        cal.set(2025, 11,25, 12, 30, 0);
        Date date = cal.getTime();
        GoodEventTime = new Timestamp(date);

        //Set good registration end date and time - must be in future and before event time
        cal.set(2025, 11,24, 12, 30, 0);
        date = cal.getTime();
        GoodRegEndTime = new Timestamp(date);

        //Set bad event date and time - in past
        cal.set(2020, 11,25, 12, 30, 0);
        date = cal.getTime();
        BadEventTime = new Timestamp(date);

        //Set bad registration end date and time - after event time
        cal.set(2025, 11,26, 12, 30, 0);
        date = cal.getTime();
        BadRegEndTime = new Timestamp(date);

        //Set bad registration end date and time - in past
        cal.set(2020, 11,25, 12, 30, 0);
        date = cal.getTime();
        PastRegEndTime = new Timestamp(date);









    }



    @Test
    public void testEventCreationTimes() {

        //Normal event creation
        Event event = new Event("Test Event", "Test Location", "Test Description", GoodEventTime, GoodRegEndTime, 1, 1);
        assert event.getName().equals("Test Event");

        //Test that exception is thrown if event time is in the past
        try {
            Event badEvent = new Event("Test Event", "Test Location", "Test Description", BadEventTime, GoodRegEndTime, 1, 1);
            fail("Expected exception not thrown for past event time");
        } catch (IllegalArgumentException e) {
            //pass
        }
        //Test that exception is thrown if registration end time is in the past
        try {
            Event badEvent = new Event("Test Event", "Test Location", "Test Description", GoodEventTime, PastRegEndTime, 1, 1);
            fail("Expected exception not thrown for past registration end time");
        } catch (IllegalArgumentException e) {
            assert true;

        }
        //Test that exception is thrown if registration end time is after event time
        try {
            Event badEvent = new Event("Test Event", "Test Location", "Test Description", GoodEventTime, BadRegEndTime, 1, 1);
            fail("Expected exception not thrown for bad registration end time");
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }


}
