package com.vnair.air.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.air.dto.request.create.AirplaneCreateRequest;
import com.vnair.air.dto.request.update.AirplaneUpdateRequest;
import com.vnair.air.dto.response.AirplaneResponse;
import com.vnair.air.exception.AirplaneNotFoundException;
import com.vnair.air.model.AirplaneModel;
import com.vnair.air.repository.AirplaneRepository;
import com.vnair.air.service.AirplaneService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AirplaneServiceImpl implements AirplaneService {

    private final AirplaneRepository airplaneRepository;

    // ================ PUBLIC SERVICE METHODS ================

    @Override
    public AirplaneResponse createAirplane(AirplaneCreateRequest request) {
        logOperationStart("Creating airplane with model: {}", request.getModel());
        
        AirplaneModel airplane = buildAirplaneFromRequest(request);
        AirplaneModel savedAirplane = airplaneRepository.save(airplane);
        
        logOperationSuccess("Successfully created airplane with ID: {}", savedAirplane.getId());
        return mapToResponse(savedAirplane);
    }

    @Override
    public AirplaneResponse updateAirplane(Integer id, AirplaneUpdateRequest request) {
        logOperationStart("Updating airplane with ID: {}", id);
        
        AirplaneModel airplane = findAirplaneByIdOrThrow(id);
        updateAirplaneFields(airplane, request);
        AirplaneModel savedAirplane = airplaneRepository.save(airplane);
        
        logOperationSuccess("Successfully updated airplane with ID: {}", savedAirplane.getId());
        return mapToResponse(savedAirplane);
    }

    @Override
    @Transactional(readOnly = true)
    public AirplaneResponse getAirplaneById(Integer id) {
        logOperationStart("Getting airplane by ID: {}", id);
        
        AirplaneModel airplane = findAirplaneByIdOrThrow(id);
        return mapToResponse(airplane);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirplaneResponse> getAllAirplanes(Pageable pageable) {
        logOperationStart("Getting all airplanes with pagination");
        
        Page<AirplaneModel> airplanes = airplaneRepository.findAll(pageable);
        return mapPageToResponse(airplanes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirplaneResponse> getAllAirplanes() {
        logOperationStart("Getting all airplanes without pagination");
        
        List<AirplaneModel> airplanes = airplaneRepository.findAll();
        return mapListToResponse(airplanes);
    }

    @Override
    public void deleteAirplane(Integer id) {
        logOperationStart("Deleting airplane with ID: {}", id);
        
        validateAirplaneExists(id);
        airplaneRepository.deleteById(id);
        
        logOperationSuccess("Successfully deleted airplane with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirplaneResponse> findByModel(String model, Pageable pageable) {
        logOperationStart("Finding airplanes by model: {}", model);
        
        Page<AirplaneModel> airplanes = airplaneRepository.findByModelIgnoreCaseContaining(model, pageable);
        return mapPageToResponse(airplanes);
    }

    // ================ PRIVATE UTILITY METHODS ================

    /**
     * Validates that an airplane exists by ID, throws exception if not found
     * Used for operations that require existing airplane validation
     */
    private void validateAirplaneExists(Integer id) {
        if (!airplaneRepository.existsById(id)) {
            throw new AirplaneNotFoundException("Airplane not found with ID: " + id);
        }
    }

    /**
     * Finds airplane by ID or throws AirplaneNotFoundException
     * Centralized method to avoid code duplication across service methods
     */
    private AirplaneModel findAirplaneByIdOrThrow(Integer id) {
        return airplaneRepository.findById(id)
                .orElseThrow(() -> new AirplaneNotFoundException("Airplane not found with ID: " + id));
    }

    /**
     * Builds new AirplaneModel from create request
     * Separates object creation logic following Single Responsibility principle
     */
    private AirplaneModel buildAirplaneFromRequest(AirplaneCreateRequest request) {
        AirplaneModel airplane = new AirplaneModel();
        airplane.setModel(request.getModel());
        airplane.setSeatCapacity(request.getSeatCapacity());
        airplane.setAirline(request.getAirline());
        return airplane;
    }

    /**
     * Updates airplane fields from update request
     * Handles selective field updates, only updates non-null values
     */
    private void updateAirplaneFields(AirplaneModel airplane, AirplaneUpdateRequest request) {
        if (request.getModel() != null) {
            airplane.setModel(request.getModel());
        }
        if (request.getAirline() != null) {
            airplane.setAirline(request.getAirline());
        }
        if (request.getSeatCapacity() != null) {
            airplane.setSeatCapacity(request.getSeatCapacity());
        }
    }

    /**
     * Maps single AirplaneModel to AirplaneResponse
     * Core mapping logic used across all service methods
     */
    private AirplaneResponse mapToResponse(AirplaneModel airplane) {
        return AirplaneResponse.builder()
                .model(airplane.getModel())
                .seatCapacity(airplane.getSeatCapacity())
                .airline(airplane.getAirline())
                .createdAt(airplane.getCreatedAt())
                .updatedAt(airplane.getUpdatedAt())
                .build();
    }

    /**
     * Maps Page<AirplaneModel> to Page<AirplaneResponse>
     * Reusable mapping for paginated results
     */
    private Page<AirplaneResponse> mapPageToResponse(Page<AirplaneModel> airplanes) {
        return airplanes.map(this::mapToResponse);
    }

    /**
     * Maps List<AirplaneModel> to List<AirplaneResponse>
     * Reusable mapping for list results
     */
    private List<AirplaneResponse> mapListToResponse(List<AirplaneModel> airplanes) {
        return airplanes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Logs operation start with consistent format
     * Centralized logging for operation initiation
     */
    private void logOperationStart(String message, Object... args) {
        log.info(message, args);
    }

    /**
     * Logs operation success with consistent format
     * Centralized logging for successful operations
     */
    private void logOperationSuccess(String message, Object... args) {
        log.info(message, args);
    }
}