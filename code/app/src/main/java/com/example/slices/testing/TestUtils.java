package com.example.slices.testing;

import com.example.slices.Event;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUtils {




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
            String id = "1" + i;

            new Entrant(name, email, phone, id, new EntrantCallback() {
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



    public static List<Event> createTestEvents(int count, int timeoutSec, int maxEntrants) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(count);
        List<Event> events = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < count; i++) {
            String name = "Event" + i;
            String description = "Description" + i;
            String location = "Location" + i;
            Calendar cal = Calendar.getInstance();
            cal.set(2025, 12, 12, 12, 0, 0);
            Date date = cal.getTime();
            Timestamp eventDate = new Timestamp(date);
            cal.set(2025, 12, 12, 13, 0, 0);
            Date date2 = cal.getTime();
            Timestamp regDeadline = new Timestamp(date2);

            new Event(name, description, location, eventDate, regDeadline, maxEntrants, new EventCallback() {
                @Override
                public void onSuccess(Event event) {
                    events.add(event);
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    DebugLogger.d("TestUtils", "Failed to create event: " + e.getMessage());
                    latch.countDown();

                }

            });

        }
        boolean completed = latch.await(timeoutSec, TimeUnit.SECONDS);
        if (!completed) {
            throw new AssertionError("Timed out waiting for events to be created");
        }
        return events;

    }

    public static void createTestNotifications(int count, int timeoutSec) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(count);
        List<Entrant> entrants = createTestEntrants(count + 1, timeoutSec);

        for (int i = 0; i < count; i++) {
            //Send a notification to each entrant from the last entrant
            NotificationManager.sendNotification((entrants.get(i)).getName(), "Test", entrants.get(i).getId(), entrants.get(count + 1).getId(), new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    DebugLogger.d("TestUtils", "Failed to create notification: " + e.getMessage());
                    latch.countDown();
                }
            });
        }
    }










}
