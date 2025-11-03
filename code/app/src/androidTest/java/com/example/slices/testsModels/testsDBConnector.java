package com.example.slices.testsModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.example.slices.models.Event;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.models.Entrant;
import com.example.slices.testing.TestUtils;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testsDBConnector {
    private DBConnector dbConnector;
    private static final int TIMEOUT = 10; // seconds

    @BeforeEach
    public void setup() throws InterruptedException {
        dbConnector = new DBConnector();
        clearAllCollections();
    }

    private void clearAllCollections() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        dbConnector.clearEntrants(() -> latch.countDown());
        dbConnector.clearEvents(() -> latch.countDown());
        dbConnector.clearNotifications(() -> latch.countDown());
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    // ---------- HELPER METHODS ----------

    private void asyncWrite(Runnable writeAction) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        writeAction.run();
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    private <T> T awaitResult(AsyncSupplier<T> supplier) throws InterruptedException {
        final Object[] resultHolder = new Object[1];
        final Exception[] exceptionHolder = new Exception[1];
        CountDownLatch latch = new CountDownLatch(1);

        supplier.get(new AsyncCallback<T>() {
            @Override
            public void onSuccess(T result) {
                resultHolder[0] = result;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                exceptionHolder[0] = e;
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        if (exceptionHolder[0] != null) {
            fail("Async operation failed: " + exceptionHolder[0].getMessage());
        }

        return (T) resultHolder[0];
    }

    @FunctionalInterface
    interface AsyncCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    @FunctionalInterface
    interface AsyncSupplier<T> {
        void get(AsyncCallback<T> callback);
    }

    // ---------- ENTRANT TESTS ----------

    @Test
    public void testWriteAndGetEntrant() throws InterruptedException {
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);

        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant");
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Entrant retrieved = awaitResult(cb -> dbConnector.getEntrant(entrant.getId(), cb::onSuccess));
        assertEquals(entrant.getName(), retrieved.getName());
    }

    @Test
    public void testUpdateEntrant() throws InterruptedException {
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        dbConnector.writeEntrant(entrant, new DBWriteCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        entrant.setName("Updated Name");
        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.updateEntrant(entrant, new DBWriteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onFailure(Exception e) { fail(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Entrant updated = awaitResult(cb -> dbConnector.getEntrant(entrant.getId(), cb::onSuccess));
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    public void testDeleteEntrant() throws InterruptedException {
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        dbConnector.writeEntrant(entrant, new DBWriteCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        dbConnector.deleteEntrant(String.valueOf(entrant.getId()));

        Exception exception = assertThrows(Exception.class, () -> {
            awaitResult(cb -> dbConnector.getEntrant(entrant.getId(), cb::onSuccess));
        });
    }

    // ---------- EVENT TESTS ----------

    @Test
    public void testWriteAndGetEvent() throws InterruptedException {
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.writeEvent(event, new DBWriteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onFailure(Exception e) { fail(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Event retrieved = awaitResult(cb -> dbConnector.getEvent(event.getId(), cb::onSuccess));
        assertEquals(event.getName(), retrieved.getName());
    }

    @Test
    public void testUpdateEvent() throws InterruptedException {
        Event event = TestUtils.createTestEvents(1, 10, 10).get(0);
        dbConnector.writeEvent(event, new DBWriteCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(Exception e) { fail(); }
        });

        event.setName("Updated Event");
        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.updateEvent(event, new DBWriteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onFailure(Exception e) { fail(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Event updated = awaitResult(cb -> dbConnector.getEvent(event.getId(), cb::onSuccess));
        assertEquals("Updated Event", updated.getName());
    }

    // ---------- NOTIFICATION TESTS ----------

    @Test
    public void testWriteAndGetNotification() throws InterruptedException {
        Notification notification = new Notification(1, "Message", 1, 2);

        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.writeNotification(notification, new DBWriteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onFailure(Exception e) { fail(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        Notification retrieved = awaitResult(cb -> dbConnector.getNotificationById(notification.getId(), cb::onSuccess));
        assertEquals("Message", retrieved.getMessage());
    }

    // ---------- LOG TESTS ----------

    @Test
    public void testWriteAndGetLog() throws InterruptedException {
        Log log = new Log(1, "Test Log");

        CountDownLatch latch = new CountDownLatch(1);
        dbConnector.writeLog(log, new DBWriteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onFailure(Exception e) { fail(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);

        List<Log> logs = awaitResult(cb -> dbConnector.getAllLogs(cb::onSuccess));
        assertTrue(logs.stream().anyMatch(l -> l.getMessage().equals("Test Log")));
    }

}
    /*private DBConnector db;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = new DBConnector();
    }


    //@Ignore //Ignore this test for now only enable when needed
    @Test
    public void testEntrantCRUD() throws InterruptedException {
        //Clear the database
        db.clearEntrants(() -> {
            Log.d("TestsDBConnector", "Entrants cleared");
        });

        //Create a test entrant
        CountDownLatch latch = new CountDownLatch(1);
        try {
            List<Entrant> entrants = TestUtils.createTestEntrants(1, 10);
            Entrant entrant = entrants.get(0);
            db.getEntrant(entrant.getId(), new EntrantCallback() {
                @Override
                public void onSuccess(Entrant result) {
                    assertNotNull(result);
                    assertTrue(result.getId() == entrant.getId());
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get entrant" + e.getMessage());
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            fail("Failed to create test entrants" + e.getMessage());
            latch.countDown();


        }
        boolean success = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        if (!success) {
            fail("Test timed out");
        }

    }


    @Test
    public void testGetEntrantsForEvent() {
        db.clearEntrants(() -> {
            Log.d("TestsDBConnector", "Entrants cleared");
        });
        db.clearEvents(() -> {
            Log.d("TestsDBConnector", "Events cleared");
        });
        //Create a test event

        CountDownLatch latch = new CountDownLatch(1);
        try {
            List<Event> events = TestUtils.createTestEvents(1, 10, 10);
            Event event = events.get(0);
            //Create a test entrant
            List<Entrant> entrants = TestUtils.createTestEntrants(10, 10);
            for (Entrant entrant : entrants) {
                event.addEntrant(entrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TestsDBConnector", "Entrant added to event");

                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to add entrant to event" + e.getMessage());
                    }
                });
            }
            db.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
                @Override
                public void onSuccess(List<Entrant> result) {
                    assertNotNull(result);
                    assertTrue(result.size() == 10);
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    fail("Failed to get all entrants in event" + e.getMessage());


                }



            });

        } catch (Exception e) {
            fail("Failed to create test events" + e.getMessage());
            latch.countDown();
        }


    }
}*/
