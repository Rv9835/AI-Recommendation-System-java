package service;

import db.DBConnection;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for authentication throttling logic in {@link AuthService}.
 */
public class AuthServiceThrottlingTest {

    static class StubConn implements DBConnection {
        @Override public void close() {}
        @Override public void addFavorites(String userId, java.util.List<String> itemIds) {}
        @Override public void removeFavorites(String userId, java.util.List<String> itemIds) {}
        @Override public java.util.Set<String> getFavoriteIds(String userId) { return java.util.Collections.emptySet(); }
        @Override public java.util.Set<entity.Item> getFavoriteItems(String userId) { return java.util.Collections.emptySet(); }
        @Override public java.util.Set<String> getCategories(String itemId) { return java.util.Collections.emptySet(); }
        @Override public java.util.List<entity.Item> searchItems(double lat, double lon, String term) { return java.util.Collections.emptyList(); }
        @Override public void saveItem(entity.Item item) {}
        @Override public String getUserName(String userId) { return ""; }
        @Override public boolean verifyLogin(String userId, String password) { return false; } // always fail
        @Override public boolean registerUser(String userId, String password, String firstName, String lastName) { return false; }
    }

    @Test
    void throttlingThrowsAfterMaxAttempts() {
        Supplier<DBConnection> sup = () -> new StubConn();
        AuthService s = new AuthService(sup);
        String user = "user1";
        // MAX_ATTEMPTS is 5 in AuthService; make 5 failed attempts first
        for (int i = 0; i < 5; i++) {
            AuthService.LoginResult r = s.login(user, "badpass");
            assertFalse(r.success());
        }
        // Next attempt should throw TooManyAttemptsException
        assertThrows(TooManyAttemptsException.class, () -> s.login(user, "badpass"));
    }
}
