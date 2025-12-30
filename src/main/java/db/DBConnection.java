package db;

import java.util.List;
import java.util.Set;

import entity.Item;

/**
 * Abstraction for data access operations used by the service and servlet layers.
 * Implementations should acquire connections from a pool and wrap SQL errors in
 * {@link DataAccessException} so callers receive a consistent checked runtime exception.
 */
public interface DBConnection extends AutoCloseable {

    /** Close and release underlying resources. */
    @Override
    void close();

    /** Insert favorite item ids for a user. */
    void addFavorites(String userId, List<String> itemIds);

    /** Remove favorite item ids for a user. */
    void removeFavorites(String userId, List<String> itemIds);

    /** Get favorite item ids for a user. */
    Set<String> getFavoriteIds(String userId);

    /** Get favorite items for a user. */
    Set<Item> getFavoriteItems(String userId);

    /** Categories for an item. */
    Set<String> getCategories(String itemId);

    /**
     * Search items near a location and optional keyword.
     */
    List<Item> searchItems(double lat, double lon, String term);

    /** Persist an item if not already stored. */
    void saveItem(Item item);

    /** User full name. */
    String getUserName(String userId);

    /** Credential check. */
    boolean verifyLogin(String userId, String password);

    /** Register a new user. */
    boolean registerUser(String userId, String password, String firstName, String lastName);
}
