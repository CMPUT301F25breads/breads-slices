package com.example.slices;

import static org.junit.Assert.assertEquals;

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
}

