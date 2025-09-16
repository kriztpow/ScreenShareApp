package com.example.screenshare;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaProjectionManager projectionManager;

    private final ActivityResultLauncher<Intent> projectionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                    serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.getResultCode());
                    serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_INTENT, result.getData());
                    startForegroundService(serviceIntent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> {
            if (projectionManager != null) {
                Intent captureIntent = projectionManager.createScreenCaptureIntent();
                projectionLauncher.launch(captureIntent);
            }
        });

        stopButton.setOnClickListener(v -> {
            Intent stop = new Intent(this, ScreenCaptureService.class);
            stop.setAction(ScreenCaptureService.ACTION_STOP);
            startService(stop);
        });
    }
}
