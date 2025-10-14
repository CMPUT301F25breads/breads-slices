package com.example.slices;

import java.util.Collection;

//Prototype class for the entrant
public class Entrant {
    private String name;
    private String email;
    private String phoneNumber;
    private int id;

    private Collection<Integer> subEntrants;

    public Entrant() {

    }
    public Entrant(String name, String email, String phoneNumber, int id) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.id = id;
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
}
