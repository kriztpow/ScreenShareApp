package com.example.screenshare;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapWrapper {
    private static final String TAG = "BitmapWrapper";

    public final Bitmap bitmap;

    private BitmapWrapper(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public static BitmapWrapper fromImage(Image image, int width, int height) {
        try {
            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            // Create bitmap from ARGB buffer - this is a naive conversion and might not be perfect.
            Bitmap bmp = Bitmap.createBitmap(width + (rowStride / pixelStride) - width, height, Config.ARGB_8888);
            ByteBuffer wrap = ByteBuffer.wrap(data);
            bmp.copyPixelsFromBuffer(wrap);
            Bitmap scaled = Bitmap.createScaledBitmap(bmp, width, height, true);
            return new BitmapWrapper(scaled);
        } catch (Exception e) {
            Log.e(TAG, "fromImage failed: " + e.getMessage());
            return new BitmapWrapper(Bitmap.createBitmap(width, height, Config.ARGB_8888));
        }
    }

    public static byte[] toJpeg(BitmapWrapper bw) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bw.bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
