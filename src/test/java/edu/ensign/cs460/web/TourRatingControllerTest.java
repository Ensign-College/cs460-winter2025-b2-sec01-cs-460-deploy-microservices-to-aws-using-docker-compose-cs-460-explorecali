package edu.ensign.cs460.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ensign.cs460.config.SecurityConfig;
import edu.ensign.cs460.business.TourRatingService;
import edu.ensign.cs460.model.TourRating;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ SecurityConfig.class })
class TourRatingControllerTest {

  private static final int TOUR_ID = 999;
  private static final int CUSTOMER_ID = 1000;
  private static final int SCORE = 3;
  private static final String COMMENT = "comment";
  private static final String BASE = "/tours/{tourId}/ratings";

  @Autowired private ObjectMapper mapper;
  @Autowired private TestRestTemplate template;

  @MockBean private TourRatingService service;

  private TestRestTemplate userRestTemplate;
  private TestRestTemplate adminRestTemplate;

  private final RatingDto ratingDto = new RatingDto(SCORE, COMMENT, CUSTOMER_ID);

  @BeforeEach
  void setUp() {
    this.userRestTemplate = template.withBasicAuth("user", "password");
    this.adminRestTemplate = template.withBasicAuth("admin", "admin123");
  }

  @Test
  void testCreateTourRating() throws Exception {
    when(service.createNew(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT))
        .thenReturn(new TourRating());

    ResponseEntity<Void> resp =
        adminRestTemplate.postForEntity(BASE, ratingDto, Void.class, TOUR_ID);

    assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    verify(service).createNew(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
  }

  @Test
  void testDelete() {
    ResponseEntity<Void> resp =
        adminRestTemplate.exchange(
            BASE + "/{customerId}", HttpMethod.DELETE, null, Void.class, TOUR_ID, CUSTOMER_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(service).delete(TOUR_ID, CUSTOMER_ID);
  }

  @Test
  void testGetAllRatingsForTour() {
    when(service.lookupRatings(TOUR_ID)).thenReturn(List.of(new TourRating()));

    ResponseEntity<TourRating[]> resp =
        userRestTemplate.getForEntity(BASE, TourRating[].class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(service).lookupRatings(TOUR_ID);
  }

  @Test
  void testGetAverage() {
    when(service.getAverageScore(TOUR_ID)).thenReturn(4.5D);

    ResponseEntity<Double> resp =
        userRestTemplate.getForEntity(BASE + "/average", Double.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(service).getAverageScore(TOUR_ID);
  }

  @Test
  void testUpdateWithPatch() throws Exception {
    when(service.updateSome(anyInt(), anyInt(), any(), any())).thenReturn(new TourRating());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(ratingDto), headers);

    ResponseEntity<Void> resp =
        adminRestTemplate.exchange(BASE, HttpMethod.PATCH, entity, Void.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(service).updateSome(anyInt(), anyInt(), any(), any());
  }

  @Test
  void testUpdateWithPut() throws Exception {
    when(service.update(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT))
        .thenReturn(new TourRating());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(ratingDto), headers);

    ResponseEntity<Void> resp =
        adminRestTemplate.exchange(BASE, HttpMethod.PUT, entity, Void.class, TOUR_ID);

    assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    verify(service).update(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
  }

  @Test
  void testCreateManyTourRatings() throws Exception {
    Integer[] customers = {123};

    ResponseEntity<Void> resp =
        adminRestTemplate.postForEntity(
            BASE + "/batch?score={score}", customers, Void.class, TOUR_ID, SCORE);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    verify(service).rateMany(anyInt(), anyInt(), anyList());
  }

  @Test
  void test404() {
    when(service.lookupRatings(TOUR_ID)).thenThrow(new NoSuchElementException());

    ResponseEntity<String> resp =
        userRestTemplate.getForEntity(BASE, String.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void test400() {
    when(service.lookupRatings(TOUR_ID)).thenThrow(new ConstraintViolationException(null));

    ResponseEntity<String> resp =
        userRestTemplate.getForEntity(BASE, String.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void userCannotCreateRating_403() throws Exception {
    ResponseEntity<String> resp =
        userRestTemplate.postForEntity(BASE, ratingDto, String.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void unauthenticatedGets401() {
    ResponseEntity<String> resp =
        template.getForEntity(BASE, String.class, TOUR_ID);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
