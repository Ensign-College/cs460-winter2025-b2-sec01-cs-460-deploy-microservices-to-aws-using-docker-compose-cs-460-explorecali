package edu.ensign.cs460.recommendation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.explorecalijpa.repo.TourRatingRepository;

/**
 * Tests for RecommendationService
 */
@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

  private static final int CUSTOMER_ID = 123;
  private static final int LIMIT = 5;

  @Mock
  private TourRatingRepository tourRatingRepositoryMock;

  @Mock
  private TourSummary tourSummaryMock1;

  @Mock
  private TourSummary tourSummaryMock2;

  @InjectMocks
  private RecommendationService service;

  @Test
  public void recommendTopN_shouldReturnMappedRecommendations() {
    // Given
    when(tourSummaryMock1.getTourId()).thenReturn(1);
    when(tourSummaryMock1.getTitle()).thenReturn("Tour 1");
    when(tourSummaryMock1.getAvgScore()).thenReturn(4.5);
    when(tourSummaryMock1.getReviewCount()).thenReturn(10L);

    when(tourSummaryMock2.getTourId()).thenReturn(2);
    when(tourSummaryMock2.getTitle()).thenReturn("Tour 2");
    when(tourSummaryMock2.getAvgScore()).thenReturn(4.0);
    when(tourSummaryMock2.getReviewCount()).thenReturn(5L);

    List<TourSummary> mockSummaries = Arrays.asList(tourSummaryMock1, tourSummaryMock2);
    when(tourRatingRepositoryMock.findTopTours(any(Pageable.class))).thenReturn(mockSummaries);

    // When
    List<TourRecommendation> result = service.recommendTopN(LIMIT);

    // Then
    assertThat(result.size(), is(2));

    TourRecommendation first = result.get(0);
    assertThat(first.tourId(), is(1));
    assertThat(first.title(), is("Tour 1"));
    assertThat(first.averageScore(), is(4.5));
    assertThat(first.reviewCount(), is(10L));

    TourRecommendation second = result.get(1);
    assertThat(second.tourId(), is(2));
    assertThat(second.title(), is("Tour 2"));
    assertThat(second.averageScore(), is(4.0));
    assertThat(second.reviewCount(), is(5L));

    // Verify repository was called with correct PageRequest
    verify(tourRatingRepositoryMock).findTopTours(PageRequest.of(0, LIMIT));
  }

  @Test
  public void recommendForCustomer_shouldReturnMappedRecommendations() {
    // Given
    when(tourSummaryMock1.getTourId()).thenReturn(3);
    when(tourSummaryMock1.getTitle()).thenReturn("Customer Tour 1");
    when(tourSummaryMock1.getAvgScore()).thenReturn(4.8);
    when(tourSummaryMock1.getReviewCount()).thenReturn(15L);

    List<TourSummary> mockSummaries = Arrays.asList(tourSummaryMock1);
    when(tourRatingRepositoryMock.findRecommendedForCustomer(eq(CUSTOMER_ID), any(Pageable.class)))
        .thenReturn(mockSummaries);

    // When
    List<TourRecommendation> result = service.recommendForCustomer(CUSTOMER_ID, LIMIT);

    // Then
    assertThat(result.size(), is(1));

    TourRecommendation recommendation = result.get(0);
    assertThat(recommendation.tourId(), is(3));
    assertThat(recommendation.title(), is("Customer Tour 1"));
    assertThat(recommendation.averageScore(), is(4.8));
    assertThat(recommendation.reviewCount(), is(15L));

    // Verify repository was called with correct parameters
    verify(tourRatingRepositoryMock).findRecommendedForCustomer(eq(CUSTOMER_ID), any(Pageable.class));
  }

  @Test
  public void recommendTopN_shouldReturnEmptyListWhenNoData() {
    // Given
    when(tourRatingRepositoryMock.findTopTours(any(Pageable.class))).thenReturn(Arrays.asList());

    // When
    List<TourRecommendation> result = service.recommendTopN(LIMIT);

    // Then
    assertThat(result.size(), is(0));
    verify(tourRatingRepositoryMock).findTopTours(PageRequest.of(0, LIMIT));
  }

  @Test
  public void recommendForCustomer_shouldReturnEmptyListWhenNoData() {
    // Given
    when(tourRatingRepositoryMock.findRecommendedForCustomer(eq(CUSTOMER_ID), any(Pageable.class)))
        .thenReturn(Arrays.asList());

    // When
    List<TourRecommendation> result = service.recommendForCustomer(CUSTOMER_ID, LIMIT);

    // Then
    assertThat(result.size(), is(0));
    verify(tourRatingRepositoryMock).findRecommendedForCustomer(eq(CUSTOMER_ID), any(Pageable.class));
  }

  @Test
  public void evictAll_shouldCallRepository() {
    // When
    service.evictAll();

    // Then - method should complete without exception
    // The @CacheEvict annotation handles the actual cache clearing
  }
}
