package edu.ensign.cs460.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for tour recommendations.
 * Provides business logic for finding top tours and personalized
 * recommendations.
 */
@Service
@CacheConfig(cacheNames = "topTourRecs")
public class RecommendationService {

  private final TourRatingRepository repo;

  public RecommendationService(TourRatingRepository repo) {
    this.repo = repo;
  }

  /**
   * Get top N tours ranked by average rating.
   *
   * @param limit maximum number of tours to return
   * @return list of tour recommendations ordered by rating
   */
  @Cacheable(key = "'top:' + #limit")
  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendTopN(int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findTopTours(page).stream()
        .map(s -> new TourRecommendation(
            s.getTourId(),
            s.getTitle(),
            s.getAvgScore(),
            s.getReviewCount()))
        .toList();
  }

  /**
   * Get personalized tour recommendations for a customer.
   * Returns tours the customer has not yet rated, ordered by average rating.
   *
   * @param customerId the customer ID
   * @param limit      maximum number of tours to return
   * @return list of tour recommendations excluding tours already rated by this
   *         customer
   */
  @Cacheable(key = "'cust:' + #customerId + ':' + #limit")
  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendForCustomer(int customerId, int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findRecommendedForCustomer(customerId, page).stream()
        .map(s -> new TourRecommendation(
            s.getTourId(),
            s.getTitle(),
            s.getAvgScore(),
            s.getReviewCount()))
        .toList();
  }

  /**
   * Clear all cached recommendations.
   * Useful for testing and demo purposes.
   */
  @CacheEvict(allEntries = true)
  public void evictAll() {
    // No-op method; annotation handles cache eviction
  }
}
