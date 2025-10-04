package com.example.explorecalijpa.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.example.explorecalijpa.model.TourRating;
import edu.ensign.cs460.recommendation.TourSummary;

/**
 * Tour Rating Repository Interface
 *
 * Created by Mary Ellen Bowman
 */
@RepositoryRestResource(exported = false)
public interface TourRatingRepository extends JpaRepository<TourRating, Integer>, CrudRepository<TourRating, Integer> {

  /**
   * Lookup all the TourRatings for a tour.
   *
   * @param tourId is the tour Identifier
   * @return a List of any found TourRatings
   */
  List<TourRating> findByTourId(Integer tourId);

  /**
   * Lookup a TourRating by the TourId and Customer Id
   * 
   * @param tourId
   * @param customerId
   * @return TourRating if found, null otherwise.
   */
  Optional<TourRating> findByTourIdAndCustomerId(Integer tourId, Integer customerId);

  /**
   * Find top-rated tours across the entire system.
   * Calculates average rating and review count for each tour.
   * Results are ordered by average score (desc), review count (desc), and title
   * (asc).
   *
   * @param pageable pagination parameters (use PageRequest.of(0, limit))
   * @return list of tour summaries with rating statistics
   */
  @Query("""
         select tr.tour.id as tourId,
                tr.tour.title as title,
                avg(tr.score)  as avgScore,
                count(tr.id)   as reviewCount
         from TourRating tr
         group by tr.tour.id, tr.tour.title
         order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
      """)
  List<TourSummary> findTopTours(Pageable pageable);

  /**
   * Find recommended tours for a specific customer.
   * Returns tours not yet rated by the customer, ordered by average rating.
   *
   * @param customerId the customer ID
   * @param pageable   pagination parameters
   * @return list of tour summaries excluding tours already rated by this customer
   */
  @Query("""
         select tr.tour.id as tourId,
                tr.tour.title as title,
                avg(tr.score)  as avgScore,
                count(tr.id)   as reviewCount
         from TourRating tr
         where tr.tour.id not in (
             select r.tour.id from TourRating r where r.customerId = :customerId
         )
         group by tr.tour.id, tr.tour.title
         order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
      """)
  List<TourSummary> findRecommendedForCustomer(int customerId, Pageable pageable);
}
