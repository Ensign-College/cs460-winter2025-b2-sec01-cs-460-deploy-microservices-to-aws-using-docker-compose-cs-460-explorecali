package edu.ensign.cs460.recommendation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import edu.ensign.cs460.business.TourPackageService;
import edu.ensign.cs460.business.TourService;
import edu.ensign.cs460.business.TourRatingService;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

  @Autowired private MockMvc mvc;

  // Mocks required by your @SpringBootApplication autowiring (third workaround)
  @MockBean private edu.ensign.cs460.business.TourPackageService tourPackageService;
  @MockBean private edu.ensign.cs460.business.TourService tourService;
  @MockBean private edu.ensign.cs460.business.TourRatingService tourRatingService;

  // Controller dependency
  @MockBean
  private RecommendationService service;

  // Mock beans required by the @SpringBootApplication class so the test context can start

  @Test
  void top_valid_returns200Json() throws Exception {
    when(service.recommendTopN(3)).thenReturn(List.of(
        new TourRecommendation(1, "A", 4.5, 7L),
        new TourRecommendation(2, "B", 4.3, 5L)
    ));

    mvc.perform(get("/recommendations/top/3").accept(MediaType.APPLICATION_JSON))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$[0].tourId").value(1))
       .andExpect(jsonPath("$[0].title").value("A"))
       .andExpect(jsonPath("$[0].averageScore").value(4.5))
       .andExpect(jsonPath("$[0].reviewCount").value(7))
       .andExpect(jsonPath("$[1].tourId").value(2));
  }

  @Test
  void top_invalidLimit_returns400() throws Exception {
    mvc.perform(get("/recommendations/top/0")).andExpect(status().isBadRequest());
    mvc.perform(get("/recommendations/top/101")).andExpect(status().isBadRequest());
  }

  @Test
  void customer_defaultLimit5_andValid() throws Exception {
    when(service.recommendForCustomer(42, 5)).thenReturn(List.of());

    mvc.perform(get("/recommendations/customer/42"))
       .andExpect(status().isOk())
       .andExpect(content().json("[]"));
  }

  @Test
  void customer_withExplicitLimit_ok() throws Exception {
    when(service.recommendForCustomer(7, 9))
        .thenReturn(List.of(new TourRecommendation(11, "C", 4.9, 12L)));

    mvc.perform(get("/recommendations/customer/7").param("limit", "9"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$[0].title").value("C"));
  }

  @Test
  void customer_invalidParams_returns400() throws Exception {
    mvc.perform(get("/recommendations/customer/0")).andExpect(status().isBadRequest());
    mvc.perform(get("/recommendations/customer/5").param("limit", "0"))
       .andExpect(status().isBadRequest());
  }

  @Test
  void clearCache_returns204_andEvicts() throws Exception {
    mvc.perform(delete("/recommendations/cache"))
       .andExpect(status().isNoContent());

    verify(service).evictAll();
  }
}
