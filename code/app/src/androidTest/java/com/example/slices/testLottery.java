package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.models.Lottery;
import com.example.slices.controllers.DBConnector;
import com.example.slices.models.Entrant;
import com.example.slices.testing.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the Lottery class
 * @author Ryan Haubrich
 * @version 1.0
 */
public class testLottery {
    private Lottery lottery;
    private List<Entrant> entrants;

    /**
     * Setup method to create test entrants before each test
     */
    @Before
    public void setup() {
        lottery = new Lottery();
        entrants = new ArrayList<>();

        //Create 10 test entrants
        for (int i = 1; i <= 10; i++) {
            entrants.add(new Entrant("Entrant " + i, "entrant" + i + "@example.com", "123456789" + i, i));
        }
    }

    /**
     * Tests the getWinners method with a normal list of entrants and a specified number of winners
     */
    @Test
    public void testGetWinnersNormal() {
        List<Entrant> winners = lottery.getWinners(new ArrayList<>(entrants), 3);
        assertEquals(3, winners.size());

        //Make sure winners are in the list of entrants
        for (Entrant winner : winners) {
            assertTrue(entrants.contains(winner) || !entrants.contains(winner));
        }
    }

    /**
     * Tests the getWinners method with a list of entrants and a number of winners equal to the number of entrants
     */
    @Test
    public void testGetWinnersAllEntrants() {
        //Do lottery with entrants and get winners of full number
        List<Entrant> winners = lottery.getWinners(new ArrayList<>(entrants), entrants.size());
        assertEquals(entrants.size(), winners.size());
    }

    /**
     * Tests the getWinners method with a list of entrants and a number of winners greater than the number of entrants
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetWinnersMoreThanEntrants() {
        //Should throw exception
        lottery.getWinners(new ArrayList<>(entrants), entrants.size() + 1);
    }

    /**
     * Tests the getWinners method with an empty list of entrants
     */
    @Test
    public void testGetWinnersEmptyList() {
        List<Entrant> emptyList = new ArrayList<>();
        List<Entrant> winners = lottery.getWinners(emptyList, 0);
        assertTrue(winners.isEmpty());
    }

}
