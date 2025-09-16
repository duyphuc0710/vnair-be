package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vnair.air.model.AirplaneModel;

import java.util.List;

@Repository
public interface AirplaneRepository extends JpaRepository<AirplaneModel, Integer> {
    
    /**
     * Find airplanes by airline
     */
    Page<AirplaneModel> findByAirlineIgnoreCaseContaining(String airline, Pageable pageable);
    
    /**
     * Find airplanes by model
     */
    Page<AirplaneModel> findByModelIgnoreCaseContaining(String model, Pageable pageable);
    
    /**
     * Find airplanes with seat capacity greater than or equal to specified value
     */
    List<AirplaneModel> findBySeatCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * Find airplanes by seat capacity range
     */
    Page<AirplaneModel> findBySeatCapacityBetween(Integer minCapacity, Integer maxCapacity, Pageable pageable);
}