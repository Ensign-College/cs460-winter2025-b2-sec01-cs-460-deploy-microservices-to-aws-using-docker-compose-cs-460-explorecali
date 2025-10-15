package com.example.explorecalijpa.config;

import com.example.explorecalijpa.model.TourPackage;
import com.example.explorecalijpa.model.Tour;
import com.example.explorecalijpa.model.TourRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler({ TourPackage.class, Tour.class, TourRating.class })
public class RepositorySecurityEventHandler {

  private static final Logger log = LoggerFactory.getLogger(RepositorySecurityEventHandler.class);

  private void requireAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    log.debug("Repository event auth: {}", auth);
    if (auth == null || !auth.isAuthenticated()) {
      log.warn("Unauthenticated access to repository write attempted");
      throw new AccessDeniedException("Authentication required");
    }
    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin) {
      log.warn("User '{}' attempted repository write without ADMIN role", auth.getName());
      throw new AccessDeniedException("Admin role required");
    }
  }

  @HandleBeforeCreate
  public void beforeCreate(Object entity) {
    log.debug("beforeCreate: {}", entity.getClass().getSimpleName());
    requireAdmin();
  }

  @HandleBeforeSave
  public void beforeSave(Object entity) {
    log.debug("beforeSave: {}", entity.getClass().getSimpleName());
    requireAdmin();
  }

  @HandleBeforeDelete
  public void beforeDelete(Object entity) {
    log.debug("beforeDelete: {}", entity.getClass().getSimpleName());
    requireAdmin();
  }
}
