package com.example.slices.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.slices.R;
import com.example.slices.controllers.QRCodeManager;

import java.nio.ByteBuffer;

/**
 * CameraFragment
 *   Displays the camera preview for QR scanning using CameraX library and ZXing (Zebra Crossing)
 *
 *   When a valid QR code containing an Event ID is detected, the event ID is passed back to the
 *   BrowseFragment using FragmentResult
 */
public class CameraFragment extends Fragment{
    private PreviewView previewView;

    /**
     * onCreateView
     *   Inflate the camera preview layout for scanning
     * @return
     *   return the inflated fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    /**
     * ActivityResultLauncher
     *   opens the permission dialog for using the device's camera to request permission
     *
     *   If granted, then camera is opened
     *   If denied, fragment navigates back
     * @Param String
     */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (previewView != null) {
                        openCamera(previewView);
                    }
                } else {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });

    /**
     * Called after the fragment's view has been created and attempts to open the camera,
     * requesting permission first
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.camera_preview);

        if (hasCameraPermission()) {
            openCamera(previewView);
        } else {
            requestCameraPermission();
        }
    }

    /**
     * hasCameraPermission()
     *   Checks for permission to allow launching the device's Camera app
     * @return
     *   true if camera has permission, false otherwise
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * requestCameraPermission
     *   requests permission to use the Camera app when hasCameraPermission fails. The result
     *   is handled by cameraPermissionLauncher
     */
    private void requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    /**
     * opens the camera app for QR scanning using CameraX library, binding the preview and image
     * analysis use cases and begins scanning for QR codes
     * @param previewView
     *    The view used to display the camera preview
     */
    private void openCamera(PreviewView previewView) {
        ProcessCameraProvider.getInstance(requireContext())
                .addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider =
                                ProcessCameraProvider.getInstance(requireContext()).get();

                        Preview preview = new Preview.Builder().build();
                        preview.setSurfaceProvider(previewView.getSurfaceProvider());

                        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

                        cameraProvider.unbindAll();
                        ImageAnalysis analysis = new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                        analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()),
                                this::scanImage);

                        cameraProvider.bindToLifecycle(this, selector, preview,
                                analysis);

                    } catch (Exception x) {
                        Log.e("CameraFragment", "CameraX error", x);
                    }
                }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * process each frame from the camera using ZXing then convert frame into a source to decode the
     * QR code. Calls onCodeScanned(String) if it succeeds
     * @param image
     *    the ImageProxy frame as provided by CameraX
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanImage(ImageProxy image) {
        try (image) {
            @ExperimentalGetImage
            android.media.Image mediaImage = image.getImage();
            if (mediaImage != null) {
                int width = mediaImage.getWidth();
                int height = mediaImage.getHeight();

                ByteBuffer buffer = mediaImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                com.google.zxing.LuminanceSource source =
                        new com.google.zxing.RGBLuminanceSource(width, height, bytesToInts(bytes));

                com.google.zxing.BinaryBitmap bitmap =
                        new com.google.zxing.BinaryBitmap(new
                                com.google.zxing.common.HybridBinarizer(source));

                try {
                    com.google.zxing.Result result =
                            new com.google.zxing.qrcode.QRCodeReader().decode(bitmap);

                    onCodeScanned(result.getText());

                } catch (Exception e) {
                    // Not a QR or unreadable = ignored!
                }
            }
        }
    }

    /**
     * converts y-plane image bbytes into integers for ZXing
     * @param yuv
     *    the y-plane bytes
     * @return
     *   an int array representing grayscale pixels
     */
    private int[] bytesToInts(byte[] yuv) {
        int[] out = new int[yuv.length];
        for (int i = 0; i < yuv.length; i++) {
            int y = yuv[i] & 0xFF;
            out[i] = 0xFF000000 | (y << 16) | (y << 8) | y;
        }
        return out;
    }

    /**
     * handles a succesfully scanned QR code's text
     *
     * uses QRCodeManager.java controller to extract the event ID
     * @param text
     *    the raw text received from extracting from the QR code
     */
    private void onCodeScanned(String text) {
        // prevent multiple triggers if the fragment isn't attached any more
        if (!isAdded()) return;

        // use the shared QRCodeManager to interpret the QR codes information
        // the expected format (from the QRCodeManager) is "EVENT:<id>"
        int eventId = QRCodeManager.decodeQRCode(text);

        if (eventId == -1) {
            // Invalid or unsupported QR content get ignored
            Log.w("CameraFragment", "Scanned QR does not contain valid event id: " + text);
            return;
        }

        // send eventID back to BrowseFragment
        Bundle result = new Bundle();
        result.putInt("scanned_event_id", eventId);

        getParentFragmentManager().setFragmentResult(
                "qr_scan_result",
                result
        );


    // returns to previous screen
    //requireActivity().getOnBackPressedDispatcher().onBackPressed();
        goToBrowse();
    }
    private void goToBrowse() {
        NavController navController = NavHostFragment.findNavController(this);
        NavOptions options = new NavOptions.Builder()
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_graph, false)
                .build();

        navController.navigate(R.id.BrowseFragment, null, options);
    }
}