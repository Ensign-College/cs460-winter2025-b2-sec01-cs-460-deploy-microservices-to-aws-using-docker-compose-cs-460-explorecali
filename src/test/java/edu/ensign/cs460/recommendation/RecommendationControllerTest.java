package edu.ensign.cs460.recommendation;

import com.example.explorecalijpa.ExplorecaliJpaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for RecommendationController.
 * Tests REST endpoint behavior, validation, and error handling.
 */
@WebMvcTest(RecommendationController.class)
@ContextConfiguration(classes = { ExplorecaliJpaApplication.class, RecommendationController.class })
class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RecommendationService service;

  @Test
  void testTopEndpoint_validRequest_returns200() throws Exception {
    // Arrange
    List<TourRecommendation> recommendations = List.of(
        new TourRecommendation(1, "Tour A", 4.9, 100L),
        new TourRecommendation(2, "Tour B", 4.8, 85L));
    when(service.recommendTopN(5)).thenReturn(recommendations);

    // Act & Assert
    mockMvc.perform(get("/recommendations/top/5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].tourId", is(1)))
        .andExpect(jsonPath("$[0].title", is("Tour A")))
        .andExpect(jsonPath("$[0].averageScore", is(4.9)))
        .andExpect(jsonPath("$[0].reviewCount", is(100)))
        .andExpect(jsonPath("$[1].tourId", is(2)))
        .andExpect(jsonPath("$[1].title", is("Tour B")));

    verify(service).recommendTopN(5);
  }

  @Test
  void testTopEndpoint_invalidLimit_returns400() throws Exception {
    mockMvc.perform(get("/recommendations/top/0"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendations/top/-1"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendations/top/101"))
        .andExpect(status().isBadRequest());

    verify(service, never()).recommendTopN(anyInt());
  }

  @Test
  void testTopEndpoint_emptyResults_returns200WithEmptyArray() throws Exception {
    when(service.recommendTopN(anyInt())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/recommendations/top/5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void testCustomerEndpoint_validRequest_returns200() throws Exception {
    List<TourRecommendation> recommendations = List.of(
        new TourRecommendation(10, "Coastal Bike Ride", 4.7, 44L),
        new TourRecommendation(25, "Wine Country Day Trip", 4.6, 62L));
    when(service.recommendForCustomer(123, 5)).thenReturn(recommendations);

    mockMvc.perform(get("/recommendations/customer/123?limit=5"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].tourId", is(10)))
        .andExpect(jsonPath("$[0].title", is("Coastal Bike Ride")))
        .andExpect(jsonPath("$[0].averageScore", is(4.7)))
        .andExpect(jsonPath("$[0].reviewCount", is(44)))
        .andExpect(jsonPath("$[1].tourId", is(25)));

    verify(service).recommendForCustomer(123, 5);
  }

  @Test
  void testCustomerEndpoint_defaultLimit_uses5() throws Exception {
    when(service.recommendForCustomer(eq(123), eq(5))).thenReturn(Collections.emptyList());
    mockMvc.perform(get("/recommendations/customer/123"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));

    verify(service).recommendForCustomer(123, 5);
  }

  @Test
  void testCustomerEndpoint_invalidCustomerId_returns400() throws Exception {
    mockMvc.perform(get("/recommendations/customer/0"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendations/customer/-1"))
        .andExpect(status().isBadRequest());

    verify(service, never()).recommendForCustomer(anyInt(), anyInt());
  }

  @Test
  void testCustomerEndpoint_invalidLimit_returns400() throws Exception {
    mockMvc.perform(get("/recommendations/customer/123?limit=0"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendations/customer/123?limit=101"))
        .andExpect(status().isBadRequest());

    verify(service, never()).recommendForCustomer(anyInt(), anyInt());
  }

  @Test
  void testCustomerEndpoint_emptyResults_returns200WithEmptyArray() throws Exception {
    when(service.recommendForCustomer(anyInt(), anyInt())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/recommendations/customer/456?limit=10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void testClearCacheEndpoint_returns204() throws Exception {
    doNothing().when(service).evictAll();

    mockMvc.perform(delete("/recommendations/cache"))
        .andExpect(status().isNoContent());

    verify(service).evictAll();
  }

  @Test
  void testTopEndpoint_boundaryValues_valid() throws Exception {
    when(service.recommendTopN(1)).thenReturn(Collections.emptyList());
    mockMvc.perform(get("/recommendations/top/1"))
        .andExpect(status().isOk());

    when(service.recommendTopN(100)).thenReturn(Collections.emptyList());
    mockMvc.perform(get("/recommendations/top/100"))
        .andExpect(status().isOk());

    verify(service).recommendTopN(1);
    verify(service).recommendTopN(100);
  }

  @Test
  void testCustomerEndpoint_customLimit_overridesDefault() throws Exception {
    when(service.recommendForCustomer(eq(789), eq(10))).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/recommendations/customer/789?limit=10"))
        .andExpect(status().isOk());

    verify(service).recommendForCustomer(789, 10);
    verify(service, never()).recommendForCustomer(789, 5); // Should not use default
  }
}
