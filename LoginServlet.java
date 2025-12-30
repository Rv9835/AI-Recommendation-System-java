package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
		// constructor
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection db = DBConnectionFactory.getConnection();
		try {
			// if there is no session, return null
			HttpSession session = request.getSession(false);
			JSONObject result = new JSONObject();
			
			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				result.put("status", "OK")
				.put("user_id", userId)
				.put("name", db.getUserName(userId));
			} else {
				response.setStatus(403);
				result.put("status", "Session Invalid");
			} 
			
			RpcHelper.writeJsonObject(response, result);
		} catch (JSONException e) {
			util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
		} finally {
			// close the connection
			try { db.cleanUp(); } catch (Exception ignore) {}
		}
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
			
			JSONObject result = new JSONObject();
			if (db.verifyLogin(userId, password)) {
				// if there is no session, create a new one
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);
				
				result.put("status", "OK")
				.put("user_id", userId)
				.put("name", db.getUserName(userId));
				
			} else {
				response.setStatus(401); // login failure
				result.put("status", "FAILURE");
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
