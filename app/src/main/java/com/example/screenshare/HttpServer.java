package com.example.screenshare;

import android.util.Log;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    private final AtomicReference<byte[]> currentFrame = new AtomicReference<>();

    public HttpServer(int port) {
        super(port);
    }

    public void updateFrame(byte[] jpeg) {
        if (jpeg != null && jpeg.length > 0) currentFrame.set(jpeg);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if ("/stream".equals(uri)) {
            return newChunked(MIME_JPEG, new FrameInputStream());
        } else if ("/".equals(uri)) {
            String html = "<html><body><img src='/stream' /></body></html>";
            return newFixedLengthResponse(html);
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found");
        }
    }

    private static final String MIME_JPEG = "multipart/x-mixed-replace; boundary=frame";

    private class FrameInputStream extends InputStream {
        private byte[] boundary = ("--frame\r\nContent-Type: image/jpeg\r\nContent-Length: %d\r\n\r\n").getBytes();
        private byte[] suffix = "\r\n".getBytes();
        private ByteArrayInputStream current = null;

        @Override
        public int read() {
            try {
                byte[] frame = currentFrame.get();
                if (frame == null) {
                    Thread.sleep(100);
                    return -1;
                }
                String head = new String(boundary);
                head = head.replace("%d", String.valueOf(frame.length));
                byte[] headB = head.getBytes();
                byte[] out = new byte[headB.length + frame.length + suffix.length];
                System.arraycopy(headB, 0, out, 0, headB.length);
                System.arraycopy(frame, 0, out, headB.length, frame.length);
                System.arraycopy(suffix, 0, out, headB.length + frame.length, suffix.length);
                current = new ByteArrayInputStream(out);
                return current.read();
            } catch (Exception e) {
                Log.e(TAG, "Frame stream error: " + e.getMessage());
                return -1;
            }
        }
    }
}
