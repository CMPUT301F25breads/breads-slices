package com.example.slices;

import androidx.annotation.Nullable;

import org.junit.Test;

public class TestsDBConnector {
    private DBConnector mockCon() {
        DBConnector db =  new DBConnector();
        return db;
    }

    private Entrant mockEntrant() {
        Entrant mock = new Entrant("Foo", "Foo@Foo.Foo", "780-678-1211", 1);
        return mock;
    }

    public

    @Test
    void testAddEntrant() {
        DBConnector db = mockCon();
        Entrant entrant = mockEntrant();
        db.writeEntrant(entrant);
        db.getEntrant(1, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                assert entrant.getName().equals("Foo");
                assert entrant.getEmail().equals("Foo@Foo.Foo");
                assert entrant.getPhoneNumber().equals("780-678-1211");
                assert entrant.getId() == 1;
            }

            @Override
            public void onFailure(Exception e) {
                assert false;
            }
        });


    }
}
