package com.example.slices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUtils {
    private static DebugLogger d = new DebugLogger();


    /**
     * Creates multiple Entrants asynchronously and waits until all are written to Firestore.
     *
     * @param count      Number of entrants to create.
     * @param timeoutSec How long to wait before timing out (to avoid hanging tests).
     * @return List of created Entrant objects (only if all were successfully created).
     * @throws InterruptedException if waiting was interrupted.
     * @throws AssertionError if not all entrants were created in time.
     */
    public static List<Entrant> createTestEntrants(int count, int timeoutSec) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(count);
        List<Entrant> entrants = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < count; i++) {
            String name = "Entrant" + i;
            String email = "entrant" + i + "@test.com";
            String phone = "780-000-000" + i;

            new Entrant(name, email, phone, new EntrantCallback() {
                @Override
                public void onSuccess(Entrant entrant) {
                    entrants.add(entrant);
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    DebugLogger.d("TestUtils", "Failed to create entrant: " + e.getMessage());
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(timeoutSec, TimeUnit.SECONDS);
        if (!completed) {
            throw new AssertionError("Timed out waiting for entrants to be created");
        }

        return entrants;
    }
}
