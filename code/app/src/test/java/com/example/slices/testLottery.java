package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class testLottery {
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
    public void testLottery() {
        List<Entrant> entrants = createEntrants(10);
        Lottery lottery = new Lottery();
        List<Entrant> winners = lottery.getWinners(entrants, 1);
        assertNotNull(winners);
        assertEquals(1, winners.size());
        Entrant winner = winners.get(0);
        assertNotNull(winner);
        assertTrue(winner.getId() >= 0 && winner.getId() < entrants.size());
    }


}
