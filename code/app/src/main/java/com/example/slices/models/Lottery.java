package com.example.slices.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a lottery
 * @author Ryan Haubrich
 * @version 0.1
 *
 */
public class Lottery {



    /**
     * Constructor for the Lottery class - takes no arguments
     */

    public Lottery() {

    }


    /**
     * Function that gets the winners from the list of entrants
     * @param entrants
     *      List of entrants to select winners from
     * @param numberOfWinners
     *      Number of winners to select
     * @return
     *      List of winners
     */
    public List<Entrant> getWinners(List<Entrant> entrants, int numberOfWinners) {
        if (numberOfWinners > entrants.size()) {
            throw new IllegalArgumentException("Number of winners cannot be greater than the number of entrants");
        }
        //Select winners from list of entrants
        List<Entrant> winners = new ArrayList<Entrant>();
        for (int i = 0; i < numberOfWinners; i++) {
            int randomIndex = (int) (Math.random() * entrants.size());
            winners.add(entrants.get(randomIndex));
            entrants.remove(randomIndex);
        }

        return winners;

    }
}

