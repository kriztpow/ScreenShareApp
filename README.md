# ScreenShareApp

Simple Android app that captures the device screen using MediaProjection API and serves MJPEG frames
over HTTP using NanoHTTPD. Open `http://<device-ip>:8080/stream` from any device on the same WiFi to view.

Notes:
- minSdk = 30
- Uses a foreground service for MediaProjection.
- The MJPEG serving implementation is intentionally simple. For production use consider an encoded H.264 stream.
