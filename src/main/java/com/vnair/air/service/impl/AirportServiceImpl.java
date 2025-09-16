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

        if (airportRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateAirportCodeException("Airport with code " + request.getCode() + " already exists");
        }

        AirportModel airport = new AirportModel();
        airport.setCode(request.getCode());
        airport.setName(request.getName());
        airport.setCity(request.getCity());
        airport.setCountry(request.getCountry());

        AirportModel savedAirport = airportRepository.save(airport);
        log.info("Successfully created airport with ID: {}", savedAirport.getId());

        return mapToResponse(savedAirport);
    }

    @Override
    public AirportResponse updateAirport(Integer id, AirportUpdateRequest request) {
        log.info("Updating airport with ID: {}", id);

        AirportModel airport = airportRepository.findById(id)
                .orElseThrow(() -> new AirportNotFoundException("Airport not found with ID: " + id));

        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(airport.getCode())) {
            if (airportRepository.existsByCodeIgnoreCase(request.getCode())) {
                throw new DuplicateAirportCodeException("Airport with code " + request.getCode() + " already exists");
            }
            airport.setCode(request.getCode());
        }

        if (request.getName() != null) {
            airport.setName(request.getName());
        }
        if (request.getCity() != null) {
            airport.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            airport.setCountry(request.getCountry());
        }

        AirportModel savedAirport = airportRepository.save(airport);
        log.info("Successfully updated airport with ID: {}", savedAirport.getId());

        return mapToResponse(savedAirport);
    }

    @Override
    @Transactional(readOnly = true)
    public AirportResponse getAirportById(Integer id) {
        log.info("Getting airport by ID: {}", id);

        AirportModel airport = airportRepository.findById(id)
                .orElseThrow(() -> new AirportNotFoundException("Airport not found with ID: " + id));

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

        if (!airportRepository.existsById(id)) {
            throw new AirportNotFoundException("Airport not found with ID: " + id);
        }

        airportRepository.deleteById(id);
        log.info("Successfully deleted airport with ID: {}", id);
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