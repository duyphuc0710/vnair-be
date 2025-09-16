package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.AirplaneCreateRequest;
import com.vnair.air.dto.request.update.AirplaneUpdateRequest;
import com.vnair.air.dto.response.AirplaneResponse;

import java.util.List;

public interface AirplaneService {
    
    /**
     * Create new airplane
     */
    AirplaneResponse createAirplane(AirplaneCreateRequest request);
    
    /**
     * Update existing airplane
     */
    AirplaneResponse updateAirplane(Integer id, AirplaneUpdateRequest request);
    
    /**
     * Get airplane by ID
     */
    AirplaneResponse getAirplaneById(Integer id);
    
    /**
     * Get all airplanes with pagination
     */
    Page<AirplaneResponse> getAllAirplanes(Pageable pageable);
    
    /**
     * Get all airplanes without pagination
     */
    List<AirplaneResponse> getAllAirplanes();
    
    /**
     * Delete airplane by ID
     */
    void deleteAirplane(Integer id);
    
    /**
     * Find airplanes by model
     */
    Page<AirplaneResponse> findByModel(String model, Pageable pageable);
    
}