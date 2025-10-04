package edu.ensign.cs460.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for tour recommendation endpoints.
 * Provides APIs for discovering top-rated tours and personalized
 * recommendations.
 */
@RestController
@RequestMapping("/recommendations")
@Validated
public class RecommendationController {

  private final RecommendationService service;

  public RecommendationController(RecommendationService service) {
    this.service = service;
  }

  /**
   * Get top N tours ranked by average rating.
   *
   * @param limit number of tours to return (1-100)
   * @return list of top-rated tours
   */
  @GetMapping("/top/{limit}")
  public List<TourRecommendation> top(
      @PathVariable @Min(1) @Max(100) int limit) {
    return service.recommendTopN(limit);
  }

  /**
   * Get personalized tour recommendations for a customer.
   * Returns tours the customer has not yet rated, ordered by rating.
   *
   * @param customerId the customer ID (must be >= 1)
   * @param limit      number of tours to return (1-100, default 5)
   * @return list of recommended tours
   */
  @GetMapping("/customer/{customerId}")
  public List<TourRecommendation> forCustomer(
      @PathVariable @Min(1) int customerId,
      @RequestParam(defaultValue = "5") @Min(1) @Max(100) int limit) {
    return service.recommendForCustomer(customerId, limit);
  }

  /**
   * Clear cached recommendations.
   * Useful for testing and demonstration purposes.
   */
  @DeleteMapping("/cache")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearCache() {
    service.evictAll();
  }
}
