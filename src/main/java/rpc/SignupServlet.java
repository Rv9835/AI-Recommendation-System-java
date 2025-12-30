package rpc;

import service.AuthService;
import util.RpcHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/register")
public class SignupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AuthService authService = new AuthService();

    /**
     * Servlet for user registration. Expects JSON body with `user_id`, `password`,
     * optional `first_name` and `last_name`.
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject obj = RpcHelper.readJsonObject(request);
        JSONObject result = new JSONObject();
        try {
            String userId = obj.getString("user_id");
            String password = obj.getString("password");
            String firstName = obj.optString("first_name", "");
            String lastName = obj.optString("last_name", "");

            if (!util.Validation.isValidUserId(userId) || !util.Validation.isValidPassword(password)) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user_id or password policy not met");
                return;
            }

                boolean ok = false;
                try {
                    ok = authService.register(userId, password, firstName, lastName);
                } catch (service.TooManyAttemptsException tma) {
                    util.ErrorHandler.sendError(request, response, 429, "Too many signup attempts");
                    return;
                }
            if (ok) {
                result.put("status", "SUCCESS").put("user_id", userId);
                RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_OK);
            } else {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_CONFLICT, "User exists or registration failed");
            }
        } catch (JSONException e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
        } catch (Exception e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
