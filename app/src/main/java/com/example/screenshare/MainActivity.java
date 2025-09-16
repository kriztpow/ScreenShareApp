package com.example.screenshare;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaProjectionManager projectionManager;
    private ActivityResultLauncher<Intent> projectionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        // Inicializamos el launcher para pedir permiso de captura de pantalla
        projectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent serviceIntent = new Intent(MainActivity.this, ScreenCaptureService.class);
                            serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.getResultCode());
                            serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_INTENT, result.getData());
                            startForegroundService(serviceIntent);
                        }
                    }
                }
        );

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            if (projectionManager != null) {
                Intent intent = projectionManager.createScreenCaptureIntent();
                projectionLauncher.launch(intent);
            }
        });

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> {
            Intent stop = new Intent(MainActivity.this, ScreenCaptureService.class);
            stop.setAction(ScreenCaptureService.ACTION_STOP);
            startService(stop);
        });
    }
}
