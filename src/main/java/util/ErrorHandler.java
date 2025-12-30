package util;

import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class ErrorHandler {
    private ErrorHandler() {}

    /**
     * Centralized error response helper used by servlets.
     * Provides JSON responses for API clients and a simple HTML page when the
     * request `Accept` header includes `text/html`.
     */

    public static void sendError(HttpServletResponse resp, int status, String message) {
        Logger log = AppLogger.get(ErrorHandler.class);
        log.warn("Returning error {}: {}", status, message);
        resp.setContentType("application/json");
        resp.setStatus(status);
        String trace = java.util.UUID.randomUUID().toString();
        resp.setHeader("X-Trace-Id", trace);
        JSONObject obj = new JSONObject();
        obj.put("status", "ERROR");
        obj.put("code", status);
        obj.put("message", message);
        obj.put("trace", trace);
        try (PrintWriter w = resp.getWriter()) {
            w.print(obj);
        } catch (IOException e) {
            log.error("Failed to write error response", e);
        }
    }

    public static void sendError(HttpServletRequest req, HttpServletResponse resp, int status, String message) {
        Logger log = AppLogger.get(ErrorHandler.class);
        log.warn("Returning error {} for {}: {}", status, req.getRequestURI(), message);
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.setStatus(status);
            try (PrintWriter w = resp.getWriter()) {
                w.print("<!doctype html><html><head><meta charset=\"utf-8\"><title>Error</title></head><body>");
                w.print("<h1>Server Error</h1>");
                w.print("<p>");
                w.print(escapeHtml(message));
                w.print("</p>");
                w.print("</body></html>");
            } catch (IOException e) {
                log.error("Failed to write HTML error response", e);
            }
        } else {
            sendError(resp, status, message);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
