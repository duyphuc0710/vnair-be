package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.enums.FlightStatus;
import com.vnair.air.model.FlightModel;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<FlightModel, Long> {
    
    /**
     * Search flights by departure airport, arrival airport and departure date
     */
    @Query("SELECT f FROM FlightModel f WHERE f.departureAirport.id = :departureAirportId " +
           "AND f.arrivalAirport.id = :arrivalAirportId " +
           "AND DATE(f.departureTime) = :departureDate " +
           "AND f.status = :status")
    List<FlightModel> searchFlights(@Param("departureAirportId") Integer departureAirportId,
                                    @Param("arrivalAirportId") Integer arrivalAirportId,
                                    @Param("departureDate") LocalDate departureDate,
                                    @Param("status") FlightStatus status);
    
    /**
     * Find flights by airplane
     */
    Page<FlightModel> findByAirplaneId(Integer airplaneId, Pageable pageable);
    
    /**
     * Find flights by departure airport
     */
    Page<FlightModel> findByDepartureAirportId(Integer departureAirportId, Pageable pageable);
    
    /**
     * Find flights by arrival airport
     */
    Page<FlightModel> findByArrivalAirportId(Integer arrivalAirportId, Pageable pageable);
    
    /**
     * Find flights by status
     */
    Page<FlightModel> findByStatus(FlightStatus status, Pageable pageable);
    
    /**
     * Find flights departing between dates
     */
    @Query("SELECT f FROM FlightModel f WHERE f.departureTime BETWEEN :startDate AND :endDate")
    Page<FlightModel> findByDepartureTimeBetween(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 Pageable pageable);
    
    /**
     * Find upcoming flights (departure time > current time)
     */
    @Query("SELECT f FROM FlightModel f WHERE f.departureTime > CURRENT_TIMESTAMP ORDER BY f.departureTime")
    Page<FlightModel> findUpcomingFlights(Pageable pageable);
    
    /**
     * Find overlapping flights for an airplane within a time period
     */
    @Query("SELECT f FROM FlightModel f WHERE f.airplane.id = :airplaneId " +
           "AND ((f.departureTime <= :endTime AND f.arrivalTime >= :startTime))")
    List<FlightModel> findOverlappingFlights(@Param("airplaneId") Integer airplaneId,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);
}