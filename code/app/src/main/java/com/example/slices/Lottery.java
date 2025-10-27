package com.example.slices;

import java.util.ArrayList;
import java.util.List;

public class Lottery {

    private List<Entrant> entrants;
    private int numberOfWinners;

    public Lottery() {
        this.entrants = new ArrayList<Entrant>();
        this.numberOfWinners = 0;
    }

    public List<Entrant> getWinners(List<Entrant> entrants, int numberOfWinners) {
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

