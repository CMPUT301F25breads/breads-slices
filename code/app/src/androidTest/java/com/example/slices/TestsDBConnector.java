package com.example.slices;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestsDBConnector {
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


}
