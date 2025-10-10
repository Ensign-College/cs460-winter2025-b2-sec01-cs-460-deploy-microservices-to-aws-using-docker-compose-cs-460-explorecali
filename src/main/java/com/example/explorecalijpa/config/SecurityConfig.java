// src/main/java/com/example/explorecalijpa/config/SecurityConfig.java
package com.example.explorecalijpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    var user = User.withUsername("user").password(encoder.encode("password")).roles("USER").build();
    var admin = User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health", "/actuator/info").permitAll()

        // READ endpoints: authenticated (USER or ADMIN)
        .requestMatchers(HttpMethod.GET,
            "/tours/**",
            "/packages/**",
            "/recommendations/**"      // add this so your recs GETs are 200 for user/admin, 401 for none
        ).hasAnyRole("USER","ADMIN")

        // Mutations require ADMIN
        .requestMatchers(HttpMethod.POST,   "/tours/**", "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT,    "/tours/**", "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH,  "/tours/**", "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/tours/**", "/packages/**").hasRole("ADMIN")

        // Anything else: authenticated
        .anyRequest().authenticated()
      )
      .httpBasic(Customizer.withDefaults())
      .formLogin(form -> form.disable());

    return http.build();
  }
}
