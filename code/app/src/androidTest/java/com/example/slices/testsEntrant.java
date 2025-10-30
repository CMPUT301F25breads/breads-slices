package com.example.slices;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantListCallback;
import com.example.slices.models.Entrant;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class testsEntrant {

    @Test
    public void testEntrant() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Entrant e = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);
        assertTrue(e.getName().equals("Foo"));
        assertTrue(e.getEmail().equals("Foo@Foo.Foo"));
        assertTrue(e.getPhoneNumber().equals("780-678-1211"));
        assertTrue(e.getId() > 0);
    }

    @Test
    public void testEntrantWithParent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Entrant parent = new Entrant("Foo2", "Foo@Foo.Foo", "780-678-1211", new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        Boolean completed = latch.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);
        assertTrue(parent.getName().equals("Foo2"));

        CountDownLatch latch2 = new CountDownLatch(1);
        Entrant child = new Entrant("Foo", "Foo@Foo.Foo", "780-678-121", parent, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                latch2.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch2.countDown();
                fail("Failed to create child entrant");
            }
        });
        completed = latch2.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);
        assertTrue(child.getName().equals("Foo"));

        CountDownLatch latch3 = new CountDownLatch(1);
        child.getParent(new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                assertTrue(entrant.getName().equals("Foo2"));
                latch3.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get parent");
                latch3.countDown();
            }
        });
        completed = latch3.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);

        CountDownLatch latch4 = new CountDownLatch(1);
        parent.getSubEntrants(new EntrantListCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                assertTrue(entrants.size() == 1);
                assertTrue(entrants.get(0).getName().equals("Foo"));
                latch4.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get sub entrants");
                latch4.countDown();
            }
            });
        completed = latch4.await(15000, TimeUnit.MILLISECONDS);
        assertTrue(completed);
    }
}

