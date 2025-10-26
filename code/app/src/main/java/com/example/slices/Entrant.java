package com.example.slices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

//Prototype class for the entrant
public class Entrant {
    private String name;
    private String email;
    private String phoneNumber;
    private int id;

    private List<Integer> subEntrants;



    public Entrant() {

    }
    public Entrant(String name, String email, String phoneNumber) {
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
    public List<Integer> getSubEntrants() {
        return subEntrants;
        }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entrant entrant = (Entrant) o;
        return id == entrant.id; // or whatever uniquely identifies an Entrant
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
