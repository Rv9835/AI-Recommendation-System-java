package service;

import db.DBConnection;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    public void login_success_and_displayName() {
        DBConnection mock = new DBConnection() {
            @Override public void close() {}
            @Override public void addFavorites(String userId, java.util.List<String> itemIds) {}
            @Override public void removeFavorites(String userId, java.util.List<String> itemIds) {}
            @Override public java.util.Set<String> getFavoriteIds(String userId) { return java.util.Set.of(); }
            @Override public java.util.Set<entity.Item> getFavoriteItems(String userId) { return java.util.Set.of(); }
            @Override public java.util.Set<String> getCategories(String itemId) { return java.util.Set.of(); }
            @Override public java.util.List<entity.Item> searchItems(double lat, double lon, String term) { return java.util.List.of(); }
            @Override public void saveItem(entity.Item item) {}
            @Override public String getUserName(String userId) { return "Test User"; }
            @Override public boolean verifyLogin(String userId, String password) { return true; }
            @Override public boolean registerUser(String userId, String password, String firstName, String lastName) { return true; }
        };

        AuthService svc = new AuthService((Supplier<DBConnection>)() -> mock);
        AuthService.LoginResult r = svc.login("u","p");
        assertTrue(r.success());
        assertEquals("Test User", r.displayName());
        assertTrue(svc.register("u2","p","F","L"));
        assertEquals("Test User", svc.getDisplayName("u"));
    }
}
package service;

import db.DBConnection;
import db.DBConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    @BeforeEach
    public void setup() {
        // inject fake DBConnection
        DBConnectionFactory.setProviderForTests(() -> new DBConnection() {
            @Override
            public void close() {}

            @Override
            public void addFavorites(String userId, java.util.List<String> itemIds) {}

            @Override
            public void removeFavorites(String userId, java.util.List<String> itemIds) {}

            @Override
            public java.util.Set<String> getFavoriteIds(String userId) { return Collections.emptySet(); }

            @Override
            public java.util.Set<entity.Item> getFavoriteItems(String userId) { return Collections.emptySet(); }

            @Override
            public java.util.Set<String> getCategories(String itemId) { return Collections.emptySet(); }

            @Override
            public java.util.List<entity.Item> searchItems(double lat, double lon, String term) { return Collections.emptyList(); }

            @Override
            public void saveItem(entity.Item item) {}

            @Override
            public String getUserName(String userId) { return "Test User"; }

            @Override
            public boolean verifyLogin(String userId, String password) { return "user1".equals(userId) && "pass".equals(password); }

            @Override
            public boolean registerUser(String userId, String password, String firstName, String lastName) { return true; }
        });
    }

    @AfterEach
    public void teardown() {
        DBConnectionFactory.resetProvider();
    }

    @Test
    public void testLoginSuccess() {
        AuthService s = new AuthService();
        AuthService.LoginResult r = s.login("user1", "pass");
        assertTrue(r.success());
        assertEquals("Test User", r.displayName());
    }

    @Test
    public void testRegister() {
        AuthService s = new AuthService();
        assertTrue(s.register("newuser", "Pass1234", "First", "Last"));
    }
}
