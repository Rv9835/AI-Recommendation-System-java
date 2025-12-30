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

    /**
     * Close and release underlying resources associated with this wrapper.
     * <p>
     * Implementations should release any JDBC connections or pooled resources here.
     */
    @Override
    void close();

    /**
     * Insert favorite item ids for a user.
     *
     * @param userId user identifier
     * @param itemIds list of item ids to mark as favorite; implementations may ignore empty lists
     * @throws DataAccessException on underlying SQL errors
     */
    void addFavorites(String userId, List<String> itemIds);

    /**
     * Remove favorite item ids for a user.
     *
     * @param userId user identifier
     * @param itemIds list of item ids to unmark as favorite
     * @throws DataAccessException on underlying SQL errors
     */
    void removeFavorites(String userId, List<String> itemIds);

    /**
     * Get favorite item ids for a user.
     *
     * @param userId user identifier
     * @return set of item ids (never null)
     * @throws DataAccessException on underlying SQL errors
     */
    Set<String> getFavoriteIds(String userId);

    /**
     * Get favorite items for a user.
     *
     * @param userId user identifier
     * @return set of Item entities (never null)
     * @throws DataAccessException on underlying SQL errors
     */
    Set<Item> getFavoriteItems(String userId);

    /**
     * Categories for an item.
     *
     * @param itemId item identifier
     * @return set of category strings
     * @throws DataAccessException on underlying SQL errors
     */
    Set<String> getCategories(String itemId);

    /**
     * Search items near a location and optional keyword.
     *
     * @param lat latitude
     * @param lon longitude
     * @param term optional search term (may be null)
     * @return list of matching Items (may be empty)
     * @throws DataAccessException on underlying SQL errors
     */
    List<Item> searchItems(double lat, double lon, String term);

    /**
     * Persist an item if not already stored.
     *
     * @param item the item to persist
     * @throws DataAccessException on underlying SQL errors
     */
    void saveItem(Item item);

    /**
     * User full name.
     *
     * @param userId user identifier
     * @return full display name or empty string if not found
     * @throws DataAccessException on underlying SQL errors
     */
    String getUserName(String userId);

    /**
     * Credential check.
     *
     * @param userId user identifier
     * @param password plaintext password to validate
     * @return true if credentials are valid
     * @throws DataAccessException on underlying SQL errors
     */
    boolean verifyLogin(String userId, String password);

    /**
     * Register a new user.
     *
     * @param userId desired user id
     * @param password plaintext password (will be hashed by implementation)
     * @param firstName optional first name
     * @param lastName optional last name
     * @return true if registration succeeded (false if user already exists)
     * @throws DataAccessException on underlying SQL errors
     */
    boolean registerUser(String userId, String password, String firstName, String lastName);
}
