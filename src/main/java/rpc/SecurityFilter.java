package rpc;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class SecurityFilter implements Filter {

    /**
     * Global security filter that enforces basic headers, HTTPS redirection,
     * and CSRF token validation for state-changing requests.
     */

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Enforce HTTPS (simple redirect) if behind direct HTTP
        if (request.getScheme().equals("http")) {
            String redirect = "https://" + request.getServerName() + request.getRequestURI();
            if (request.getQueryString() != null) redirect += "?" + request.getQueryString();
            response.sendRedirect(redirect);
            return;
        }

        // Add basic Content Security Policy header
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';");

        // For state-changing requests require CSRF token in header. For other requests generate token if absent.
        // Exempt authentication endpoints which have no session yet.
        String path = request.getRequestURI();
        boolean isAuthEndpoint = path.endsWith("/login") || path.endsWith("/register");
        if (("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()) || "DELETE".equalsIgnoreCase(request.getMethod())) && !isAuthEndpoint) {
            String headerToken = request.getHeader("X-XSRF-TOKEN");
            if (request.getSession(false) == null || !util.CsrfUtil.validate(request.getSession(false), headerToken)) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_FORBIDDEN, "CSRF token missing or invalid");
                return;
            }
        } else {
            // ensure a CSRF token exists and expose via cookie for JS clients
            if (request.getSession(false) == null) {
                request.getSession(true);
            }
            String token = util.CsrfUtil.getToken(request.getSession(false));
            if (token == null) {
                token = util.CsrfUtil.generateToken();
                util.CsrfUtil.setToken(request.getSession(false), token);
            }
            // expose token to JS via cookie; include Secure attribute when request is over HTTPS
            String xsrfCookie = "XSRF-TOKEN=" + token + "; Path=/; SameSite=Strict";
            if (request.isSecure()) {
                xsrfCookie += "; Secure";
            }
            response.addHeader("Set-Cookie", xsrfCookie);
        }

        // Proceed with request
        chain.doFilter(req, res);
    }
}
