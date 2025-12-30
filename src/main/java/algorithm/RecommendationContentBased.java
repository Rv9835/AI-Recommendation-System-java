package algorithm;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RecommendationContentBased {

    public List<Item> recommend(String userId, double lat, double lon) {
        try (DBConnection connection = DBConnectionFactory.getConnection()) {
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
