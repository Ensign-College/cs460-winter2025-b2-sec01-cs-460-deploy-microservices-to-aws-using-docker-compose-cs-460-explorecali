package edu.ensign.cs460.recommendation;

import edu.ensign.cs460.repo.TourRatingRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "topTourRecs")
public class RecommendationService {
  private final TourRatingRepository repo;

  public RecommendationService(TourRatingRepository repo) {
    this.repo = repo;
  }

  @Cacheable(key = "'top:' + #limit")
  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendTopN(int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findTopTours(page).stream()
        .map(s -> new TourRecommendation(s.getTourId(), s.getTitle(), s.getAvgScore(), s.getReviewCount()))
        .toList();
  }

  @Cacheable(key = "'cust:' + #customerId + ':' + #limit")
  @Transactional(readOnly = true)
  public List<TourRecommendation> recommendForCustomer(int customerId, int limit) {
    var page = PageRequest.of(0, limit);
    return repo.findRecommendedForCustomer(customerId, page).stream()
        .map(s -> new TourRecommendation(s.getTourId(), s.getTitle(), s.getAvgScore(), s.getReviewCount()))
        .toList();
  }

  @CacheEvict(allEntries = true)
  public void evictAll() {
    // no-op; annotation clears the "topTourRecs" cache
  }
}
