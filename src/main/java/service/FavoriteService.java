package service;

import db.DBConnection;
import db.DBConnectionFactory;
import java.util.function.Supplier;
import entity.Item;
import util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Business logic for user favorites. Uses a {@link db.DBConnection} supplier so tests
 * can inject mock connections.
 */
public class FavoriteService {
    private static final Logger log = AppLogger.get(FavoriteService.class);
    private final Supplier<DBConnection> provider;

    public FavoriteService() {
        this.provider = DBConnectionFactory::getConnection;
    }

    // Constructor for dependency injection (tests)
    public FavoriteService(Supplier<DBConnection> provider) {
        this.provider = provider != null ? provider : DBConnectionFactory::getConnection;
    }

    /**
     * Retrieve favorite items for a user.
     */
    public Set<Item> getFavorites(String userId) {
        try (DBConnection conn = provider.get()) {
            return conn.getFavoriteItems(userId);
        } catch (Exception e) {
            log.error("Failed to get favorites for {}", userId, e);
            throw new ServiceException("Failed to get favorites", e);
        }
    }

    /**
     * Add favorite item ids for a user. This method is a no-op when the provided
     * list is null or empty.
     */
    public void addFavorites(String userId, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        try (DBConnection conn = provider.get()) {
            conn.addFavorites(userId, new ArrayList<>(itemIds));
        } catch (Exception e) {
            log.error("Failed to add favorites for {}", userId, e);
            throw new ServiceException("Failed to add favorites", e);
        }
    }

    /**
     * Remove favorite item ids for a user. This method is a no-op when the
     * provided list is null or empty.
     */
    public void removeFavorites(String userId, List<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        try (DBConnection conn = provider.get()) {
            conn.removeFavorites(userId, new ArrayList<>(itemIds));
        } catch (Exception e) {
            log.error("Failed to remove favorites for {}", userId, e);
            throw new ServiceException("Failed to remove favorites", e);
        }
    }
}
