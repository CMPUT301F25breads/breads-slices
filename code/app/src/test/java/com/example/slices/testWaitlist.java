package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class testWaitlist {

    private List<Entrant> createEntrants(int count) {
        List<Entrant> entrants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entrant e = new Entrant("Entrant " + i, "Email" + i + "@example.com", "Phone" + i);
            e.setId(i);
            entrants.add(e);
        }
        return entrants;
    }


    @Test
    public void testWaitlistNormal() {
        List<Entrant> entrants = createEntrants(10);
        Waitlist waitlist = new Waitlist();
        for (Entrant e : entrants) {
            waitlist.addEntrant(e);
        }
        for (Entrant e : entrants) {
            Entrant result = waitlist.getEntrant(e.getId());
            assertEquals(e.getId(), result.getId());
        }
    }


    @Test
    public void testWaitlistFull() {
        List<Entrant> entrants = createEntrants(9);
        Waitlist waitlist = new Waitlist(9);
        for (Entrant e : entrants) {
            waitlist.addEntrant(e);

        }
        for (Entrant e : entrants) {
            Entrant result = waitlist.getEntrant(e.getId());
            assertEquals(e.getId(), result.getId());
        }
        try {
            Entrant e = new Entrant("Entrant 10", "Email10@example.com", "Phone10");
            e.setId(10);
            waitlist.addEntrant(e);
            fail("Expected IllegalStateException not thrown");

        } catch (IllegalStateException ex) {
            // Expected exception
            assertEquals("Waitlist is full", ex.getMessage());

        }

    }


}

