package rpc;

import service.AuthService;
import util.RpcHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AuthService authService;

    /** Default constructor used by the servlet container. */
    public LoginServlet() {
        this(new AuthService());
    }

    /**
     * Constructor for tests to inject a custom AuthService.
     */
    LoginServlet(AuthService authService) {
        this.authService = authService != null ? authService : new AuthService();
    }

    /**
     * Servlet handling login checks and authentication.
     * - GET: returns current session user info.
     * - POST: authenticates credentials and initializes session + CSRF token.
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        JSONObject result = new JSONObject();
        if (session != null && session.getAttribute("user_id") != null) {
            String userId = session.getAttribute("user_id").toString();
            try {
                String name = authService.getDisplayName(userId);
                result.put("status", "OK").put("user_id", userId).put("name", name);
                RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_OK);
            } catch (service.ServiceException se) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch user info");
            } catch (JSONException je) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response serialization error");
            }
        } else {
            result.put("status", "Session Invalid");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject obj = RpcHelper.readJsonObject(request);
        JSONObject result = new JSONObject();
        try {
            String userId = obj.getString("user_id");
            String password = obj.getString("password");
            if (!util.Validation.isValidUserId(userId) || !util.Validation.isValidPassword(password)) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials format");
                return;
            }
            AuthService.LoginResult r = null;
            try {
                r = authService.login(userId, password);
            } catch (service.TooManyAttemptsException tma) {
                util.ErrorHandler.sendError(request, response, 429, "Too many login attempts");
                return;
            }
            if (r.success()) {
                HttpSession session = request.getSession();
                session.invalidate(); // prevent fixation
                session = request.getSession(true);
                session.setAttribute("user_id", userId);
                session.setMaxInactiveInterval(600);
                // generate CSRF token and expose via cookie
                String csrf = util.CsrfUtil.generateToken();
                util.CsrfUtil.setToken(session, csrf);
                response.setHeader("Set-Cookie", "XSRF-TOKEN=" + csrf + "; Path=/; SameSite=Strict");
                result.put("status", "OK").put("user_id", userId).put("name", r.displayName());
                RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_OK);
            } else {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
            }
        } catch (JSONException e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
        } catch (Exception e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
