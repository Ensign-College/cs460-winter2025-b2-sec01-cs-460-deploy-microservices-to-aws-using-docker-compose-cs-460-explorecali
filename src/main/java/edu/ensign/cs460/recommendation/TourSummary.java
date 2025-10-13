package edu.ensign.cs460.recommendation;

/**
 * JPA Projection interface for tour summary data from database queries
 */
public interface TourSummary {
  Integer getTourId();

  String getTitle();

  Double getAvgScore();

  Long getReviewCount();
}
