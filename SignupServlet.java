package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Signup
 */
@WebServlet("/register")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignupServlet() {
        super();
		// constructor
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection db = DBConnectionFactory.getConnection();
		JSONObject  obj = RpcHelper.readJsonObject(request);
		
		try {
			String userId = obj.getString("user_id");
			String password = obj.getString("password");
			String firstName = obj.getString("first_name");
			String lastName = obj.getString("last_name");
			
			JSONObject result = new JSONObject();
			if (db.registerUser(userId, password, firstName, lastName)) {
				result.put("Register status", "SUCCESS")
				.put("user_id", userId);
			} else {
				response.setStatus(401); // login failure
				result.put("Register status", "FAILURE");
			}
			
			// give response
			RpcHelper.writeJsonObject(response, result);
			
		} catch (JSONException e) {
			util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
		} finally {
			// close the connection
			try { db.cleanUp(); } catch (Exception ignore) {}
		}
	}

}
