package edu.ensign.cs460.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ensign.cs460.model.Difficulty;
import edu.ensign.cs460.model.Tour;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tours", description = "The Tour API")
public interface TourRepository extends JpaRepository<Tour, Integer> {
  List<Tour> findByDifficulty(Difficulty diff);
  List<Tour> findByTourPackageCode(String code);
}