package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testWaitlist {
    private DBConnector db = new DBConnector();



    private Entrant createEntrant(EntrantCallback callback) {
        Entrant e = new Entrant("Entrant", "Email@example.com", "Phone", callback);
        return e;
    }


    @Test
    public void testWaitlistNormal() {
        db.clearEntrants(() -> {
            try {
                List<Entrant> entrants = TestUtils.createTestEntrants(10, 10);
                Waitlist waitlist = new Waitlist(10);
                try {
                    for (Entrant e : entrants) {
                        waitlist.addEntrant(e);
                    }
                }
                catch (IllegalStateException e) {
                    fail("Failed to add entrant to waitlist, waitlist is full");
                }
            }
            catch (Exception e) {
                fail("Failed to create waitlist");
                }
            });

        }




    @Test
    public void testWaitlistFull() {
        db.clearEntrants(() -> {
            try {
                List<Entrant> entrants = TestUtils.createTestEntrants(10, 10);
                Waitlist waitlist = new Waitlist(9);
                try {
                    for (Entrant e : entrants) {
                        waitlist.addEntrant(e);
                    }
                    fail("Waitlist should be full");
                }
                catch (IllegalStateException e) {
                    assertEquals(e.getMessage(), "Waitlist is full");
                }
            }
            catch (Exception e) {
                fail("Failed to create waitlist");
            }
        });

    }


}

