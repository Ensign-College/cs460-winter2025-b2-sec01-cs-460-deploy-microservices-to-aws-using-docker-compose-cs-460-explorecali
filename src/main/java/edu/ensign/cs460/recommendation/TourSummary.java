package edu.ensign.cs460.recommendation;

/**
 * Tour Summary interface
 *
 * Created by Antonio Martinez
 */
public interface TourSummary {
    Integer getTourId();
    String getTitle();
    Double getAvgScore();
    Long getReviewCount();
}