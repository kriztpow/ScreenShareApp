package com.example.screenshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ScreenCaptureService extends Service {

    // ✅ Constantes públicas accesibles desde MainActivity
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    public static final String EXTRA_RESULT_INTENT = "EXTRA_RESULT_INTENT";
    public static final String ACTION_STOP = "ACTION_STOP";

    private static final String CHANNEL_ID = "ScreenShareChannel";

    private MediaProjection mediaProjection;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_STOP.equals(intent.getAction())) {
                stopSelf();
                return START_NOT_STICKY;
            }

            int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1);
            Intent data = intent.getParcelableExtra(EXTRA_RESULT_INTENT);

            if (resultCode != -1 && data != null) {
                MediaProjectionManager projectionManager =
                        (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

                if (projectionManager != null) {
                    mediaProjection = projectionManager.getMediaProjection(resultCode, data);

                    // 🔴 TODO: Aquí deberías implementar la lógica de streaming (a través de socket/HTTP)
                }
            }
        }

        // Obligatorio para foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Sharing")
                .setContentText("Compartiendo pantalla en la red...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Share Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
