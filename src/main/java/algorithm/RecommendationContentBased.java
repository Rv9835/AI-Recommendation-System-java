package algorithm;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Content-based recommendation algorithm.
 * <p>
 * This implementation consults the database for a user's favorites and then
 * searches for items in the most frequent categories. For testability the
 * {@link Supplier}&lt;DBConnection&gt; can be injected.
 */
public class RecommendationContentBased {
    private final Supplier<DBConnection> provider;

    /**
     * Production constructor using the default DBConnection provider.
     */
    public RecommendationContentBased() {
        this(DBConnectionFactory::getConnection);
    }

    /**
     * Testable constructor that accepts a DBConnection supplier.
     */
    public RecommendationContentBased(Supplier<DBConnection> provider) {
        this.provider = provider != null ? provider : DBConnectionFactory::getConnection;
    }

    /**
     * Recommend items for a user near a location.
     *
     * @param userId user identifier
     * @param lat latitude
     * @param lon longitude
     * @return list of recommended items (may be empty)
     */
    public List<Item> recommend(String userId, double lat, double lon) {
        try (DBConnection connection = provider.get()) {
            Set<String> favoriteIds = connection.getFavoriteIds(userId);

            Map<String, Integer> categoryCounts = new HashMap<>();
            for (String itemId : favoriteIds) {
                for (String category : connection.getCategories(itemId)) {
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }
            }

            List<Entry<String, Integer>> sorted = new ArrayList<>(categoryCounts.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            Set<String> visited = new HashSet<>();
            List<Item> recommendations = new ArrayList<>();
            for (Entry<String, Integer> category : sorted) {
                List<Item> items = connection.searchItems(lat, lon, category.getKey());
                for (Item item : items) {
                    String id = item.getId();
                    if (!favoriteIds.contains(id) && visited.add(id)) {
                        recommendations.add(item);
                    }
                }
            }
            return recommendations;
        }
    }
}
