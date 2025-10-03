package edu.ensign.cs460.recommendation;

/**
 * Tour Recommendation record
 *
 * Created by Antonio Martinez
 */
public record TourRecommendation(
    Integer tourId,
    String title,
    Double averageScore,
    Long reviewCount
) {}