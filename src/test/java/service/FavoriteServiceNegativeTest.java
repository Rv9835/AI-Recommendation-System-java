package service;

import db.DBConnection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Negative/edge tests for FavoriteService.
 */
public class FavoriteServiceNegativeTest {

    static class SpyConn implements DBConnection {
        boolean addCalled = false;
        boolean removeCalled = false;
        @Override public void close() {}
        @Override public void addFavorites(String userId, java.util.List<String> itemIds) { addCalled = true; }
        @Override public void removeFavorites(String userId, java.util.List<String> itemIds) { removeCalled = true; }
        @Override public java.util.Set<String> getFavoriteIds(String userId) { return java.util.Collections.emptySet(); }
        @Override public java.util.Set<entity.Item> getFavoriteItems(String userId) { return java.util.Collections.emptySet(); }
        @Override public java.util.Set<String> getCategories(String itemId) { return java.util.Collections.emptySet(); }
        @Override public java.util.List<entity.Item> searchItems(double lat, double lon, String term) { return java.util.Collections.emptyList(); }
        @Override public void saveItem(entity.Item item) {}
        @Override public String getUserName(String userId) { return ""; }
        @Override public boolean verifyLogin(String userId, String password) { return false; }
        @Override public boolean registerUser(String userId, String password, String firstName, String lastName) { return false; }
    }

    @Test
    void addFavorites_withNullOrEmpty_shouldNotCallDb() {
        SpyConn spy = new SpyConn();
        Supplier<DBConnection> sup = () -> spy;
        FavoriteService s = new FavoriteService(sup);
        s.addFavorites("user", null);
        s.addFavorites("user", List.of());
        assertFalse(spy.addCalled);
    }

    @Test
    void removeFavorites_withNullOrEmpty_shouldNotCallDb() {
        SpyConn spy = new SpyConn();
        Supplier<DBConnection> sup = () -> spy;
        FavoriteService s = new FavoriteService(sup);
        s.removeFavorites("user", null);
        s.removeFavorites("user", List.of());
        assertFalse(spy.removeCalled);
    }
}
