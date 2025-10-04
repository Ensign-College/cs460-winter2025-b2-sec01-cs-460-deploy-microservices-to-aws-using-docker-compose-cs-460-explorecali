package edu.ensign.cs460.recommendation;

import com.example.explorecalijpa.repo.TourRatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecommendationService.
 * Tests business logic for top N and customer-specific recommendations.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock
  private TourRatingRepository repository;

  private RecommendationService service;

  @BeforeEach
  void setUp() {
    service = new RecommendationService(repository);
  }

  @Test
  void testRecommendTopN_returnsCorrectlyOrderedResults() {
    int limit = 3;
    List<TourSummary> mockSummaries = List.of(
        createMockSummary(1, "Awesome Tour", 4.9, 100L),
        createMockSummary(2, "Great Adventure", 4.8, 85L),
        createMockSummary(3, "Nice Experience", 4.7, 70L));
    when(repository.findTopTours(any(Pageable.class))).thenReturn(mockSummaries);

    List<TourRecommendation> result = service.recommendTopN(limit);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).tourId()).isEqualTo(1);
    assertThat(result.get(0).title()).isEqualTo("Awesome Tour");
    assertThat(result.get(0).averageScore()).isEqualTo(4.9);
    assertThat(result.get(0).reviewCount()).isEqualTo(100L);

    assertThat(result.get(1).tourId()).isEqualTo(2);
    assertThat(result.get(2).tourId()).isEqualTo(3);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findTopTours(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(limit);
  }

  @Test
  void testRecommendTopN_emptyResults() {
    when(repository.findTopTours(any(Pageable.class))).thenReturn(Collections.emptyList());

    List<TourRecommendation> result = service.recommendTopN(5);

    assertThat(result).isEmpty();
    verify(repository).findTopTours(any(Pageable.class));
  }

  @Test
  void testRecommendForCustomer_excludesRatedTours() {
    int customerId = 123;
    int limit = 5;
    List<TourSummary> mockSummaries = List.of(
        createMockSummary(10, "Beach Tour", 4.6, 50L),
        createMockSummary(20, "Mountain Hike", 4.5, 45L));
    when(repository.findRecommendedForCustomer(eq(customerId), any(Pageable.class)))
        .thenReturn(mockSummaries);

    // Act
    List<TourRecommendation> result = service.recommendForCustomer(customerId, limit);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).tourId()).isEqualTo(10);
    assertThat(result.get(0).title()).isEqualTo("Beach Tour");
    assertThat(result.get(1).tourId()).isEqualTo(20);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findRecommendedForCustomer(eq(customerId), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(limit);
  }

  @Test
  void testRecommendForCustomer_noRecommendationsAvailable() {
    // Arrange
    int customerId = 456;
    when(repository.findRecommendedForCustomer(eq(customerId), any(Pageable.class)))
        .thenReturn(Collections.emptyList());

    // Act
    List<TourRecommendation> result = service.recommendForCustomer(customerId, 5);

    // Assert
    assertThat(result).isEmpty();
    verify(repository).findRecommendedForCustomer(eq(customerId), any(Pageable.class));
  }

  @Test
  void testRecommendTopN_correctMapping() {
    // Arrange
    List<TourSummary> mockSummaries = List.of(
        createMockSummary(42, "Test Tour", 4.25, 33L));
    when(repository.findTopTours(any(Pageable.class))).thenReturn(mockSummaries);

    // Act
    List<TourRecommendation> result = service.recommendTopN(1);

    // Assert
    assertThat(result).hasSize(1);
    TourRecommendation rec = result.get(0);
    assertThat(rec.tourId()).isEqualTo(42);
    assertThat(rec.title()).isEqualTo("Test Tour");
    assertThat(rec.averageScore()).isEqualTo(4.25);
    assertThat(rec.reviewCount()).isEqualTo(33L);
  }

  // Helper method to create mock TourSummary
  private TourSummary createMockSummary(Integer tourId, String title, Double avgScore, Long reviewCount) {
    return new TourSummary() {
      @Override
      public Integer getTourId() {
        return tourId;
      }

      @Override
      public String getTitle() {
        return title;
      }

      @Override
      public Double getAvgScore() {
        return avgScore;
      }

      @Override
      public Long getReviewCount() {
        return reviewCount;
      }
    };
  }
}
