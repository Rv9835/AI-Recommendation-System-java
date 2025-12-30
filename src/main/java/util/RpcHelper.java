package util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utilities for reading and writing JSON over HTTP requests/responses.
 */
public final class RpcHelper {
    private RpcHelper() {}

    /**
     * Read the request body into a JSONObject. Caller should handle IOException/JSONException.
     */
    public static JSONObject readJsonObject(HttpServletRequest request) throws IOException, JSONException {
        String ct = request.getContentType();
        if (ct == null || !ct.toLowerCase().contains("application/json")) {
            throw new IOException("Unsupported Content-Type: " + ct);
        }
        int max = 16 * 1024; // 16KB
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            int read;
            char[] buf = new char[1024];
            int total = 0;
            while ((read = reader.read(buf)) != -1) {
                total += read;
                if (total > max) throw new IOException("Request body too large");
                sb.append(buf, 0, read);
            }
        }
        return new JSONObject(sb.toString());
    }

    public static void writeJsonObject(HttpServletResponse response, JSONObject obj, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(obj);
        }
    }

    public static void writeJsonArray(HttpServletResponse response, JSONArray array, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(array);
        }
    }
}
