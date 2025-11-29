package com.example.slices.controllertest;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.net.Uri;
import com.example.slices.R;
import com.example.slices.controllers.ImageController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.ImageListCallback;
import com.example.slices.interfaces.ImageUploadCallback;
import com.example.slices.models.Image;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * Tests for the Image Controller
 *
 */
public class ImageControllerTest {

    @BeforeClass
    public static void globalSetup() throws InterruptedException {
        // Chuck it in testing mode
        ImageController.setTesting(true);


        //Clean it out
        CountDownLatch latch = new CountDownLatch(1);
        ImageController.clearImages(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", completed);
    }


    @AfterClass
    public static void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ImageController.clearImages(latch::countDown);
        boolean completed = latch.await(20, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for async operation", completed);
        ImageController.setTesting(false);
    }

    /**
     * Await a latch to complete
     * @param latch
     *      Latch to wait for
     */
    private void await(CountDownLatch latch) {
        try {
            boolean ok = latch.await(20, TimeUnit.SECONDS);
            assertTrue("Timed out waiting for async operation", ok);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
    }

    /**
     * Clear storage
     */
    private void clearAll()  {
        CountDownLatch latch = new CountDownLatch(1);
        ImageController.clearImages(latch::countDown);
        await(latch);
    }

    private Uri getTestImageUri() {
        return Uri.parse("android.resource://com.example.slices/" + R.raw.test);
    }

    /**
     * Tests uploading an image and asserts that the object is not null
     * and the attribute are not null either
     */
    @Test
    public void testUploadImage() {
        clearAll();

        Uri img = getTestImageUri();
        CountDownLatch latch = new CountDownLatch(1);

        final Image[] uploaded = new Image[1];

        ImageController.uploadImage(img, "123", new ImageUploadCallback() {
            @Override
            public void onSuccess(Image image) {
                uploaded[0] = image;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Upload failed");
            }
        });

        await(latch);

        assertNotNull("Image upload returned null result", uploaded[0]);
        assertNotNull(uploaded[0].getUrl());
        assertNotNull(uploaded[0].getPath());
    }

    /**
     * Uploads an image and then deletes the image
     * obtains all images to verify there are none
     */
    @Test
    public void testDeleteImage() {
        clearAll();

        Uri img = getTestImageUri();
        CountDownLatch latch1 = new CountDownLatch(1);

        final Image[] uploaded = new Image[1];

        ImageController.uploadImage(img, "123", new ImageUploadCallback() {
            @Override
            public void onSuccess(Image image) {
                uploaded[0] = image;
                latch1.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Upload failed");
            }
        });

        await(latch1);

        CountDownLatch latch2 = new CountDownLatch(1);

        ImageController.deleteImage(uploaded[0].getPath(), new DBWriteCallback() {
            @Override
            public void onSuccess() {
                latch2.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch2.countDown();
            }
        });

        await(latch2);

        // Ensure empty
        CountDownLatch latch3 = new CountDownLatch(1);
        final List<Image>[] result = new List[1];

        ImageController.getAllImages(new ImageListCallback() {
            @Override
            public void onSuccess(List<Image> imageList) {
                result[0] = imageList;
                latch3.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get all images");
                latch3.countDown();

            }
        });

        await(latch3);

        assertTrue(result[0].isEmpty());
    }

    /**
     * Creates an image and then modifies and asserts
     * that the path and link are different
     */
    @Test
    public void testModifyImage() {
        clearAll();
        Uri img1 = getTestImageUri();
        Uri img2 = getTestImageUri();

        CountDownLatch latch1 = new CountDownLatch(1);
        final Image[] first = new Image[1];

        ImageController.uploadImage(img1, "123", new ImageUploadCallback() {
            @Override
            public void onSuccess(Image image) {
                first[0] = image;
                latch1.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Initial upload failed");
            }
        });

        await(latch1);

        CountDownLatch latch2 = new CountDownLatch(1);
        final Image[] modified = new Image[1];

        ImageController.modifyImage(first[0].getPath(), img2, "123", new ImageUploadCallback() {
            @Override
            public void onSuccess(Image image) {
                modified[0] = image;
                latch2.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("modifyImage failed");
            }
        });

        await(latch2);

        assertNotEquals(first[0].getPath(), modified[0].getPath());
        assertNotEquals(first[0].getUrl(), modified[0].getUrl());
    }

    /**
     * Adds an image, clears the images, and obtains all
     * images to ensure that it is empty
     */
    @Test
    public void testClearImages() {
        clearAll();

        Uri img = getTestImageUri();
        CountDownLatch latch1 = new CountDownLatch(1);

        ImageController.uploadImage(img, "123", new ImageUploadCallback() {
            @Override
            public void onSuccess(Image image) {
                latch1.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Upload failed");
            }
        });

        await(latch1);

        CountDownLatch latch2 = new CountDownLatch(1);
        ImageController.clearImages(latch2::countDown);
        await(latch2);

        CountDownLatch latch3 = new CountDownLatch(1);
        final List<Image>[] result = new List[1];

        ImageController.getAllImages(new ImageListCallback() {
            @Override
            public void onSuccess(List<Image> imageList) {
                result[0] = imageList;
                latch3.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to get all images");
                latch3.countDown();
            }
        });

        await(latch3);

        assertTrue(result[0].isEmpty());
    }

}
