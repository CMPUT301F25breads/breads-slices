package com.example.slices.controllertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.EntrantController;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.Logger;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;

import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EntrantControllerTest {

    @BeforeClass
    public static void globalSetup()  {
        //Chuck it in testing mode
        EntrantController.setTesting(true);
        EventController.setTesting(true);
        Logger.setTesting(true);
        NotificationManager.setTesting(true);

        //Clean it out
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        await(latch);
    }

    @AfterClass
    public static void tearDown()  {
        //Clean it out
        CountDownLatch latch = new CountDownLatch(4);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        Logger.clearLogs(latch::countDown);
        await(latch);

        //Reset back to default
        EntrantController.setTesting(false);
        EventController.setTesting(false);
        Logger.setTesting(false);
        NotificationManager.setTesting(false);
    }

    /**
     * Await a latch to complete
     * @param latch
     *      Latch to wait for
     */
    private static void await(CountDownLatch latch) {
        try {
            boolean ok = latch.await(20, TimeUnit.SECONDS);
            assertTrue("Timed out waiting for async operation", ok);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }

    /**
     * Clear all collections (entrants, events, notifications)
     */
    private void clearAll()  {
        CountDownLatch latch = new CountDownLatch(3);
        EntrantController.clearEntrants(latch::countDown);
        EventController.clearEvents(latch::countDown);
        NotificationManager.clearNotifications(latch::countDown);
        await(latch);
    }

    /**
     * Create an entrant via fields helper
     * @param name
     *      Entrant name
     * @return
     *      Persisted entrant
     */
    private Entrant createEntrantByFields(String name) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Entrant> ref = new AtomicReference<>();
        EntrantController.createEntrant(name, name + "@mail.com", "123", new EntrantCallback() {
                    @Override
                    public void onSuccess(Entrant entrant) {
                        ref.set(entrant);
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create entrant: " + e.getMessage());
                    }
                });
        await(latch);
        return ref.get();
    }

    /**
     * Create an entrant via deviceId helper
     * @param deviceId
     *      Device ID for entrant
     * @return
     *      Persisted entrant
     */
    private Entrant createEntrantByDevice(String deviceId)  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Entrant> ref = new AtomicReference<>();
        EntrantController.createEntrant(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                ref.set(entrant);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to create entrant by deviceId: " + e.getMessage());
            }
        });
        await(latch);
        return ref.get();
    }

    /**
     * Create a valid future event for deleteEntrant tests
     * @return
     *      Persisted event
     
     */
    private Event createValidEvent()  {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> ref = new AtomicReference<>();
        //Get some valid times
        List<Timestamp> times = EventController.getTestEventTimes();
        Timestamp regStart = times.get(0);
        Timestamp regEnd = times.get(1);
        Timestamp eventDate = times.get(2);
        EventController.createEvent("Evt", "Desc", null, "Guide", "Img",
                eventDate, regStart, regEnd, 10, 5, false, "none", 123,
                new EventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        ref.set(event);
                        latch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to create event: " + e.getMessage());
                    }
                });
        await(latch);
        return ref.get();
    }

    

    /**
     * Tests the getEntrant method
     * Passes if entrant is found
     * Fail otherwise
     */
    @Test
    public void testGetEntrant() {
        clearAll();

        Entrant entrant = createEntrantByFields("Bob");

        CountDownLatch latch = new CountDownLatch(1);
        EntrantController.getEntrant(entrant.getId(), new EntrantCallback() {
            @Override
            public void onSuccess(Entrant result) {
                assertEquals(entrant.getId(), result.getId());
                assertEquals("Bob", result.getProfile().getName());
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Should find entrant");
            }
        });

        await(latch);
    }

    /**
     * Tests the getEntrant method for a non-existent entrant
     * Passes if exception is thrown
     * Fail otherwise
     */
    @Test
    public void testGetEntrantNotFound()  {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        EntrantController.getEntrant(9999, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                fail("Should not find non-existent entrant");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EntrantNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests the getEntrantByDeviceId method
     * Passes if entrant is found
     * Fail otherwise
     */
    @Test
    public void testGetEntrantByDeviceId() {
        clearAll();
        String deviceId = "device-123";
        Entrant created = createEntrantByDevice(deviceId);
        CountDownLatch latch = new CountDownLatch(1);
        EntrantController.getEntrantByDeviceId(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                assertEquals(created.getId(), entrant.getId());
                assertEquals(deviceId, entrant.getDeviceId());
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Should find entrant by deviceId");
            }
        });
        await(latch);
    }

    /**
     * Tests the getEntrantByDeviceId method for a non-existent entrant
     * Passes if exception is thrown
     * Fail otherwise

     */
    @Test
    public void testGetEntrantByDeviceIdNotFound()  {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        EntrantController.getEntrantByDeviceId("no-such-device", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                fail("Should not find entrant for random deviceId");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EntrantNotFound);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests the getNewEntrantId method for an empty collection
     * Passes if new ID is returned and is 1
     * Fail otherwise
     */
    @Test
    public void testGetNewEntrantIdEmpty()  {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                ref.set(id);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new entrant ID");
            }
        });
        await(latch);
        assertEquals(1, (int)ref.get());
    }

    /**
     * Tests the getNewEntrantId method for a non-empty collection
     * Passes if new ID is returned and is greater than the last ID
     * Fail otherwise
     */
    @Test
    public void testGetNewEntrantIdNotEmpty()  {
        clearAll();
        //Create an entrant
        Entrant e1 = createEntrantByFields("Existing");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        //Get a new ID
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                ref.set(id);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get new entrant ID");
            }
        });
        await(latch);
        assertTrue(ref.get() > e1.getId());
    }

    /**
     * Tests the writeEntrant method
     * Passes if entrant is written and can be read back
     * Fail otherwise
     */
    @Test
    public void testWriteEntrant()  {
        clearAll();
        CountDownLatch idLatch = new CountDownLatch(1);
        AtomicReference<Integer> idRef = new AtomicReference<>(0);
        //Get a new ID
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                idRef.set(id);
                idLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get id");
            }
        });
        //Wait for ID
        await(idLatch);
        //Create entrant
        Entrant entrant = new Entrant("Carl", "carl@mail.com", "555", idRef.get());
        CountDownLatch writeLatch = new CountDownLatch(1);
        //Write entrant first
        EntrantController.writeEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                writeLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("writeEntrant failed");
            }
        });
        //Wait for write
        await(writeLatch);
        CountDownLatch readLatch = new CountDownLatch(1);
        //Now read it back
        EntrantController.getEntrant(entrant.getId(), new EntrantCallback() {
            @Override
            public void onSuccess(Entrant result) {
                assertEquals("Carl", result.getProfile().getName());
                assertEquals("carl@mail.com", result.getProfile().getEmail());
                assertEquals("555", result.getProfile().getPhoneNumber());
                readLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to read entrant after write");
            }
        });
        await(readLatch);
    }


    /**
     * Tests the updateEntrant method
     * Passes if entrant is updated and can be read back
     * Fail otherwise
     */
    @Test
    public void testUpdateEntrantChangesPersist()  {
        clearAll();
        //Create entrant
        Entrant entrant = createEntrantByFields("Test");
        //Update name
        entrant.getProfile().setName("TestUpdated");
        CountDownLatch updateLatch = new CountDownLatch(1);
        //Write the update
        EntrantController.updateEntrant(entrant, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                updateLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("updateEntrant failed");
            }
        });
        await(updateLatch);
        CountDownLatch readLatch = new CountDownLatch(1);
        //Now read it back
        EntrantController.getEntrant(entrant.getId(), new EntrantCallback() {
            @Override
            public void onSuccess(Entrant result) {
                assertEquals("TestUpdated", result.getProfile().getName());
                readLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to read entrant after update");
            }
        });
        await(readLatch);
    }

    /**
     * Tests the createEntrantByDeviceId method when an entrant with the same deviceId already exists
     * Passes if exception is thrown
     * Fail otherwise
     */
    @Test
    public void testCreateEntrantByDeviceIdAlreadyExists()  {
        clearAll();
        String deviceId = "dup-device";
        //Create entrant
        createEntrantByDevice(deviceId);
        CountDownLatch latch = new CountDownLatch(1);
        //Try to create entrant with same deviceId
        EntrantController.createEntrant(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                fail("Should not create entrant with duplicate deviceId");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof DBOpFailed);
                latch.countDown();
            }
        });
        await(latch);
    }

    /**
     * Tests the clearEntrants method
     * Passes if all entrants are deleted
     * Fail otherwise
     */
    @Test
    public void testClearEntrantsEmptiesCollection()  {
        clearAll();
        createEntrantByFields("G1");
        createEntrantByFields("G2");
        CountDownLatch clearLatch = new CountDownLatch(1);
        EntrantController.clearEntrants(clearLatch::countDown);
        await(clearLatch);
        //Next ID should reset to 1 again
        CountDownLatch idLatch = new CountDownLatch(1);
        AtomicReference<Integer> idRef = new AtomicReference<>(0);
        EntrantController.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                idRef.set(id);
                idLatch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to get id after clearEntrants");
            }
        });
        await(idLatch);
        assertEquals(1, (int)idRef.get());
    }


    /**
     * Tests the deleteEntrant method when the entrant is not in any events
     * Passes if entrant is deleted
     * Fail otherwise
     */
    @Test
    public void testDeleteEntrantNoEvents() {
        clearAll();
        Entrant entrant = createEntrantByFields("Hank");
        CountDownLatch del = new CountDownLatch(1);
        try {
        EntrantController.deleteEntrant(String.valueOf(entrant.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("deleteEntrant failed for entrant with no events");
            }
        });
        } catch (Exception e) {
            fail("deleteEntrant failed for entrant with no events");
        }
        await(del);
        CountDownLatch read = new CountDownLatch(1);
        EntrantController.getEntrant(entrant.getId(), new EntrantCallback() {
            @Override
            public void onSuccess(Entrant e) {
                fail("Entrant should have been deleted");
            }
            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EntrantNotFound);
                read.countDown();
            }
        });
        await(read);
    }

    /**
     * Tests the deleteEntrant method when the entrant is in an event
     * Passes if entrant is deleted and removed from event
     * Fail otherwise

     */
    @Test
    public void testDeleteEntrantInEvent()   {
        clearAll();
        Entrant entrant = createEntrantByFields("InEvent");
        Event event = createValidEvent();
        CountDownLatch add = new CountDownLatch(1);
        EventController.addEntrantToEvent(event, entrant, new DBWriteCallback() {
            @Override public void onSuccess(){add.countDown();}
            @Override public void onFailure(Exception e){fail("Add entrant to event failed");}
        });
        await(add);
        CountDownLatch del = new CountDownLatch(1);
        try {
        EntrantController.deleteEntrant(String.valueOf(entrant.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("deleteEntrant failed for entrant in event");
            }
        });
        } catch (Exception e) {
            fail("deleteEntrant failed for entrant in event");
        }
        await(del);
        //Entrant should not be in event anymore
        CountDownLatch checkEvent = new CountDownLatch(1);
        EventController.getEntrantsForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertFalse(entrants.stream().anyMatch(en -> en.getId() == entrant.getId()));
                checkEvent.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to load event entrants after deleteEntrant");
            }
        });
        await(checkEvent);
    }

    /**
     * Tests the deleteEntrant method when the entrant is in a waitlist
     * Passes if entrant is deleted and removed from waitlist
     * Fail otherwise

     */
    @Test
    public void testDeleteEntrantInWaitlist() {
        clearAll();
        Entrant entrant = createEntrantByFields("InWait");
        Event event = createValidEvent();
        CountDownLatch addWL = new CountDownLatch(1);
        EventController.addEntrantToWaitlist(event, entrant, new DBWriteCallback() {
            @Override public void onSuccess(){addWL.countDown();}
            @Override public void onFailure(Exception e){fail("Add to waitlist failed");}
        });
        await(addWL);
        CountDownLatch del = new CountDownLatch(1);
        try {
        EntrantController.deleteEntrant(String.valueOf(entrant.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("deleteEntrant failed for entrant in waitlist");
            }
        });
        } catch (Exception e) {
            fail("deleteEntrant failed for entrant in waitlist");
        }
        await(del);
        CountDownLatch checkWL = new CountDownLatch(1);
        EventController.getWaitlistForEvent(event.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertFalse(entrants.stream().anyMatch(en -> en.getId() == entrant.getId()));
                checkWL.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                fail("Failed to load waitlist after deleteEntrant");
            }
        });
        await(checkWL);
    }

    /**
     * Tests the deleteEntrant method when the entrant is mixed membership
     * Passes if entrant is deleted and removed from event and waitlist
     * Fail otherwise
     */
    @Test
    public void testDeleteEntrantMessy()  {
        clearAll();
        Entrant entrant = createEntrantByFields("Mixed");
        Event eventMain = createValidEvent();
        Event eventWait = createValidEvent();
        CountDownLatch add = new CountDownLatch(2);
        EventController.addEntrantToEvent(eventMain, entrant, new DBWriteCallback() {
            @Override public void onSuccess(){add.countDown();}
            @Override public void onFailure(Exception e){fail("Add to event failed");}
        });
        EventController.addEntrantToWaitlist(eventWait, entrant, new DBWriteCallback() {
            @Override public void onSuccess(){add.countDown();}
            @Override public void onFailure(Exception e){fail("Add to waitlist failed");}
        });
        await(add);

        CountDownLatch del = new CountDownLatch(1);
        try {
        EntrantController.deleteEntrant(String.valueOf(entrant.getId()), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                del.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("deleteEntrant failed for mixed membership");
            }
        });
        } catch (Exception e) {
            fail("deleteEntrant failed for mixed membership");
        }
        await(del);

        CountDownLatch checkMain = new CountDownLatch(1);
        EventController.getEntrantsForEvent(eventMain.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertFalse(entrants.stream().anyMatch(en -> en.getId() == entrant.getId()));
                checkMain.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to load main event entrants");
            }
        });
        await(checkMain);

        CountDownLatch checkWait = new CountDownLatch(1);
        EventController.getWaitlistForEvent(eventWait.getId(), new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertFalse(entrants.stream().anyMatch(en -> en.getId() == entrant.getId()));
                checkWait.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to load waitlist event entrants");
            }
        });
        await(checkWait);
    }

    /**
     * Tests the deleteEntrant method when the entrant does not exist
     * Passes if exception is thrown
     * Fail otherwise
     */
    @Test
    public void testDeleteEntrantNotFound()   {
        clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        //Try to delete non-existent entrant
        try {
        EntrantController.deleteEntrant("9999", new DBWriteCallback() {
            @Override
            public void onSuccess() {
                fail("Should not succeed deleting non-existent entrant");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(e instanceof EntrantNotFound);
                latch.countDown();
            }
        });
        } catch (Exception e) {
            fail("deleteEntrant failed for non-existent entrant");
        }
        await(latch);
    }

    @Test
    public void deleteOrganizerDeletesEvent()  {
        //TODO
    }






}
