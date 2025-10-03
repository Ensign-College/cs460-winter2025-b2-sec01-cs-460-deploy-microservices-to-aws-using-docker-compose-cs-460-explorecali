package edu.ensign.cs460.repo;

import edu.ensign.cs460.model.TourRating;
import edu.ensign.cs460.recommendation.TourSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
/**
 * Tour Rating Repository Interface.
 *
 * Created by Mary Ellen Bowman
 */
@RepositoryRestResource(exported = false)
public interface TourRatingRepository extends JpaRepository<TourRating, Integer> {

  /**
   * Lookup all the TourRatings for a tour.
   *
   * @param tourId the tour identifier
   * @return a list of TourRatings
   */
  List<TourRating> findByTourId(Integer tourId);

  /**
   * Lookup a TourRating by the TourId and Customer Id.
   *
   * @param tourId the tour identifier
   * @param customerId the customer identifier
   * @return TourRating if found, empty otherwise
   */
  Optional<TourRating> findByTourIdAndCustomerId(Integer tourId, Integer customerId);

  /**
   * Return the top tours aggregated as {@link TourSummary}.
   * Sorted by avgScore desc, then reviewCount desc, then title asc.
   *
   * @param pageable pass a PageRequest with the desired limit (e.g., PageRequest.of(0, limit))
   * @return list of top tours
   */
  @Query("""
         select tr.tour.id   as tourId,
                tr.tour.title as title,
                avg(tr.score) as avgScore,
                count(tr.id)  as reviewCount
         from TourRating tr
         group by tr.tour.id, tr.tour.title
         order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
         """)
  List<TourSummary> findTopTours(Pageable pageable);

  /**
   * Recommend tours to a customer by excluding tours they've already rated.
   * Sorted by avgScore desc, then reviewCount desc, then title asc.
   *
   * @param customerId the customer identifier
   * @param pageable pass a PageRequest with the desired limit (e.g., PageRequest.of(0, limit))
   * @return list of recommended tours
   */
  @Query("""
         select tr.tour.id   as tourId,
                tr.tour.title as title,
                avg(tr.score) as avgScore,
                count(tr.id)  as reviewCount
         from TourRating tr
         where tr.tour.id not in (
             select r.tour.id from TourRating r where r.customerId = :customerId
         )
         group by tr.tour.id, tr.tour.title
         order by avg(tr.score) desc, count(tr.id) desc, tr.tour.title asc
         """)
  List<TourSummary> findRecommendedForCustomer(
      @Param("customerId") int customerId,
      Pageable pageable
  );
}