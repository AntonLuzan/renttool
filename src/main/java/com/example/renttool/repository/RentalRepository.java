package com.example.renttool.repository;

import com.example.renttool.model.Rental;
import com.example.renttool.model.Tool;
import com.example.renttool.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByToolAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndCancelledFalse(
            Tool tool,
            LocalDate endDate,
            LocalDate startDate
    );

    List<Rental> findByUserOrderByStartDateDesc(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Rental r SET r.cancelled = true WHERE r.id = :id")
    void cancelRental(@Param("id") Long id);

    @Query("""
        SELECT r
        FROM Rental r
        WHERE r.tool.id = :toolId
          AND r.endDate >= :today
          AND r.cancelled = false
        ORDER BY r.startDate
    """)
    List<Rental> findActiveRentalsByToolId(
            @Param("toolId") Long toolId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT r FROM Rental r
        WHERE r.endDate >= :today
          AND r.cancelled = false
    """)
    List<Rental> findActiveRentals(@Param("today") LocalDate today);

    @Query("""
        SELECT r FROM Rental r
        WHERE r.endDate < :today
           OR r.cancelled = true
        ORDER BY r.startDate DESC
    """)
    List<Rental> findPastRentals(@Param("today") LocalDate today);

    @Query("""
        SELECT r
        FROM Rental r
        JOIN FETCH r.user
        JOIN FETCH r.tool
        ORDER BY r.startDate DESC
    """)
    List<Rental> findAllWithDetails();
    @Query("""
    SELECT r
    FROM Rental r
    JOIN FETCH r.user
    JOIN FETCH r.tool
    WHERE r.cancelled = false
      AND r.endDate < :today
    ORDER BY r.endDate DESC
""")
    List<Rental> findFinishedRentals(@Param("today") LocalDate today);


    List<Rental> findByUserAndEndDateGreaterThanEqualAndCancelledFalseOrderByStartDateDesc(User user, LocalDate today);

    List<Rental> findByUserAndEndDateLessThanOrCancelledTrueOrderByStartDateDesc(User user, LocalDate today);

    List<Rental> findByToolAndEndDateGreaterThanEqual(Tool tool, LocalDate today);

    Collection<Object> findByToolAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Tool tool, LocalDate end, LocalDate start);
}
