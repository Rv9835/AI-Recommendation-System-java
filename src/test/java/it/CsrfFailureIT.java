package it;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import javax.servlet.DispatcherType;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: ensure SecurityFilter rejects state-changing requests missing X-XSRF-TOKEN header.
 */
public class CsrfFailureIT {
    private static Server server;

    @AfterAll
    public static void stop() throws Exception {
        if (server != null) server.stop();
        try { db.DataSourceManager.close(); } catch (Exception ignore) {}
    }

    @Test
    public void postWithoutCsrfHeaderIsForbidden() throws Exception {
        String url = "jdbc:h2:mem:recommendation_csrf;DB_CLOSE_DELAY=-1";
        System.setProperty("DB_URL", url);
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");

        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();
        flyway.migrate();

        server = new Server(0);
        ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ctx.setContextPath("/");
        server.setHandler(ctx);

        // register filter and servlets explicitly to ensure behavior
        ctx.addFilter(new FilterHolder(new rpc.SecurityFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        ctx.addServlet(new ServletHolder(new rpc.SignupServlet()), "/signup");
        ctx.addServlet(new ServletHolder(new rpc.LoginServlet()), "/login");
        ctx.addServlet(new ServletHolder(new rpc.HistoryServlet()), "/history");

        server.start();
        int port = server.getURI().getPort();

        CookieHandler.setDefault(new CookieManager());

        // signup and login to obtain session and cookies
        String signupPayload = "{" + "\"user_id\":\"csrf_user\",\"password\":\"pass123\",\"first_name\":\"C\",\"last_name\":\"S\"}";
        int sc = postJson("http://127.0.0.1:" + port + "/signup", signupPayload);
        assertEquals(200, sc);

        String loginPayload = "{" + "\"user_id\":\"csrf_user\",\"password\":\"pass123\"}";
        int sc2 = postJson("http://127.0.0.1:" + port + "/login", loginPayload);
        assertEquals(200, sc2);

        // Now POST to /history without X-XSRF-TOKEN header (CookieManager will include cookies)
        String favPayload = "{\"favorite\":[\"item-csrf\"]}";
        URL target = new URL("http://127.0.0.1:" + port + "/history");
        HttpURLConnection conn = (HttpURLConnection) target.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(favPayload.getBytes()); }

        int code = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, code);
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
}
