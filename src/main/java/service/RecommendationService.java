package service;

import algorithm.RecommendationContentBased;
import entity.Item;
import util.AppLogger;
import org.slf4j.Logger;

import java.util.List;

/**
 * High-level recommendation service that delegates to the content-based algorithm.
 */
public class RecommendationService {
    private static final Logger log = AppLogger.get(RecommendationService.class);
    private final RecommendationContentBased algo = new RecommendationContentBased();

    /**
     * Produce recommendations for a user near a given location.
     * @param userId user identifier
     * @param lat latitude
     * @param lon longitude
     * @return list of recommended items
     */
    public List<Item> recommend(String userId, double lat, double lon) {
        try {
            return algo.recommend(userId, lat, lon);
        } catch (Exception e) {
            log.error("Recommendation failed for {}", userId, e);
            throw new ServiceException("Recommendation failed", e);
        }
    }
}
