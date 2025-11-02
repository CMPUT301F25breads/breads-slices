package com.example.slices.models;

import com.example.slices.Event;
import com.example.slices.controllers.DBConnector;
import com.example.slices.testing.DebugLogger;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EntrantIDCallback;
import com.example.slices.interfaces.EntrantListCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Prototype class for the entrant
public class Entrant {
    private String name;
    private String email;
    private String phoneNumber;
    private int id;
    private String deviceId;
    private List<Event> confirmedEvents;
    private List<Event> waitlistedEvents;

    private int parent = 0;


    private DBConnector db = new DBConnector();

    private List<Integer> subEntrants;

    public Entrant() {}

    public Entrant(String deviceId, EntrantCallback callback) {
        this.deviceId = deviceId;

        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                Entrant.this.id = id;
                db.writeEntrant(Entrant.this, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        DebugLogger.d("Entrant", "Entrant created successfully");
                        callback.onSuccess(Entrant.this);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        DebugLogger.d("Entrant", "Entrant creation failed");
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Entrant", "Entrant creation failed");
            }
        });

    }

    /**
     * Constructor for the Entrant class for creating a primary entrant
     * @param name
     *      Name of the entrant
     * @param email
     *      Email of the entrant
     * @param phoneNumber
     *      Phone number of the entrant
     */
    public Entrant(String name, String email, String phoneNumber)
    {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.subEntrants = new ArrayList<Integer>();

    }
    public Entrant(String name, String email, String phoneNumber, EntrantCallback callback) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.subEntrants = new ArrayList<Integer>();
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                Entrant.this.id = id;
                db.writeEntrant(Entrant.this, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        DebugLogger.d("Entrant", "Entrant created successfully");
                        callback.onSuccess(Entrant.this);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        DebugLogger.d("Entrant", "Entrant creation failed");
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("Entrant", "Entrant creation failed");
            }
        });
    }


    /**
     * Constructor for the Entrant class for creating a secondary entrant
     * @param name
     *      Name of the entrant
     * @param email
     *      Email of the entrant
     * @param phoneNumber
     *      Phone number of the entrant
     * @param parent
     *      Parent of the entrant
     */
    public Entrant(String name, String email, String phoneNumber, Entrant parent, EntrantCallback callback) {
        if (parent.parent != 0 ) {
            throw new IllegalArgumentException("Cant have parent with parent");
        }

        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.subEntrants = null;
        db.getNewEntrantId(new EntrantIDCallback() {
            @Override
            public void onSuccess(int id) {
                Entrant.this.id = id;
                db.writeEntrant(Entrant.this, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        DebugLogger.d("Entrant", "Entrant created successfully");
                        parent.addSubEntrant(Entrant.this);
                        db.updateEntrant(parent, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                DebugLogger.d("Entrant", "Parent updated successfully");
                                Entrant.this.parent = parent.getId();
                                callback.onSuccess(Entrant.this);

                            }

                            @Override
                            public void onFailure(Exception e) {
                                DebugLogger.d("Entrant", "Parent update failed");
                            }
                        });

                    }

                    @Override
                    public void onFailure(Exception e) {
                        DebugLogger.d("Entrant", "Entrant write failed");
                    }
                });
            }
            public void onFailure(Exception e) {
                DebugLogger.d("Entrant", "Entrant creation failed");
            }
        });

    }

    public String getDeviceId() {
        return deviceId;
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


    public void addSubEntrant(Entrant child) {
        subEntrants.add(child.getId());
    }



    public void getSubEntrants(EntrantListCallback callback) {
        List<Entrant> retSubEntrants = new ArrayList<>();
        for (int i = 0; i < this.subEntrants.size(); i++) {
            int idToGet = this.subEntrants.get(i);
            db.getEntrant(idToGet, new EntrantCallback() {
                @Override
                public void onSuccess(Entrant entrant) {
                    retSubEntrants.add(entrant);

                    if (retSubEntrants.size() == Entrant.this.subEntrants.size()) {
                        callback.onSuccess(retSubEntrants);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }

    }

    public void getParent(EntrantCallback callback) {
        //Get the parent of the entrant
        db.getEntrant(this.parent, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                callback.onSuccess(entrant);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });

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
