package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.AirportCreateRequest;
import com.vnair.air.dto.request.update.AirportUpdateRequest;
import com.vnair.air.dto.response.AirportResponse;

public interface AirportService {
    
    /**
     * Create new airport
     */
    AirportResponse createAirport(AirportCreateRequest request);
    
    /**
     * Update existing airport
     */
    AirportResponse updateAirport(Integer id, AirportUpdateRequest request);
    
    /**
     * Get airport by ID
     */
    AirportResponse getAirportById(Integer id);
    
    /**
     * Get all airports with pagination
     */
    Page<AirportResponse> getAllAirports(Pageable pageable);
    
    /**
     * Delete airport by ID
     */
    void deleteAirport(Integer id);
}