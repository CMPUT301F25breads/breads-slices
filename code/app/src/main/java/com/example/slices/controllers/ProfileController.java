package com.example.slices.controllers;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.ProfileCallback;
import com.example.slices.interfaces.ProfileListCallback;
import com.example.slices.models.Profile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 *Controller for entrant profiles in firebase
 * @author Sasieni T
 *
 */
public class ProfileController {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference entrantRef = db.collection("entrants");

    /**
     * Gets all profiles from entrants in firestore
     */
    public static void getAllProfiles(ProfileListCallback callback) {
        entrantRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<Profile> profiles = new ArrayList<>();

                    snapshot.getDocuments().forEach(doc -> {
                        Profile p = new Profile();
                        p.setId(doc.getLong("id").intValue());
                        p.setName(doc.getString("name"));
                        p.setEmail(doc.getString("email"));
                        p.setPhoneNumber(doc.getString("phoneNumber"));

                        Boolean notify = doc.getBoolean("sendNotifications");
                        p.setSendNotifications(notify != null && notify);

                        // Checks for organizers through what events have organizers
                        List<?> organizedEvents = (List<?>) doc.get("organizedEvents");
                        p.setOrganizer(organizedEvents != null && !organizedEvents.isEmpty());

                        profiles.add(p);
                    });

                    callback.onSuccess(profiles);
                })
                .addOnFailureListener(callback::onFailure);
    }


    /**
     * Load one profile by entrant ID
     * Profile is organizer if organizedEvents is not null
     */
    public static void getProfileById(int id, ProfileCallback callback) {
        entrantRef.whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Profile p = query.getDocuments().get(0).toObject(Profile.class);

                        List<?> organizedEvents = (List<?>) query.getDocuments().get(0).get("organizedEvents");
                        p.setOrganizer(organizedEvents != null && !organizedEvents.isEmpty());

                        callback.onSuccess(p);
                    } else {
                        callback.onFailure(new DBOpFailed("Profile not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Gets all organizer profiles from firestore
     * @param callback
     */
    public static void getAllOrganizers(ProfileListCallback callback) {
        entrantRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<Profile> organizers = new ArrayList<>();

                    snapshot.getDocuments().forEach(doc -> {
                        Object organizedEvents = doc.get("organizedEvents");

                        if (organizedEvents != null) {
                            Profile p = new Profile();
                            p.setId(doc.getLong("id").intValue());
                            p.setName(doc.getString("name"));
                            p.setEmail(doc.getString("email"));
                            p.setPhoneNumber(doc.getString("phoneNumber"));

                            p.setOrganizer(true);

                            organizers.add(p);
                        }
                    });

                    callback.onSuccess(organizers);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * deletes a profile from firestore based on entrant ID
     * @param id
     * @param callback
     */
    public static void deleteProfile(int id, DBWriteCallback callback) {
        entrantRef.whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onFailure(new DBOpFailed("Profile not found"));
                        return;
                    }

                    query.getDocuments().get(0).getReference()
                            .delete()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(
                                    new DBOpFailed("Failed to delete profile")
                            ));
                })
                .addOnFailureListener(callback::onFailure);
    }
}