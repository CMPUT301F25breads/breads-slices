package com.example.slices.models;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class QREncoder {
    /**
     * Function that takes a string and turns it into a QRCode
     * @param
     *      msg - String to encode into a QRCode
     * @return
     *      Bitmap of QRCode, can be used in an ImageView
     * @author Brad Erdely
     */
    public static Bitmap encode(String msg) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            return barcodeEncoder.encodeBitmap(msg, BarcodeFormat.QR_CODE, 400, 400);
        }
        catch (WriterException e) {
            return null;
        }
    }
}
