package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vnair.air.model.AirportModel;

import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<AirportModel, Integer> {
    
    /**
     * Find airport by code (case insensitive)
     */
    Optional<AirportModel> findByCodeIgnoreCase(String code);
    
    /**
     * Check if airport exists by code
     */
    boolean existsByCodeIgnoreCase(String code);
    
    /**
     * Find airports by city (case insensitive)
     */
    Page<AirportModel> findByCityIgnoreCaseContaining(String city, Pageable pageable);
    
    /**
     * Find airports by country (case insensitive)
     */
    Page<AirportModel> findByCountryIgnoreCaseContaining(String country, Pageable pageable);
}