package com.example.screenshare;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE = 1000;
    private TextView linkView;
    private BroadcastReceiver linkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkView = findViewById(R.id.linkView);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        MediaProjectionManager projectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startButton.setOnClickListener(v -> {
            Intent intent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_CODE);
        });

        stopButton.setOnClickListener(v -> {
            Intent stopIntent = new Intent(MainActivity.this, ScreenCaptureService.class);
            stopIntent.setAction(ScreenCaptureService.ACTION_STOP);
            startService(stopIntent);
        });

        linkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String link = intent.getStringExtra("link");
                linkView.setText("Streaming at: " + link);
            }
        };
        registerReceiver(linkReceiver, new IntentFilter(ScreenCaptureService.ACTION_BROADCAST_LINK));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
            serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode);
            serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_INTENT, data);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(linkReceiver);
    }
}
