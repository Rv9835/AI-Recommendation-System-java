package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnectionFactory;
import entity.Item;
import db.DBConnection;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HistoryServlet() {
        super();
		// constructor
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();
		
		DBConnection db = DBConnectionFactory.getConnection();
		try {
			Set<Event> items = db.getFavorites(userId);
			for (Event  item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
		} finally {
			try { db.cleanUp(); } catch (Exception ignore) {}
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection db = DBConnectionFactory.getConnection();
		
		try {
			JSONObject  obj = RpcHelper.readJsonObject(request);
			String userId = obj.getString("user_id");
			// obtain favorite event ids
			JSONArray favorites = obj.getJSONArray("favorite");
			// logged by servlet infrastructure in production; omit noisy prints here
			List<String> eventIds = new ArrayList<>();
			for (int i = 0; i < favorites.length(); i++) {
				eventIds.add(favorites.getString(i));	
			}
			// update database
			db.setFavorites(userId, itemIds);
			// give response
			RpcHelper.writeJsonObject(response, new JSONObject().put("status", "OK"));
			
		} catch (JSONException e) {
			util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
		} finally {
			try { db.cleanUp(); } catch (Exception ignore) {}
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection db = DBConnectionFactory.getConnection();
		JSONObject  obj = RpcHelper.readJsonObject(request);
		try {
			String userId = obj.getString("user_id");
			// obtain favorite event ids
			JSONArray favorites = obj.getJSONArray("favorite");
			List<String> eventIds = new ArrayList<>();
			for (int i = 0; i < favorites.length(); i++) {
				itemIds.add(favorites.getString(i));
			}
			// update database
			db.unsetFavorites(userId, itemIds);
			// give response
			RpcHelper.writeJsonObject(response, new JSONObject().put("status", "OK"));
			
		} catch (JSONException e) {
			util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
		} finally {
			try { db.cleanUp(); } catch (Exception ignore) {}
		}
	}

}
