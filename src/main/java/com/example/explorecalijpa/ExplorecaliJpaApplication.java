package com.example.explorecalijpa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.explorecalijpa.business.TourPackageService;
import com.example.explorecalijpa.business.TourService;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// 👇 Add scanBasePackages here to include both your current package and the new one
@SpringBootApplication(scanBasePackages = {
        "com.example.explorecalijpa",
        "edu.ensign.cs460"
})
public class ExplorecaliJpaApplication implements CommandLineRunner {

    @Bean
    public OpenAPI swaggerHeader() {
        return new OpenAPI()
                .info(new Info()
                        .description("Services for the Explore California Relational Database.")
                        .title(StringUtils.substringBefore(getClass().getSimpleName(), "$")) 
                        .version("3.0.0"));
    }

    @Autowired
    private TourPackageService tourPackageService;

    @Autowired
    private TourService tourService;

    public static void main(String[] args) {
        SpringApplication.run(ExplorecaliJpaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Persisted Packages = " + tourPackageService.total());
        System.out.println("Persisted Tours = " + tourService.total());
    }
}
