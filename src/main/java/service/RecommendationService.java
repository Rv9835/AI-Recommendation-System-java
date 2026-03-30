package service;

import algorithm.RecommendationContentBased;
import entity.Item;
import util.AppLogger;
import org.slf4j.Logger;

import java.util.List;

/**
 * High-level recommendation service that delegates to a pluggable algorithm.
 */
public class RecommendationService {
    private static final Logger log = AppLogger.get(RecommendationService.class);
    private final RecommendationContentBased algo;

    /**
     * Default constructor using production algorithm implementation.
     */
    public RecommendationService() {
        this(new RecommendationContentBased());
    }
//
    /**
     * Constructor for dependency injection (tests can pass a mocked algorithm).
     */
    public RecommendationService(RecommendationContentBased algo) {
        this.algo = algo != null ? algo : new RecommendationContentBased();
    }

    /**
     * Produce recommendations for a user near a given location.
     *
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
