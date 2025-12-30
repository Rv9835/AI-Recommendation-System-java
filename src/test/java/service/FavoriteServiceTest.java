package service;

import db.DBConnection;
import entity.Item;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class FavoriteServiceTest {

    @Test
    public void add_and_remove_favorites() {
        DBConnection mock = new DBConnection() {
            private java.util.Set<String> favs = new java.util.HashSet<>();
            @Override public void close() {}
            @Override public void addFavorites(String userId, List<String> itemIds) { favs.addAll(itemIds); }
            @Override public void removeFavorites(String userId, List<String> itemIds) { favs.removeAll(itemIds); }
            @Override public java.util.Set<String> getFavoriteIds(String userId) { return favs; }
            @Override public java.util.Set<Item> getFavoriteItems(String userId) { return java.util.Set.of(); }
            @Override public java.util.Set<String> getCategories(String itemId) { return java.util.Set.of(); }
            @Override public java.util.List<Item> searchItems(double lat, double lon, String term) { return java.util.List.of(); }
            @Override public void saveItem(Item item) {}
            @Override public String getUserName(String userId) { return ""; }
            @Override public boolean verifyLogin(String userId, String password) { return false; }
            @Override public boolean registerUser(String userId, String password, String firstName, String lastName) { return false; }
        };

        FavoriteService svc = new FavoriteService((Supplier<DBConnection>)() -> mock);
        svc.addFavorites("u", List.of("i1","i2"));
        Set<Item> items = svc.getFavorites("u"); // returns empty set from mock
        assertTrue(mock.getFavoriteIds("u").contains("i1"));
        svc.removeFavorites("u", List.of("i1"));
        assertFalse(mock.getFavoriteIds("u").contains("i1"));
    }
}
package service;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FavoriteServiceTest {
    @BeforeEach
    public void setup() {
        DBConnectionFactory.setProviderForTests(() -> new DBConnection() {
            @Override
            public void close() {}

            @Override
            public void addFavorites(String userId, java.util.List<String> itemIds) {}

            @Override
            public void removeFavorites(String userId, java.util.List<String> itemIds) {}

            @Override
            public Set<String> getFavoriteIds(String userId) { return Set.of("i1","i2"); }

            @Override
            public Set<Item> getFavoriteItems(String userId) {
                Set<Item> s = new HashSet<>();
                s.add(Item.builder().setId("i1").setName("Item1").build());
                return s;
            }

            @Override
            public java.util.Set<String> getCategories(String itemId) { return Set.of("cat"); }

            @Override
            public java.util.List<Item> searchItems(double lat, double lon, String term) { return List.of(); }

            @Override
            public void saveItem(Item item) {}

            @Override
            public String getUserName(String userId) { return "Test User"; }

            @Override
            public boolean verifyLogin(String userId, String password) { return false; }

            @Override
            public boolean registerUser(String userId, String password, String firstName, String lastName) { return false; }
        });
    }

    @AfterEach
    public void teardown() { DBConnectionFactory.resetProvider(); }

    @Test
    public void testGetFavorites() {
        FavoriteService svc = new FavoriteService();
        Set<Item> items = svc.getFavorites("u");
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    public void testAddRemoveFavorites() {
        FavoriteService svc = new FavoriteService();
        svc.addFavorites("u", List.of("x"));
        svc.removeFavorites("u", List.of("x"));
        // no exceptions = success
    }
}
