package edu.ensign.cs460;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;   // <— add this
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@EnableCaching  // <— enables Spring Cache
@SpringBootApplication
public class ExplorecaliJpaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExplorecaliJpaApplication.class, args);
  }

  @Bean
  public OpenAPI swaggerHeader() {
    return new OpenAPI()
        .info(new Info()
            .description("Services for the Explore California Relational Database.")
            .title(StringUtils.substringBefore(ExplorecaliJpaApplication.class.getSimpleName(), "$"))
            .version("3.0.0"));
  }
}
