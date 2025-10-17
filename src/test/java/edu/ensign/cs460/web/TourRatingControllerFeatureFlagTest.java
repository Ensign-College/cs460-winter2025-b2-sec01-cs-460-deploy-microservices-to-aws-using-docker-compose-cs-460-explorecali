package edu.ensign.cs460.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.Is.is;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import edu.ensign.cs460.business.TourRatingService;

/**
 * Verifies all /tours/{id}/ratings endpoints return 404 when feature flag is off.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "features.tour-ratings=false")
class TourRatingControllerFeatureFlagTest {

  private static final int TOUR_ID = 999;
  private static final int CUSTOMER_ID = 1000;
  private static final String BASE = "/tours/" + TOUR_ID + "/ratings";
  private static final String DISABLED_MSG = "Tour ratings feature disabled.";

  @Autowired private TestRestTemplate template;
  private TestRestTemplate adminRestTemplate;

  @MockBean private TourRatingService serviceMock;

  @BeforeEach
  void setUp() {
    this.adminRestTemplate = template.withBasicAuth("admin", "admin123");
  }

  @Test
  void getAllRatings_disabled_returns404() {
    ResponseEntity<String> res = adminRestTemplate.getForEntity(BASE, String.class);
    assertNotFound(res);
  }

  @Test
  void getAverage_disabled_returns404() {
    ResponseEntity<String> res = adminRestTemplate.getForEntity(BASE + "/average", String.class);
    assertNotFound(res);
  }

  @Test
  void createRating_disabled_returns404() {
    String body = """
      {"customerId": %d, "score": 3, "comment": "test"}
      """.formatted(CUSTOMER_ID);
    ResponseEntity<String> res = adminRestTemplate.postForEntity(BASE, json(body), String.class);
    assertNotFound(res);
  }

  @Test
  void updateWithPut_disabled_returns404() {
    String body = """
      {"customerId": %d, "score": 4, "comment": "updated"}
      """.formatted(CUSTOMER_ID);
    ResponseEntity<String> res = adminRestTemplate.exchange(
        BASE, HttpMethod.PUT, json(body), String.class);
    assertNotFound(res);
  }

  @Test
  void updateWithPatch_disabled_returns404() {
    String body = """
      {"customerId": %d, "comment": "patched"}
      """.formatted(CUSTOMER_ID);
    ResponseEntity<String> res = adminRestTemplate.exchange(
        BASE, HttpMethod.PATCH, json(body), String.class);
    assertNotFound(res);
  }

  @Test
  void deleteRating_disabled_returns404() {
    ResponseEntity<String> res = adminRestTemplate.exchange(
        BASE + "/" + CUSTOMER_ID, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
    assertNotFound(res);
  }

  @Test
  void batchCreate_disabled_returns404() {
    String url = BASE + "/batch?score=3";
    String body = "[%d,%d]".formatted(CUSTOMER_ID, CUSTOMER_ID + 1);
    ResponseEntity<String> res = adminRestTemplate.postForEntity(url, json(body), String.class);
    assertNotFound(res);
  }

  // ---- helpers ----

  private static HttpEntity<String> json(String body) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setAccept(List.of(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON));
    return new HttpEntity<>(body, h);
  }

  private void assertNotFound(ResponseEntity<String> res) {
    assertThat(res.getStatusCode(), is(HttpStatus.NOT_FOUND));
    // Spring Boot 3 uses ProblemDetail by default; assert the detail message
    assertThat(res.getBody(), containsString(DISABLED_MSG));
  }
}
