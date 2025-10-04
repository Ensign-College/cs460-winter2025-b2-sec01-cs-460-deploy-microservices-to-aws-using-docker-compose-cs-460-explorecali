package edu.ensign.cs460.recommendation;

/**
 * JPA Projection interface for tour recommendation queries.
 * Used to efficiently fetch aggregate rating data from the database.
 */
public interface TourSummary {
  Integer getTourId();

  String getTitle();

  Double getAvgScore();

  Long getReviewCount();
}
