package com.example.explorecalijpa.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;


@Configuration
@EnableMethodSecurity // optional (handy for future method-level rules)
public class SecurityConfig {


  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }


  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.withUsername("user")
        .password(encoder.encode("password"))
        .roles("USER")             // becomes ROLE_USER
        .build();


    UserDetails admin = User.withUsername("admin")
        .password(encoder.encode("admin123"))
        .roles("ADMIN")            // becomes ROLE_ADMIN
        .build();


    return new InMemoryUserDetailsManager(user, admin);
  }


  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // Stateless REST: no CSRF tokens required for curl/Postman
      .csrf(csrf -> csrf.disable())


      // Authorization rules
      .authorizeHttpRequests(auth -> auth
          // Public docs/health
          .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                           "/actuator/health", "/actuator/info").permitAll()


          // Read access for authenticated users
          .requestMatchers(HttpMethod.GET, "/tours/**", "/packages/**")
              .hasAnyRole("USER", "ADMIN")


          // Mutations require ADMIN
          .requestMatchers(HttpMethod.POST, "/tours/**", "/packages/**").hasRole("ADMIN")
          .requestMatchers(HttpMethod.PUT,  "/tours/**", "/packages/**").hasRole("ADMIN")
          .requestMatchers(HttpMethod.PATCH,"/tours/**", "/packages/**").hasRole("ADMIN")
          .requestMatchers(HttpMethod.DELETE,"/tours/**", "/packages/**").hasRole("ADMIN")


          // Anything else must be authenticated
          .anyRequest().authenticated()
      )


      // HTTP Basic (no login form)
      .httpBasic(Customizer.withDefaults())
      .formLogin(form -> form.disable());


    return http.build();
  }
}
