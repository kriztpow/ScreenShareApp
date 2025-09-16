package com.example.screenshare;

import fi.iki.elonen.NanoHTTPD;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

public class HttpServer extends NanoHTTPD {

    // Guardamos el último frame recibido (JPEG en bytes)
    private final AtomicReference<byte[]> latestFrame = new AtomicReference<>();

    public HttpServer(int port) {
        super(port);
    }

    // Método que ScreenCaptureService usará para actualizar el frame
    public void updateFrame(byte[] frame) {
        latestFrame.set(frame);
    }

    @Override
    public Response serve(IHTTPSession session) {
        byte[] frame = latestFrame.get();
        if (frame != null) {
            return newChunkedResponse(Response.Status.OK, "image/jpeg", new ByteArrayInputStream(frame));
        } else {
            return newFixedLengthResponse(Response.Status.NO_CONTENT, "text/plain", "No frame available yet");
        }
    }
}
