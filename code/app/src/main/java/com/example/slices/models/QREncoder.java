package com.example.slices.models;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Function that takes a string and turns it into a QRCode
 * @param
 *      msg - String to encode into a QRCode
 * @returns
 *      Bitmap - bitmap of QRCode, can be used in an ImageView
 */
public class QREncoder {
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
