package com.example.screenshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {
    public static final String ACTION_STOP = "action_stop";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_INTENT = "result_intent";
    private static final String TAG = "ScreenCaptureService";

    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private MediaProjectionManager projectionManager;
    private HandlerThread handlerThread;
    private Handler handler;
    private HttpServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            if (server != null) server.stop();
            stopForeground(true);
            return START_NOT_STICKY;
        }

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1);
        Intent data = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        if (resultCode != -1 && data != null) {
            startForegroundServiceWithProjection(resultCode, data);
        }
        return START_STICKY;
    }

    private void startForegroundServiceWithProjection(int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ss_channel", "ScreenShare", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "ss_channel")
                .setContentTitle("ScreenShare")
                .setContentText("Compartiendo pantalla")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        int width = 720;
        int height = 1280;
        int density = getResources().getDisplayMetrics().densityDpi;

        imageReader = ImageReader.newInstance(width, height, ImageFormat.RGBA_8888, 2);
        mediaProjection.createVirtualDisplay("ScreenShareDisplay", width, height, density,
                0, imageReader.getSurface(), null, null);

        handlerThread = new HandlerThread("ImageReaderThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    BitmapWrapper bmp = BitmapWrapper.fromImage(image, width, height);
                    byte[] jpeg = BitmapWrapper.toJpeg(bmp);

                    if (server != null) {
                        server.updateFrame(jpeg);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
            } finally {
                if (image != null) image.close();
            }
        }, handler);

        server = new HttpServer(8080);
        try {
            server.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start server: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) server.stop();
        if (mediaProjection != null) mediaProjection.stop();
        if (imageReader != null) imageReader.close();
        if (handlerThread != null) handlerThread.quitSafely();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
