package com.example.slices.testsModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.models.Lottery;
import com.example.slices.controllers.DBConnector;
import com.example.slices.models.Entrant;
import com.example.slices.testing.TestUtils;

import org.junit.Test;

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
