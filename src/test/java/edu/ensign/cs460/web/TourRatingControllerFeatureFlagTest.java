package edu.ensign.cs460.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import edu.ensign.cs460.business.TourRatingService;

/**
 * Tests that verify the tour rating API can be disabled via feature flags.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "features.tour-ratings=false")
public class TourRatingControllerFeatureFlagTest {

  private static final int TOUR_ID = 999;
  private static final String TOUR_RATINGS_URL = "/tours/" + TOUR_ID + "/ratings";

  @Autowired private TestRestTemplate template;
  @Autowired private TestRestTemplate adminRestTemplate;
    
  @BeforeEach
  void setUp() {
    this.adminRestTemplate = template.withBasicAuth("admin", "admin123");
  }

  @MockBean
  private TourRatingService serviceMock;

  @Test
  void ratingsDisabledReturnsNotFound() {
    ResponseEntity<String> res = adminRestTemplate.getForEntity(TOUR_RATINGS_URL, String.class);
    assertThat(res.getStatusCode(), is(HttpStatus.NOT_FOUND));
  }
}
