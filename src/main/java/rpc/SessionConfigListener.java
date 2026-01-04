package rpc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.SessionCookieConfig;

@WebListener
public class SessionConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SessionCookieConfig cfg = sce.getServletContext().getSessionCookieConfig();
        cfg.setHttpOnly(true);
        // Set Secure to true for production/TLS environments. Leave false for local non-TLS tests.
        cfg.setSecure(true);
        cfg.setName("JSESSIONID");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
