package com.vnair.air.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.air.dto.request.create.AirportCreateRequest;
import com.vnair.air.dto.request.update.AirportUpdateRequest;
import com.vnair.air.dto.response.AirportResponse;
import com.vnair.air.exception.AirportNotFoundException;
import com.vnair.air.exception.DuplicateAirportCodeException;
import com.vnair.air.model.AirportModel;
import com.vnair.air.repository.AirportRepository;
import com.vnair.air.service.AirportService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;

    @Override
    public AirportResponse createAirport(AirportCreateRequest request) {
        log.info("Creating airport with code: {}", request.getCode());

        validateAirportCodeUnique(request.getCode());
        
        AirportModel airport = createAirportEntity(request);
        AirportModel savedAirport = airportRepository.save(airport);
        
        log.info("Successfully created airport with ID: {}", savedAirport.getId());
        return mapToResponse(savedAirport);
    }

    @Override
    public AirportResponse updateAirport(Integer id, AirportUpdateRequest request) {
        log.info("Updating airport with ID: {}", id);

        AirportModel airport = validateAndFetchAirport(id);
        
        if (request.getCode() != null) {
            validateAndUpdateAirportCode(airport, request.getCode());
        }
        updateAirportFields(airport, request);

        AirportModel savedAirport = airportRepository.save(airport);
        log.info("Successfully updated airport with ID: {}", savedAirport.getId());

        return mapToResponse(savedAirport);
    }

    @Override
    @Transactional(readOnly = true)
    public AirportResponse getAirportById(Integer id) {
        log.info("Getting airport by ID: {}", id);

        AirportModel airport = validateAndFetchAirport(id);
        return mapToResponse(airport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirportResponse> getAllAirports(Pageable pageable) {
        log.info("Getting all airports with pagination");

        Page<AirportModel> airports = airportRepository.findAll(pageable);
        return airports.map(this::mapToResponse);
    }

    @Override
    public void deleteAirport(Integer id) {
        log.info("Deleting airport with ID: {}", id);

        AirportModel airport = validateAndFetchAirport(id);
        airportRepository.delete(airport);
        log.info("Successfully deleted airport with ID: {}", id);
    }

    // =================== Helper Methods ===================

    /**
     * Helper methods for validation, entity creation/update, and data conversion
     */

    private void validateAirportCodeUnique(String code) {
        if (airportRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateAirportCodeException("Airport code already exists: " + code);
        }
    }

    private AirportModel createAirportEntity(AirportCreateRequest request) {
        AirportModel airport = new AirportModel();
        airport.setCode(request.getCode().toUpperCase().trim());
        airport.setName(request.getName().trim());
        airport.setCity(request.getCity().trim());
        airport.setCountry(request.getCountry().trim());
        return airport;
    }

    private AirportModel validateAndFetchAirport(Integer id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new AirportNotFoundException("Airport not found with ID: " + id));
    }

    private void validateAndUpdateAirportCode(AirportModel airport, String newCode) {
        String trimmedCode = newCode.toUpperCase().trim();
        if (!airport.getCode().equals(trimmedCode)) {
            validateAirportCodeUnique(trimmedCode);
            airport.setCode(trimmedCode);
        }
    }

    private void updateAirportFields(AirportModel airport, AirportUpdateRequest request) {
        if (request.getName() != null) {
            airport.setName(request.getName().trim());
        }
        if (request.getCity() != null) {
            airport.setCity(request.getCity().trim());
        }
        if (request.getCountry() != null) {
            airport.setCountry(request.getCountry().trim());
        }
    }

    private AirportResponse mapToResponse(AirportModel airport) {
        return AirportResponse.builder()
                .code(airport.getCode())
                .name(airport.getName())
                .city(airport.getCity())
                .country(airport.getCountry())
                .createdAt(airport.getCreatedAt())
                .updatedAt(airport.getUpdatedAt())
                .build();
    }
}