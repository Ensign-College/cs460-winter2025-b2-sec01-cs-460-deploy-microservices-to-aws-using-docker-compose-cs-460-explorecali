package com.example.explorecalijpa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SecurityEnforcementFilter extends OncePerRequestFilter {

  private boolean isPublicPath(String path) {
    return path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
        || path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    // Allow public paths through
    if (isPublicPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Enforce rules for /packages and /tours
    if (path.startsWith("/packages") || path.startsWith("/tours")) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean authenticated = auth != null && auth.isAuthenticated() && auth.getPrincipal() != null
          && !"anonymousUser".equals(auth.getPrincipal());

      if ("GET".equalsIgnoreCase(method)) {
        if (!authenticated) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
      } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
          || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
        if (!authenticated) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
