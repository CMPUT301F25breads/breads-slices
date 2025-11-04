package com.example.slices;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.models.Entrant;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testsEntrant {


    @Test
    public void testEntrant() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);
        assertTrue(e.getName().equals("Foo"));
        assertTrue(e.getEmail().equals("Foo@Foo.Foo"));
        assertTrue(e.getPhoneNumber().equals("780-678-1211"));
        assertTrue(e.getId() == 1);
    }

    @Test
    public void testEntrantWithParent() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);
        Entrant p;
        try {
            p = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 2, e);
            assertTrue(e.getSubEntrants().contains(2));
            assertTrue(p.getParent() == 1);
        }
        catch (Exception r) {
            fail("Failed to create parent");
        }
    }

    @Test
    public void testEntrantWithParentFail() {
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);

        try {
            Entrant p = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 2, e);
            try {
                Entrant p2 = new Entrant("Bar", "Bar@Bar.Bar", "780-678-1212", 3, p);
                fail("Should not be able to create parent with parent");
            }
            catch (Exception r) {
                assertTrue(true);
            }
        }
        catch (Exception r) {
            fail("Failed to create parent");
        }
    }
}

