package com.example.screenshare;

import fi.iki.elonen.NanoHTTPD;
import java.io.InputStream;

public class HttpServer extends NanoHTTPD {

    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        // Respuesta en streaming con imágenes
        return newChunkedResponse(Response.Status.OK, "image/jpeg", new FrameInputStream());
    }

    // Simulación de frames (aquí deberías inyectar frames reales de la captura)
    private static class FrameInputStream extends InputStream {
        @Override
        public int read() {
            return -1; // Fin de flujo (ejemplo)
        }
    }
}
