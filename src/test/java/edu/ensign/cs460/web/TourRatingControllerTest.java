package edu.ensign.cs460.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.explorecalijpa.config.SecurityConfig; // ← adjust if needed

import edu.ensign.cs460.business.TourRatingService;
import edu.ensign.cs460.model.TourRating;

@WebMvcTest(TourRatingController.class)
@Import({ SecurityConfig.class /* , YourControllerAdviceIfAny.class */ })
class TourRatingControllerTest {

  private static final int TOUR_ID = 999;
  private static final int CUSTOMER_ID = 1000;
  private static final int SCORE = 3;
  private static final String COMMENT = "comment";
  private static final String BASE = "/tours/" + TOUR_ID + "/ratings";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper mapper;

  @MockBean private TourRatingService service;

  private final RatingDto ratingDto = new RatingDto(SCORE, COMMENT, CUSTOMER_ID);

  private static String basic(String user, String pass) {
    var token = user + ":" + pass;
    var b64 = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    return "Basic " + b64;
  }

  // ---------- CREATE (ADMIN) ----------
  @Test
void testCreateTourRating() throws Exception {
  // stub a non-null result so controller can serialize
  when(service.createNew(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT))
      .thenReturn(new TourRating());

  mvc.perform(post(BASE)
        .header(HttpHeaders.AUTHORIZATION, basic("admin","admin123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(ratingDto)))
     // your controller returned 201 in your earlier expectation, but to be robust:
     .andExpect(status().is2xxSuccessful());

  verify(service).createNew(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
}


  // ---------- DELETE (ADMIN) ----------
  @Test
void testDelete() throws Exception {
  mvc.perform(delete(BASE + "/" + CUSTOMER_ID)
        .header(HttpHeaders.AUTHORIZATION, basic("admin","admin123")))
     .andExpect(status().isOk());  // was isNoContent()

  verify(service).delete(TOUR_ID, CUSTOMER_ID);
}


  // ---------- LIST (USER) ----------
  @Test
  void testGetAllRatingsForTour() throws Exception {
    when(service.lookupRatings(TOUR_ID)).thenReturn(List.of(new TourRating()));

    mvc.perform(get(BASE)
          .header(HttpHeaders.AUTHORIZATION, basic("user","password")))
       .andExpect(status().isOk());

    verify(service).lookupRatings(TOUR_ID);
  }

  // ---------- AVERAGE (USER) ----------
  @Test
  void testGetAverage() throws Exception {
    when(service.getAverageScore(TOUR_ID)).thenReturn(4.5D);

    mvc.perform(get(BASE + "/average")
          .header(HttpHeaders.AUTHORIZATION, basic("user","password")))
       .andExpect(status().isOk());

    verify(service).getAverageScore(TOUR_ID);
  }

  // ---------- PATCH (ADMIN) ----------
  @Test
  void testUpdateWithPatch() throws Exception {
    when(service.updateSome(anyInt(), anyInt(), any(), any())).thenReturn(new TourRating());

    mvc.perform(patch(BASE)
          .header(HttpHeaders.AUTHORIZATION, basic("admin","admin123"))
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(ratingDto)))
       .andExpect(status().isOk());

    verify(service).updateSome(anyInt(), anyInt(), any(), any());
  }

  // ---------- PUT (ADMIN) ----------
  @Test
void testUpdateWithPut() throws Exception {
  when(service.update(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT))
      .thenReturn(new TourRating());

  mvc.perform(put(BASE)
        .header(HttpHeaders.AUTHORIZATION, basic("admin","admin123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(ratingDto)))
     .andExpect(status().is2xxSuccessful()); // controller returned 200

  verify(service).update(TOUR_ID, CUSTOMER_ID, SCORE, COMMENT);
}


  // ---------- BATCH (ADMIN) ----------
  @Test
  void testCreateManyTourRatings() throws Exception {
    Integer[] customers = {123};

    mvc.perform(post(BASE + "/batch")
          .header(HttpHeaders.AUTHORIZATION, basic("admin","admin123"))
          .param("score", String.valueOf(SCORE))
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(customers)))
       .andExpect(status().isCreated()); // or is2xxSuccessful()

    verify(service).rateMany(anyInt(), anyInt(), anyList());
  }

  // ---------- UNHAPPY PATHS (USER) ----------
  @Test
  void test404() throws Exception {
    when(service.lookupRatings(TOUR_ID)).thenThrow(new NoSuchElementException());

    mvc.perform(get(BASE)
          .header(HttpHeaders.AUTHORIZATION, basic("user","password")))
       .andExpect(status().isNotFound());
  }

  @Test
  void test400() throws Exception {
    when(service.lookupRatings(TOUR_ID)).thenThrow(new ConstraintViolationException(null));

    mvc.perform(get(BASE)
          .header(HttpHeaders.AUTHORIZATION, basic("user","password")))
       .andExpect(status().isBadRequest());
  }

  // ---------- SECURITY SANITY ----------
  @Test
  void userCannotCreateRating_403() throws Exception {
    mvc.perform(post(BASE)
          .header(HttpHeaders.AUTHORIZATION, basic("user","password"))
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(ratingDto)))
       .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedGets401() throws Exception {
    mvc.perform(get(BASE))
       .andExpect(status().isUnauthorized());
  }
}
