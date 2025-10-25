package com.example.slices;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.FirebaseApp;
import com.google.type.DateTime;

import org.junit.Before;
import org.junit.Test;

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
                DateTime eventDate = DateTime.newBuilder()
                        .setYear(2023)
                        .setMonth(1)
                        .setDay(1)
                        .setHours(12)
                        .setMinutes(0)
                        .setSeconds(0)
                        .build();
                DateTime regDeadline = DateTime.newBuilder()
                        .setYear(2023)
                        .setMonth(1)
                        .setDay(1)
                        .setHours(12)
                        .setMinutes(0)
                        .setSeconds(0)
                        .build();

                Event event = new Event("Foo", "Foo", "Foo", eventDate, regDeadline, 10, id);
                

    }


}
