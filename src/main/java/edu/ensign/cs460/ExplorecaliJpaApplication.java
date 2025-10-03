package edu.ensign.cs460;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.ensign.cs460.business.TourPackageService;
import edu.ensign.cs460.business.TourService;
import edu.ensign.cs460.business.TourRatingService;
import edu.ensign.cs460.recommendation.RecommendationService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@SpringBootApplication
public class ExplorecaliJpaApplication implements CommandLineRunner {
 
    @Bean
    public OpenAPI swaggerHeader() {
        return new OpenAPI()
            .info((new Info())
            .description("Services for the Explore California Relational Database.")
            .title(StringUtils.substringBefore(getClass().getSimpleName(), "$"))
            .version("3.0.0"));
    }
    

    @Autowired
    private TourPackageService tourPackageService;

    @Autowired
    private TourService tourService;

    @Autowired
    private TourRatingService tourRatingService;

    @Autowired
    private RecommendationService tourRecommendationService;

    public static void main(String[] args) {

        SpringApplication.run(ExplorecaliJpaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Persisted Packages = " + tourPackageService.total());
        System.out.println("Persisted Tours = " + tourService.total());
    }
}
