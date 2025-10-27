package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class testLottery {
    private DBConnector db = new DBConnector();




    @Test
    public void testLottery() {
        db.clearEntrants(() -> {
            try {
                List<Entrant> entrants = TestUtils.createTestEntrants(10, 10);
                Lottery lottery = new Lottery();
                List<Entrant> winners = lottery.getWinners(entrants, 5);
                assertNotNull(winners);
                assertEquals(winners.size(), 5);
                for (Entrant winner : winners) {
                    assertTrue(entrants.contains(winner));
                }
            }
            catch (Exception e) {
                fail("Failed to create lottery");
            }
        });
    }

}
