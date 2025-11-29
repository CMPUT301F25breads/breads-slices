package com.example.slices.modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.slices.models.EventInfo;
import com.example.slices.models.Image;
import com.google.firebase.Timestamp;

import org.junit.Test;

public class EventInfoTest {
    /**
     * Creates timestamps easily
     */
    private Timestamp ts(long millis) {
        return new Timestamp(
                millis / 1000,
                (int) ((millis % 1000) * 1_000_000)
        );
    }

    /**
     * Tests full constructor initializes all key fields properly
     * Pass if all fields are initialized
     * Fail otherwise
     */
    @Test
    public void testFullConstructor() {
        long now = System.currentTimeMillis();

        Timestamp eventDate = ts(now + 10_000);
        Timestamp regStart  = ts(now);
        Timestamp regEnd    = ts(now + 5_000);

        EventInfo info = new EventInfo("My Event", "Desc", "Loc", "Guide", "Img", eventDate,
                regStart, regEnd, 50, 10, true, "dist", 99, 123, new Image());

        assertEquals("My Event", info.getName());
        assertEquals("Desc", info.getDescription());
        //assertEquals("Loc", info.getLocation());
        assertEquals("Guide", info.getGuidelines());
        assertEquals("Img", info.getImageUrl());

        assertEquals(eventDate, info.getEventDate());
        assertEquals(regStart, info.getRegStart());
        assertEquals(regEnd, info.getRegEnd());

        assertEquals(50, info.getMaxEntrants());
        assertEquals(10, info.getMaxWaiting());
        assertEquals(0, info.getCurrentEntrants());       // constructor sets 0

        assertTrue(info.getEntrantLoc());
        assertEquals("dist", info.getEntrantDist());
        assertEquals(99, info.getId());
        assertEquals(123, info.getOrganizerID());
    }

    /**
     * Tests default constructor initializes fields to defaults
     * Pass if all fields are initialized to defaults
     * Fail otherwise
     *
     */
    @Test
    public void testDefaultConstructor() {
        EventInfo info = new EventInfo();

        assertNull(info.getName());
        assertNull(info.getDescription());
        assertNull(info.getLocation());
        assertNull(info.getEventDate());
        assertNull(info.getRegStart());
        assertNull(info.getRegEnd());

        assertEquals(Integer.MAX_VALUE, info.getMaxWaiting());
        assertEquals(0, info.getCurrentEntrants());
        assertEquals(0, info.getMaxEntrants());
        assertEquals(0, info.getId());
    }

    /**
     * Tests that currentEntrants starts at 0 in full constructor
     * Pass if currentEntrants is 0
     * Fail otherwise
     */
    @Test
    public void testCurrentEntrantsStartsAtZero() {
        EventInfo info = new EventInfo(
                "E", "D", "L",
                "G", "I",
                ts(1), ts(2), ts(3),
                10, 5,
                false, "none",
                1, 2, new Image()
        );

        assertEquals(0, info.getCurrentEntrants());
    }


    // Removed as there is no more default download url
    /**
     * Tests that imageUrl defaults correctly in default constructor
     * Pass if default is used
     * Fail otherwise
     */
//    @Test
//    public void testDefaultImageUrl() {
//        EventInfo info = new EventInfo();
//        assertEquals("https://cdn.mos.cms.futurecdn.net/39CUYMP8vJqHAYGVzUghBX.jpg", info.getImage().getUrl());
//    }

    /**
     * Tests that maxWaiting defaults to Integer.MAX_VALUE
     */
    @Test
    public void testDefaultMaxWaitingIsMaxInt() {
        EventInfo info = new EventInfo();
        assertEquals(Integer.MAX_VALUE, info.getMaxWaiting());
    }

    /**
     * Tests that entrantLoc and entrantDist are set correctly
     */
    @Test
    public void testEntrantLocAndDist() {
        EventInfo info = new EventInfo("Name", "Desc", "Loc", "Guide", "Img",
                ts(10), ts(1), ts(5), 20, 5, true, "radius", 77, 333, new Image());
        assertTrue(info.getEntrantLoc());
        assertEquals("radius", info.getEntrantDist());
    }
}

