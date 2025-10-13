package edu.ensign.cs460.recommendation;

import edu.ensign.cs460.repo.TourRatingRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

  @Test
  void recommendTopN_mapsProjectionAndRespectsOrder() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);

    var p1 = projection(1, "A", 4.9, 10L);
    var p2 = projection(2, "B", 4.9, 8L); // same avg, fewer reviews => after A
    when(repo.findTopTours(any())).thenReturn(List.of(p1, p2));

    var out = svc.recommendTopN(2);

    // mapping
    assertThat(out).hasSize(2);
    assertThat(out.get(0).tourId()).isEqualTo(1);
    assertThat(out.get(0).title()).isEqualTo("A");
    assertThat(out.get(0).averageScore()).isEqualTo(4.9);
    assertThat(out.get(0).reviewCount()).isEqualTo(10);

    // order from repo preserved
    assertThat(out.get(0).title()).isEqualTo("A");
    assertThat(out.get(1).title()).isEqualTo("B");

    // limit used via PageRequest(0, limit)
    var prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(repo).findTopTours(prCaptor.capture());
    assertThat(prCaptor.getValue().getPageSize()).isEqualTo(2);
  }

  @Test
  void recommendTopN_empty() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);
    when(repo.findTopTours(any())).thenReturn(List.of());
    assertThat(svc.recommendTopN(5)).isEmpty();
  }

  @Test
  void recommendForCustomer_excludesAlreadyRatedAndMaps() {
    var repo = mock(TourRatingRepository.class);
    var svc = new RecommendationService(repo);

    var p1 = projection(10, "Z", 4.2, 3L);
    when(repo.findRecommendedForCustomer(eq(123), any())).thenReturn(List.of(p1));

    var out = svc.recommendForCustomer(123, 5);

    assertThat(out).hasSize(1);
    assertThat(out.get(0).title()).isEqualTo("Z");

    var prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(repo).findRecommendedForCustomer(eq(123), prCaptor.capture());
    assertThat(prCaptor.getValue().getPageSize()).isEqualTo(5);
  }

  private static TourSummary projection(Integer id, String title, Double avg, Long cnt) {
    return new TourSummary() {
      public Integer getTourId() { return id; }
      public String getTitle() { return title; }
      public Double getAvgScore() { return avg; }
      public Long getReviewCount() { return cnt; }
    };
  }
}
