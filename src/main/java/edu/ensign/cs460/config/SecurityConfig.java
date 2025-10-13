package edu.ensign.cs460.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    var user  = User.withUsername("user").password(encoder.encode("password")).roles("USER").build();
    var admin = User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .httpBasic(Customizer.withDefaults())
      .formLogin(form -> form.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health", "/actuator/info").permitAll()

        // PACKAGES
        .requestMatchers(HttpMethod.GET, "/packages/**").hasAnyRole("USER","ADMIN")
        .requestMatchers(HttpMethod.POST, "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/packages/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/packages/**").hasRole("ADMIN")

        // TOURS / RATINGS
        .requestMatchers(HttpMethod.GET, "/tours/**").hasAnyRole("USER","ADMIN")
        .requestMatchers(HttpMethod.POST, "/tours/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/tours/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/tours/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/tours/**").hasRole("ADMIN")

        // Recommendations GETs for authenticated users
        .requestMatchers(HttpMethod.GET, "/recommendations/**").hasAnyRole("USER","ADMIN")

        .anyRequest().authenticated()
      );
    return http.build();
  }
}
