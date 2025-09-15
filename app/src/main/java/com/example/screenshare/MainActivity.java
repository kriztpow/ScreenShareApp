package com.example.screenshare;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> projectionLauncher;
    private MediaProjectionManager projectionManager;
    private TextView statusText;
    private Button startBtn, stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        projectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                    serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.getResultCode());
                    serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_INTENT, result.getData());
                    startForegroundService(serviceIntent);
                    statusText.setText("Compartiendo en: " + getDeviceIpAddress() + ":8080/stream");
                } else {
                    statusText.setText("Permiso de captura de pantalla denegado.");
                }
            }
        );

        startBtn.setOnClickListener(v -> {
            Intent intent = projectionManager.createScreenCaptureIntent();
            projectionLauncher.launch(intent);
        });

        stopBtn.setOnClickListener(v -> {
            Intent stop = new Intent(this, ScreenCaptureService.class);
            stop.setAction(ScreenCaptureService.ACTION_STOP);
            startService(stop);
            statusText.setText("Detenido.");
        });

        statusText.setText("IP local: " + getDeviceIpAddress());
    }

    private String getDeviceIpAddress() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }
}
