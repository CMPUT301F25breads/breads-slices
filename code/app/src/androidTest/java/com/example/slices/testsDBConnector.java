package com.example.slices;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.services.events.TimeStamp;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testsDBConnector {
    private DBConnector db;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = new DBConnector();
    }

   private void createTestEntrants(int x) {
        if (x > 0) {
            db.getNewEntrantId(new EntrantIDCallback() {
                @Override
                public void onSuccess(int id) {
                    Entrant entrant = new Entrant("Foo" + x, "Foo@Foo.Foo" + x, "780-678-1211" + x);
                    entrant.setId(id);
                    db.writeEntrant(entrant, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            createTestEntrants(x - 1);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                }

                @Override
                public void onFailure(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        else {
            return;
        }
   }

    @Ignore //Ignore this test for now only enable when needed
    @Test
    public void testEntrantCRUD() throws InterruptedException {
        //Clear the database
        db.clearEntrants(() -> {
            Log.d("TestsDBConnector", "Entrants cleared");
        });

        //Create a test entrant
        //First get the next available ID
        CountDownLatch latch = new CountDownLatch(1);
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                //Create a test entrant
                Entrant entrant = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211");
                entrant.setId(id);

                //Write the entrant to the database
                db.writeEntrant(entrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        //Read the entrant from the database
                        db.getEntrant(id, new EntrantCallback() {
                            @Override
                            public void onSuccess(Entrant retrieved) {
                                assertNotNull(retrieved);
                                assert retrieved.getName().equals("Foo");

                                //Delete the entrant from the database
                                db.deleteEntrant(String.valueOf(id));
                                latch.countDown();
                            }
                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get entrant" + e.getMessage());
                                latch.countDown();
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write entrant" + e.getMessage());
                        latch.countDown();
                    }
                    });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get next entrant ID" +e.getMessage());
                latch.countDown();
            }
        });
        //Wait for the latch to count down
        boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);
        


    }
    @Ignore //Ignore this test for now only enable when needed
    @Test
    public void testEventCRUD() {
        //Clear the database
        db.clearEvents(() -> {
            Log.d("TestsDBConnector", "Events cleared");
        });

        //Create a test event
        //First get the next available ID
        CountDownLatch latch = new CountDownLatch(1);
        db.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                //Create a test event
                //Set up date and time objects
                Calendar cal = Calendar.getInstance();
                cal.set(2023, 1, 1, 12, 0, 0);
                Date date = cal.getTime();
                Timestamp eventDate = new Timestamp(date);

                //Set up deadline
                Calendar cal2 = Calendar.getInstance();
                cal2.set(2023, 1, 1, 13, 0, 0);
                Date date2 = cal2.getTime();
                Timestamp regDeadline = new Timestamp(date2);







                Event event = new Event("Foo", "Foo", "Foo", eventDate, regDeadline, 10, id);

                //Attempt to write the event to the database
                db.writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        //If successful, read the event from the database
                        db.getEvent(id, new EventCallback() {
                            @Override
                            public void onSuccess(Event retrieved) {
                                assertNotNull(retrieved);
                                assert retrieved.getName().equals("Foo");
                                assert retrieved.getDescription().equals("Foo");
                                assert retrieved.getLocation().equals("Foo");
                                assert retrieved.getEventDate().equals(eventDate);
                                assert retrieved.getRegDeadline().equals(regDeadline);
                                assert retrieved.getMaxEntrants() == 10;
                                assert retrieved.getCurrentEntrants() == 0;
                                assert retrieved.getId() == id;


                                //Delete the event from the database
                                db.deleteEvent(String.valueOf(id));
                                latch.countDown();

                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get event" + e.getMessage());
                                latch.countDown();
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write event" + e.getMessage());
                        latch.countDown();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get next event ID" +e.getMessage());
                latch.countDown();
            }
        });
    }



    @Test
    public void testGetAllEntrantsInEvent() {
        db.clearEntrants(() -> {
            Log.d("TestsDBConnector", "Entrants cleared");
        });
        db.clearEvents(() -> {
            Log.d("TestsDBConnector", "Events cleared");
        });
        //Create a test event
        //First get the next available ID
        CountDownLatch latch = new CountDownLatch(1);
        db.getNewEventId(new EventIDCallback() {
            @Override
            public void onSuccess(int id) {
                //Create a test event
                Calendar cal = Calendar.getInstance();
                cal.set(2025, 1, 1, 12, 0, 0);
                Date date = cal.getTime();
                Timestamp eventDate = new Timestamp(date);
                cal.set(2025, 1, 1, 13, 0, 0);
                Date date2 = cal.getTime();
                Timestamp regDeadline = new Timestamp(date2);
                Event event = new Event("Foo", "Foo", "Foo", eventDate, regDeadline, 10, id);
                //Attempt to write the event to the database
                db.writeEvent(event, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        createTestEntrants(10);
                        db.getEntrantsForEvent(id, new EntrantListCallback() {
                            @Override
                            public void onSuccess(List<Entrant> entrants) {
                                assertNotNull(entrants);
                                assert entrants.size() == 10;
                                assert entrants.get(0).getName().equals("Foo1");
                                assert entrants.get(9).getName().equals("Foo10");
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get entrants" + e.getMessage());
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to write event" + e.getMessage());
                        latch.countDown();
                    }


                });

            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get next event ID" + e.getMessage());
                latch.countDown();
            }
        });
    }
}
