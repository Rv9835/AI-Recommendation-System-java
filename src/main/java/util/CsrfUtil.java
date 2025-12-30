package util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpSession;

public final class CsrfUtil {
    private static final SecureRandom rnd = new SecureRandom();
    private static final String ATTR = "XSRF_TOKEN";

    private CsrfUtil() {}

    /**
     * Generate a random CSRF token suitable for use in cookies and headers.
     */
    public static String generateToken() {
        byte[] b = new byte[24];
        rnd.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /** Store the CSRF token in the session. */
    public static void setToken(HttpSession session, String token) {
        session.setAttribute(ATTR, token);
    }

    /** Retrieve the CSRF token from session or null if missing. */
    public static String getToken(HttpSession session) {
        Object o = session.getAttribute(ATTR);
        return o == null ? null : o.toString();
    }

    /** Validate provided token matches session token. */
    public static boolean validate(HttpSession session, String token) {
        String s = getToken(session);
        return s != null && s.equals(token);
    }
}
