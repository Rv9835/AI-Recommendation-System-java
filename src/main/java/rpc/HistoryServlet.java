package rpc;

import service.FavoriteService;
import util.RpcHelper;
import entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final FavoriteService favoriteService;

    /** Default constructor used by the servlet container. */
    public HistoryServlet() {
        this(new FavoriteService());
    }

    /**
     * Constructor for tests to inject a custom FavoriteService.
     */
    HistoryServlet(FavoriteService favoriteService) {
        this.favoriteService = favoriteService != null ? favoriteService : new FavoriteService();
    }

    /**
     * Servlet to manage user favorite items. GET returns favorites; POST adds; DELETE removes.
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        JSONObject result = new JSONObject();
        if (session == null || session.getAttribute("user_id") == null) {
            result.put("status", "SESSION_INVALID");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = session.getAttribute("user_id").toString();
        JSONArray array = new JSONArray();
        try {
            for (Item item : favoriteService.getFavorites(userId)) {
                array.put(item.toJSONObject().put("favorite", true));
            }
            RpcHelper.writeJsonArray(response, array, HttpServletResponse.SC_OK);
        } catch (service.ServiceException se) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load favorites");
        } catch (JSONException je) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Response serialization error");
        } catch (Exception e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject obj = RpcHelper.readJsonObject(request);
        JSONObject result = new JSONObject();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            result.put("status", "SESSION_INVALID");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = session.getAttribute("user_id").toString();
        try {
            JSONArray favs = obj.getJSONArray("favorite");
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < favs.length(); i++) {
                ids.add(favs.getString(i));
            }
            // CSRF protection: validate header token
            String headerToken = request.getHeader("X-XSRF-TOKEN");
            if (!util.CsrfUtil.validate(session, headerToken)) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_FORBIDDEN, "CSRF token missing or invalid");
                return;
            }
            favoriteService.addFavorites(userId, ids);
            result.put("status", "OK");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_OK);
        } catch (JSONException e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input");
        } catch (service.ServiceException se) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add favorites");
        } catch (Exception e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject obj = RpcHelper.readJsonObject(request);
        JSONObject result = new JSONObject();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            result.put("status", "SESSION_INVALID");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = session.getAttribute("user_id").toString();
        try {
            JSONArray favs = obj.getJSONArray("favorite");
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < favs.length(); i++) {
                ids.add(favs.getString(i));
            }
            // CSRF protection: validate header token
            String headerToken = request.getHeader("X-XSRF-TOKEN");
            if (!util.CsrfUtil.validate(session, headerToken)) {
                util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_FORBIDDEN, "CSRF token missing or invalid");
                return;
            }
            favoriteService.removeFavorites(userId, ids);
            result.put("status", "OK");
            RpcHelper.writeJsonObject(response, result, HttpServletResponse.SC_OK);
        } catch (JSONException e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input");
        } catch (service.ServiceException se) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to remove favorites");
        } catch (Exception e) {
            util.ErrorHandler.sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
