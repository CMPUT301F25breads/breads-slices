package com.example.slices.controllers;

import com.example.slices.models.Event;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.Entrant;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Service class for handling notification operations
 * Supports sending notifications to waitlists, chosen participants (from the lottery), and selected entrants
 */

public class NotificationService {
    /**
     * Send notification to all entrants on the waitlist for an event
     *
     * @param event - Event containing the waitlist
     * @param title - Notification title
     * @param body - Notification body
     * @param senderId - ID of the sender
     * @param callback - Callback for completion status
     */
    public static void sendToWaitlist(Event event, String title, String body, int senderId, DBWriteCallback callback){

        if (event == null || event.getWaitlist() == null) {
            callback.onFailure(new IllegalArgumentException("Event or waitlist is null"));
            return;
        }

        List<Entrant> waitlistEntrants = event.getWaitlist().getEntrants();
        if (waitlistEntrants == null || waitlistEntrants.isEmpty()) {
            callback.onSuccess(); // No entrants to notify, but operation is successful
            return;
        }

        sendToEntrantList(waitlistEntrants, title, body, senderId, event.getId(), callback);
    }

    // Send notification to all chosen entrants in an event
    public static void sendToEventEntrants(Event event, String title, String body, int senderId, DBWriteCallback callback) {
        if (event == null || event.getEntrants() == null) {
            callback.onFailure(new IllegalArgumentException("Event or entrants list is null"));
            return;
        }

        List<Entrant> eventEntrants = event.getEntrants();
        if (eventEntrants.isEmpty()) {
            callback.onSuccess(); // No entrants to notify, but operation is successful
            return;
        }

        sendToEntrantList(eventEntrants, title, body, senderId, event.getId(), callback);
    }

    // Send notification to selected entrants
    public static void sendToSelectedEntrants(List<Entrant> selectedEntrants, String title, String body, int senderId, DBWriteCallback callback) {
        if (selectedEntrants == null || selectedEntrants.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("No entrants selected"));
            return;
        }

        sendToEntrantList(selectedEntrants, title, body, senderId, 0, callback);
    }

    /**
     * Internal method to send notifications to a list of entrants
     * Consolidated method that handles both event-related and non-event notifications
     * 
     * @param entrants List of entrants to notify
     * @param title Notification title
     * @param body Notification body
     * @param senderId ID of the sender
     * @param eventId ID of the associated event (0 if not event-related)
     * @param callback Callback for completion status
     */
    private static void sendToEntrantList(List<Entrant> entrants, String title, String body, int senderId, int eventId, DBWriteCallback callback) {
        final int totalEntrants = entrants.size();
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);
        final AtomicInteger completedCount = new AtomicInteger(0);

        for (Entrant entrant : entrants) {
            NotificationManager.sendNotification(title, body, entrant.getId(), senderId, eventId, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    successCount.incrementAndGet();
                    checkCompletion();
                }

                @Override
                public void onFailure(Exception e) {
                    failureCount.incrementAndGet();
                    checkCompletion();
                }

                private void checkCompletion() {
                    int completed = completedCount.incrementAndGet();
                    if (completed == totalEntrants) {
                        // All notifications processed
                        if (failureCount.get() == 0) {
                            // All succeeded
                            callback.onSuccess();
                        } else {
                            // Some failed - report as failure with details
                            String errorMessage = String.format("Notifications partially failed: %d succeeded, %d failed", successCount.get(), failureCount.get());
                            callback.onFailure(new RuntimeException(errorMessage));
                        }
                    }
                }
            });
        }
    }
}