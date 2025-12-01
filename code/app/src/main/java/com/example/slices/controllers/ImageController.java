package com.example.slices.controllers;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.slices.exceptions.DBOpFailed;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.ImageListCallback;
import com.example.slices.interfaces.ImageUploadCallback;
import com.example.slices.interfaces.ImageUrlCallback;
import com.example.slices.models.Event;
import com.example.slices.models.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for facilitating the storing, updating, and retrieving
 * of images in the Firestore Storage
 *
 * @author Brad
 */
public class ImageController {

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static StorageReference imagesRef = storage.getReference().child("event_images/");

    private ImageController() {}

    public static void setTesting(boolean testing) {
        if (testing)
            imagesRef = storage.getReference().child("event_images_test/");
    }

    /**
     * Uploads an image to the firebase storage asynchronously
     * @param imageUri
     *      Uri of the image to be uploaded
     * @param userId
     *      userId of the organizer who uploaded the image (to be used to generate a unique key)
     * @param callback
     *      Callback to call once the operation is complete
     */
    public static void uploadImage(Uri imageUri, String userId, ImageUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI is null"));
            return;
        }

        String path = userId + "_" + Timestamp.now().toDate().getTime();

        StorageReference imageRef = imagesRef.child(path);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                            callback.onSuccess(new Image(path, uri.toString()))
                        )
                )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Image Controller", "Failed to upload image", null);
                        callback.onFailure(new DBOpFailed("Failed to upload image"));
                    }
                });
    }

    /**
     * Gets a list of all images from the storage
     * @param callback
     *      Callback to call once the operation is complete
     */
    public static void getAllImages(ImageListCallback callback) {
        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<StorageReference> items = listResult.getItems();

                    if (items.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                    }

                    List<Task<Uri>> tasks = new ArrayList<>();
                    for (StorageReference item : items)
                        tasks.add(item.getDownloadUrl());

                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(downUrls -> {
                                List<Image> images = new ArrayList<>();
                                for (int i = 0; i < items.size(); i++) {
                                    images.add(new Image(items.get(i).getPath(), downUrls.get(i).toString()));
                                }
                                callback.onSuccess(images);
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Image Controller", "Failed to get all images", null);
                                    callback.onFailure(new DBOpFailed("Failed to get all images"));
                                }
                            });}
                )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Image Controller", "Failed to get all images", null);
                        callback.onFailure(new DBOpFailed("Failed to get all images"));
                    }
                });
    }

    /**
     * Gets a single image's download url, likely not used with current implementation
     * @param path
     *      path of the image in storage
     * @param callback
     *      Callback to when the operation is complete
     */
    public static void getImageUrl(String path, ImageUrlCallback callback) {
        imagesRef.child(path)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    callback.onSuccess(String.valueOf(uri));
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Image Controller", "Failed to get image at: " + path, null);
                        callback.onFailure(new DBOpFailed("Failed to get an image at: " + path));
                    }
                });
    }

    /**
     * Deletes an image from the firebase storage
     * @param path
     *      path of the image in storage to delete
     * @param callback
     *      Callback to when the operation is complete
     */
    public static void deleteImage(String path, DBWriteCallback callback) {
        imagesRef.child(path)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d("Image Controller", "Successfully deleted image at: " + path, null);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.d("Image Controller", "Failed to delete at: " + path, null);
                    callback.onFailure(new DBOpFailed("Failed to delete at: " + path));
                });
    }
    public static void deleteEventImage(String eventId, DBWriteCallback callback) {

        EventController.getEvent(Integer.parseInt(eventId), new EventCallback() {
            @Override
            public void onSuccess(Event event) {

                if (event == null || event.getEventInfo() == null) {
                    callback.onFailure(new Exception("Event not found"));
                    return;
                }

                if (event.getEventInfo().getImage() == null) {
                    callback.onFailure(new Exception("Event has no image"));
                    return;
                }

                String path = event.getEventInfo().getImage().getPath();

                if (path == null || path.isEmpty()) {
                    callback.onFailure(new Exception("Image path missing"));
                    return;
                }

                // 1. Delete storage file
                deleteImage(path, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {

                        // 2. Remove image fields from Firestore
                        event.getEventInfo().setImage(null);
                        event.getEventInfo().setImageUrl(null);

                        EventController.updateEvent(event, new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(new Exception(
                                        "Storage deleted but Firestore update failed: " + e.getMessage()
                                ));
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(new Exception(
                                "Failed to delete from storage: " + e.getMessage()
                        ));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Event not found"));
            }
        });
    }

    /**
     * Modify an image in storage by deleting and uploading in order to refresh glide
     * @param path
     *      path of the image in storage to delete
     * @param imageUri
     *      Uri of the new image to upload
     * @param userId
     *      Id of the organizer who uploaded the image
     * @param callback
     *      Callback to when the operation is complete
     */
    public static void modifyImage(String path, Uri imageUri, String userId, ImageUploadCallback callback) {
        if(path != null) {
            imagesRef.child(path)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            uploadImage(imageUri, userId, new ImageUploadCallback() {
                                @Override
                                public void onSuccess(Image image) {
                                    callback.onSuccess(image);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onFailure(new DBOpFailed("Failed to get download data for new image"));

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onFailure(new DBOpFailed("Failed to delete old image"));
                        }
                    });
        }
        else {
            uploadImage(imageUri, userId, new ImageUploadCallback() {
                @Override
                public void onSuccess(Image image) {
                    callback.onSuccess(image);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(new DBOpFailed("Failed to get download data for new image"));

                }
            });
        }
    }

    public static void clearImages(Runnable onComplete) {
        imagesRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> items = listResult.getItems();

                        if (items.isEmpty())
                            onComplete.run();

                        List<Task<Void>> deletionTasks = new ArrayList<>();
                        for(StorageReference item : items) {
                            deletionTasks.add(item.delete());
                        }

                        Tasks.whenAll(deletionTasks)
                                .addOnSuccessListener(unused -> {
                                    Log.d("Image Controller", "Successfully cleared all images", null);
                                    onComplete.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("Image Controller", "Failed to clear all images", e);
                                    onComplete.run();
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Image Controller", "Failed to list all images for deletion", e);
                        onComplete.run();
                    }
                });
    }
}
