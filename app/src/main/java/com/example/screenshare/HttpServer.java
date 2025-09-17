package com.example.screenshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.PixelFormat;
import android.view.WindowManager;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MyHttpServer extends NanoHTTPD {

    private final MediaProjection mediaProjection;
    private final Context context;
    private ImageReader imageReader;
    private Bitmap latestBitmap;
    private Handler backgroundHandler;

    public MyHttpServer(int port, MediaProjection projection, Context ctx) {
        super(port);
        this.mediaProjection = projection;
        this.context = ctx;
        initImageReader();
    }

    private void initImageReader() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        HandlerThread handlerThread = new HandlerThread("ImageReaderThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width, height, density,
                0,
                imageReader.getSurface(),
                null,
                backgroundHandler
        );

        imageReader.setOnImageAvailableListener(reader -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image != null) {
                    int widthImg = image.getWidth();
                    int heightImg = image.getHeight();

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    int pixelStride = image.getPlanes()[0].getPixelStride();
                    int rowStride = image.getPlanes()[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * widthImg;

                    Bitmap bitmap = Bitmap.createBitmap(
                            widthImg + rowPadding / pixelStride,
                            heightImg, Bitmap.Config.ARGB_8888
                    );
                    bitmap.copyPixelsFromBuffer(buffer);

                    latestBitmap = Bitmap.createBitmap(bitmap, 0, 0, widthImg, heightImg);
                    bitmap.recycle();
                }
            } catch (Exception e) {
                Log.e("MyHttpServer", "Error capturing image", e);
            }
        }, backgroundHandler);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if ("/screen.jpg".equals(session.getUri())) {
            try {
                if (latestBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    latestBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    return Response.newFixedLengthResponse(Status.OK, "image/jpeg", baos.toByteArray());
                } else {
                    return Response.newFixedLengthResponse(Status.NO_CONTENT, "text/plain", "No frame yet");
                }
            } catch (Exception e) {
                Log.e("MyHttpServer", "Error serving image", e);
                return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error");
            }
        } else {
            return Response.newFixedLengthResponse(Status.OK, "text/html",
                    "<html><body><h1>Screen Share</h1>" +
                            "<p>Open <a href='/screen.jpg'>/screen.jpg</a> to view the screen.</p>" +
                            "</body></html>");
        }
    }
}
