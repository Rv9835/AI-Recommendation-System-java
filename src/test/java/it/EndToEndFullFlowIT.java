package it;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.CookieHandler;
import java.net.CookieManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end flow: signup -> login -> add favorite -> remove favorite -> verify.
 */
public class EndToEndFullFlowIT {

    private static Server server;

    @AfterAll
    public static void stop() throws Exception {
        if (server != null) server.stop();
        try { db.DataSourceManager.close(); } catch (Exception e) { e.printStackTrace(); }
    }

    @Test
    public void signupLoginAddRemoveFavoriteFlow() throws Exception {
        String url = "jdbc:h2:mem:recommendation_full;DB_CLOSE_DELAY=-1";
        System.setProperty("DB_URL", url);
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");

        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();
        flyway.migrate();

        server = new Server(0);
        ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ctx.setContextPath("/");
        server.setHandler(ctx);

        ctx.addServlet(new ServletHolder(new rpc.SignupServlet()), "/signup");
        ctx.addServlet(new ServletHolder(new rpc.LoginServlet()), "/login");
        ctx.addServlet(new ServletHolder(new rpc.HistoryServlet()), "/history");

        server.start();
        int port = server.getURI().getPort();

        CookieHandler.setDefault(new CookieManager());

        // signup
        String signupPayload = "{\"user_id\":\"e2e_user_full\",\"password\":\"pass123\",\"first_name\":\"E\",\"last_name\":\"2E\"}";
        int sc = postJson("http://127.0.0.1:" + port + "/signup", signupPayload);
        assertEquals(200, sc);

        // login
        String loginPayload = "{\"user_id\":\"e2e_user_full\",\"password\":\"pass123\"}";
        String resp = postJsonWithResponse("http://127.0.0.1:" + port + "/login", loginPayload);
        assertTrue(resp.contains("OK") || resp.contains("SUCCESS"));

        // add favorite (no explicit XSRF header — filter issues cookie and session)
        String favPayload = "{\"favorite\":[\"item-e2e-full\"]}";
        int sc2 = postJsonWithHeaderStatus("http://127.0.0.1:" + port + "/history", favPayload, null);
        assertEquals(200, sc2);

        // remove favorite
        String remPayload = "{\"favorite\":[\"item-e2e-full\"]}";
        int sc3 = deleteJsonWithHeaderStatus("http://127.0.0.1:" + port + "/history", remPayload, null);
        assertEquals(200, sc3);

        // get favorites via GET
        URL getUrl = new URL("http://127.0.0.1:" + port + "/history");
        HttpURLConnection conn = (HttpURLConnection) getUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        int code = conn.getResponseCode();
        assertEquals(200, code);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            String body = sb.toString();
            assertFalse(body.contains("item-e2e-full"));
        }
    }

    private int postJson(String target, String payload) throws Exception {
        URL url = new URL(target);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload.getBytes()); }
        return conn.getResponseCode();
    }

    private String postJsonWithResponse(String target, String payload) throws Exception {
        URL url = new URL(target);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload.getBytes()); }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private int postJsonWithHeaderStatus(String target, String payload, String xsrf) throws Exception {
        URL url = new URL(target);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (xsrf != null) conn.setRequestProperty("X-XSRF-TOKEN", xsrf);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload.getBytes()); }
        return conn.getResponseCode();
    }

    private int deleteJsonWithHeaderStatus(String target, String payload, String xsrf) throws Exception {
        URL url = new URL(target);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");
        if (xsrf != null) conn.setRequestProperty("X-XSRF-TOKEN", xsrf);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload.getBytes()); }
        return conn.getResponseCode();
    }
}
