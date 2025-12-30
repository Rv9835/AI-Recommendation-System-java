package db.mysql;

import db.DBConnection;
import entity.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

/**
 * MySQL-backed implementation of {@link db.DBConnection} using HikariCP for pooling.
 * Methods obtain a short-lived {@link java.sql.Connection} from {@link db.DataSourceManager}
 * and use try-with-resources to ensure proper closure. SQLExceptions are wrapped in
 * {@link db.DataAccessException}.
 */
public class MySQLConnection implements DBConnection {

    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/recommendation?serverTimezone=UTC&useSSL=false");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");

    public MySQLConnection() {
        // no-op: connections are obtained per-method from DataSourceManager
    }

    @Override
    public void close() {
        // nothing to close here; DataSourceManager manages the pool
    }

    @Override
    public void addFavorites(String userId, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        final String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (String itemId : itemIds) {
                ps.setString(1, userId);
                ps.setString(2, itemId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to add favorites", e);
        }
    }

    @Override
    public void removeFavorites(String userId, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        final String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (String itemId : itemIds) {
                ps.setString(1, userId);
                ps.setString(2, itemId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to remove favorites", e);
        }
    }

    @Override
    public Set<String> getFavoriteIds(String userId) {
        final String sql = "SELECT item_id FROM history WHERE user_id = ?";
        Set<String> ids = new HashSet<>();
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString("item_id"));
                }
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to load favorite ids", e);
        }
        return ids;
    }

    @Override
    public Set<Item> getFavoriteItems(String userId) {
        Set<String> ids = getFavoriteIds(userId);
        Set<Item> items = new HashSet<>();
        for (String id : ids) {
            Item item = getItemById(id);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public Set<String> getCategories(String itemId) {
        final String sql = "SELECT category FROM item_categories WHERE item_id = ?";
        Set<String> categories = new HashSet<>();
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("category"));
                }
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to load categories", e);
        }
        return categories;
    }

    @Override
    public List<Item> searchItems(double lat, double lon, String term) {
        StringBuilder sql = new StringBuilder("SELECT * FROM items");
        List<Object> params = new ArrayList<>();
        boolean hasTerm = term != null && !term.trim().isEmpty();
        if (hasTerm) {
            sql.append(" WHERE name LIKE ? OR description LIKE ?");
            String like = "%" + term.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" LIMIT 50");

        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Item> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(buildItem(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to search items", e);
        }
    }

    @Override
    public void saveItem(Item item) {
        if (item == null) {
            return;
        }
        final String sql = "INSERT INTO items (id, name, address, image_url, url, lat, lon, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), image_url = VALUES(image_url), url = VALUES(url), lat = VALUES(lat), lon = VALUES(lon), description = VALUES(description)";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection()) {
            try {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, item.getId());
                    ps.setString(2, item.getName());
                    ps.setString(3, item.getAddress());
                    ps.setString(4, item.getImageUrl());
                    ps.setString(5, item.getUrl());
                    ps.setDouble(6, item.getLat());
                    ps.setDouble(7, item.getLon());
                    ps.setString(8, item.getDescription());
                    ps.executeUpdate();
                }
                saveCategories(c, item);
                c.commit();
            } catch (SQLException e) {
                try { c.rollback(); } catch (SQLException ex) {}
                throw new db.DataAccessException("Failed to save item", e);
            } finally {
                try { c.setAutoCommit(true); } catch (SQLException e) {}
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to save item", e);
        }
    @Override
    public String getUserName(String userId) {
        final String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("first_name") + " " + rs.getString("last_name");
                }
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to fetch user name", e);
        }
        return "";
    }

    @Override
    public boolean verifyLogin(String userId, String password) {
        final String sql = "SELECT password FROM users WHERE user_id = ?";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password");
                    return hash != null && BCrypt.checkpw(password, hash);
                }
                return false;
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to verify login", e);
        }
    }

    @Override
    public boolean registerUser(String userId, String password, String firstName, String lastName) {
        final String sql = "INSERT INTO users (user_id, password, first_name, last_name) VALUES (?, ?, ?, ?)";
        final String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, hashed);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to register user", e);
        }
    }

    private Item getItemById(String itemId) {
        final String sql = "SELECT * FROM items WHERE id = ?";
        try (java.sql.Connection c = db.DataSourceManager.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildItem(rs);
                }
            }
        } catch (SQLException e) {
            throw new db.DataAccessException("Failed to load item", e);
        }
        return null;
    }

    private void saveCategories(java.sql.Connection c, Item item) throws SQLException {
        if (item.getCategories() == null || item.getCategories().isEmpty()) {
            return;
        }
        final String sql = "INSERT IGNORE INTO item_categories (item_id, category) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (String category : item.getCategories()) {
                ps.setString(1, item.getId());
                ps.setString(2, category);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Item buildItem(ResultSet rs) throws SQLException {
        return Item.builder()
                .setId(rs.getString("id"))
                .setName(rs.getString("name"))
                .setAddress(rs.getString("address"))
                .setImageUrl(rs.getString("image_url"))
                .setUrl(rs.getString("url"))
                .setLat(rs.getDouble("lat"))
                .setLon(rs.getDouble("lon"))
                .setDescription(rs.getString("description"))
                .setCategories(getCategories(rs.getString("id")))
                .build();
    }
}
