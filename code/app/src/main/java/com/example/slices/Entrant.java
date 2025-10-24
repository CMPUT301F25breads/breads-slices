package com.example.slices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Prototype class for the entrant
public class Entrant {
    private String name;
    private String email;
    private String phoneNumber;
    private int id;

    private List<Integer> subEntrants;

    private DBConnector db = new DBConnector();

    public Entrant() {

    }
    public Entrant(String name, String email, String phoneNumber) {
        //Connect to database
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                //Set the ID
                Entrant.this.id = id;
            }

            @Override
            public void onFailure(Exception e) {
                //Handle failure
                //Will eventually throw an exception
            }

        });
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;

        //Write to database



    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getId() {
        return id;
    }
    public void setName(String name) {
        this.name = name;

    }
    public void setEmail(String email) {
        this.email = email;

    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;

    }
    public void setId(int id) {
        this.id = id;

    }
    public void addSubEntrant(int id) {
        subEntrants.add(id);

    }
    public void removeSubEntrant(int id) {
        subEntrants.remove(id);

    }
    public Collection<Integer> getSubEntrants() {
        return subEntrants;
        }
    public void delete() {
        db.deleteEntrant(String.valueOf(id));
        for (int subEntrant : subEntrants) {
            db.deleteEntrant(String.valueOf(subEntrant));
        }
    }
}
