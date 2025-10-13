package edu.ensign.cs460.recommendation;

/**
 * DTO for tour recommendations API response
 */
public record TourRecommendation(
    Integer tourId,
    String title,
    Double averageScore,
    Long reviewCount) {
}
