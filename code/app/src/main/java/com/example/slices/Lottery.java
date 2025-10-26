package com.example.slices;

import java.util.List;

public class Lottery {

    private List<Entrant> entrants;
    private int numberOfWinners;

    public Lottery(List<Entrant> entrants, int numberOfWinners) {
        this.entrants = entrants;
        this.numberOfWinners = numberOfWinners;

    }

    public List<Entrant> getWinners() {
        //Select winners from list of entrants
        List<Entrant> winners = null;

        for (int i = 0; i < numberOfWinners; i++) {
            int randomIndex = (int) (Math.random() * entrants.size());
            winners.add(entrants.get(randomIndex));
            entrants.remove(randomIndex);
        }

        return winners;

    }
}

