package edu.ensign.cs460.business;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.ensign.cs460.model.Difficulty;
import edu.ensign.cs460.model.Region;
import edu.ensign.cs460.model.Tour;
import edu.ensign.cs460.model.TourPackage;
import edu.ensign.cs460.repo.TourPackageRepository;
import edu.ensign.cs460.repo.TourRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class TourService {
  private TourPackageRepository tourPackageRepository;
  private TourRepository tourRepository;

  public TourService(TourPackageRepository tourPackageRepository, TourRepository tourRepository) {
    this.tourPackageRepository = tourPackageRepository;
    this.tourRepository = tourRepository;
  }

  public Tour createTour(String tourPackageName, String title,
      String description, String blurb, Integer price, String duration,
      String bullets, String keywords, Difficulty difficulty, Region region) {
    log.info("Create tour {} for package {}", title, tourPackageName);
    TourPackage tourPackage = tourPackageRepository.findByName(tourPackageName)
        .orElseThrow(() -> new RuntimeException("Tour Package not found for id:" + tourPackageName));
    return tourRepository.save(new Tour(title, description, blurb,
        price, duration, bullets, keywords, tourPackage, difficulty, region));
  }

  public List<Tour> lookupByDifficulty(Difficulty difficulty) {
    log.info("Lookup tours by difficulty {}", difficulty);
    return tourRepository.findByDifficulty(difficulty);
  }

  public List<Tour> lookupByPackage(String tourPackageCode) {
    log.info("Lookup tour by code {}", tourPackageCode);
    return tourRepository.findByTourPackageCode(tourPackageCode);
  }

  public long total() {
    log.info("Get total tours");
    return tourRepository.count();
  }
}
