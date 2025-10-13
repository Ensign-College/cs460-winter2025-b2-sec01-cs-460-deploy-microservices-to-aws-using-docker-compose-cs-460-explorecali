package edu.ensign.cs460.recommendation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.example.explorecalijpa.ExplorecaliJpaApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Tests for RecommendationController
 */
@SpringBootTest(classes = ExplorecaliJpaApplication.class, webEnvironment = RANDOM_PORT)
public class RecommendationControllerTest {

  private static final int CUSTOMER_ID = 123;
  private static final int LIMIT = 5;

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private RecommendationService serviceMock;

  @Test
  void testGetTopRecommendations_ValidRequest() {
    // Given
    TourRecommendation recommendation1 = new TourRecommendation(1, "Tour 1", 4.5, 10L);
    TourRecommendation recommendation2 = new TourRecommendation(2, "Tour 2", 4.0, 5L);
    List<TourRecommendation> mockRecommendations = Arrays.asList(recommendation1, recommendation2);

    when(serviceMock.recommendTopN(LIMIT)).thenReturn(mockRecommendations);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity("/recommendations/top/" + LIMIT, String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    verify(serviceMock).recommendTopN(LIMIT);
  }

  @Test
  void testGetTopRecommendations_InvalidLimit_TooLow() {
    // When
    ResponseEntity<String> response = restTemplate.getForEntity("/recommendations/top/0", String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void testGetTopRecommendations_InvalidLimit_TooHigh() {
    // When
    ResponseEntity<String> response = restTemplate.getForEntity("/recommendations/top/101", String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void testGetCustomerRecommendations_ValidRequest() {
    // Given
    TourRecommendation recommendation = new TourRecommendation(3, "Customer Tour", 4.8, 15L);
    List<TourRecommendation> mockRecommendations = Arrays.asList(recommendation);

    when(serviceMock.recommendForCustomer(CUSTOMER_ID, LIMIT)).thenReturn(mockRecommendations);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendations/customer/" + CUSTOMER_ID + "?limit=" + LIMIT, String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    verify(serviceMock).recommendForCustomer(CUSTOMER_ID, LIMIT);
  }

  @Test
  void testGetCustomerRecommendations_DefaultLimit() {
    // Given
    TourRecommendation recommendation = new TourRecommendation(4, "Default Tour", 4.2, 8L);
    List<TourRecommendation> mockRecommendations = Arrays.asList(recommendation);

    when(serviceMock.recommendForCustomer(CUSTOMER_ID, 5)).thenReturn(mockRecommendations);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendations/customer/" + CUSTOMER_ID, String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    verify(serviceMock).recommendForCustomer(CUSTOMER_ID, 5);
  }

  @Test
  void testGetCustomerRecommendations_InvalidCustomerId() {
    // When
    ResponseEntity<String> response = restTemplate.getForEntity("/recommendations/customer/0", String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void testGetCustomerRecommendations_InvalidLimit_TooLow() {
    // When
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendations/customer/" + CUSTOMER_ID + "?limit=0", String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void testGetCustomerRecommendations_InvalidLimit_TooHigh() {
    // When
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendations/customer/" + CUSTOMER_ID + "?limit=101", String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

  @Test
  void testGetCustomerRecommendations_EmptyResult() {
    // Given
    when(serviceMock.recommendForCustomer(CUSTOMER_ID, LIMIT)).thenReturn(Arrays.asList());

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/recommendations/customer/" + CUSTOMER_ID + "?limit=" + LIMIT, String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), is("[]"));
    verify(serviceMock).recommendForCustomer(CUSTOMER_ID, LIMIT);
  }

  @Test
  void testClearCache() {
    // When
    ResponseEntity<String> response = restTemplate.exchange(
        "/recommendations/cache",
        org.springframework.http.HttpMethod.DELETE,
        null,
        String.class);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    verify(serviceMock).evictAll();
  }
}
