package com.example.slices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.models.Entrant;
import com.example.slices.testing.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testEntrantController {

    private EntrantController db;

    private void clearAll() throws InterruptedException {
        // Clear all collections before each test
        CountDownLatch latch = new CountDownLatch(1);
        EntrantController.clearEntrants(() -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }


    @Before
    public void setup() throws InterruptedException {
        db = EntrantController.getInstance();
        EntrantController.setTesting(true);

    }



    @Test
    public void testWriteAndGetEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEntrant(entrant.getId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        assertEquals(entrant.getName(), result.getName());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrant");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Tests the getEntrantByDeviceId method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testGetEntrantByDeviceId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        entrant.setDeviceId("testDeviceId");
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrantDeviceId(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.getEntrantByDeviceId(entrant.getDeviceId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        assertEquals(entrant.getDeviceId(), result.getDeviceId());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to get entrant by device ID");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant device ID");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Tests the getNewEntrantId method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testGetNewEntrantId() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int newId) {
                assertTrue(newId >= 1);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new entrant ID");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Tests the updateEntrant method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testUpdateEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                entrant.setName("Updated Name");
                db.updateEntrant(entrant, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        db.getEntrant(entrant.getId(), new EntrantCallback() {
                            @Override
                            public void onSuccess(Entrant result) {
                                assertEquals("Updated Name", result.getName());
                                latch.countDown();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                fail("Failed to get updated entrant");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to update entrant");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Tests the deleteEntrant method.
     * @throws InterruptedException
     *      Thrown if the thread is interrupted while waiting.
     */
    @Test
    public void testDeleteEntrant() throws InterruptedException {

        // Clear all collections before each test
        clearAll();
        Entrant entrant = TestUtils.createLocalTestEntrants(1).get(0);
        CountDownLatch latch = new CountDownLatch(1);

        db.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                db.deleteEntrant(String.valueOf(entrant.getId()));
                db.getEntrant(entrant.getId(), new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant result) {
                        fail("Entrant should have been deleted");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown(); // Expected
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to write entrant for delete test");
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }
}
