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

    @Override
    public AirplaneResponse createAirplane(AirplaneCreateRequest request) {
        log.info("Creating airplane with model: {}", request.getModel());
        
        AirplaneModel airplane = new AirplaneModel();
        airplane.setModel(request.getModel());
        airplane.setSeatCapacity(request.getSeatCapacity());
        airplane.setAirline(request.getAirline());
        
        AirplaneModel savedAirplane = airplaneRepository.save(airplane);
        log.info("Successfully created airplane with ID: {}", savedAirplane.getId());
        
        return mapToResponse(savedAirplane);
    }

    @Override
    public AirplaneResponse updateAirplane(Integer id, AirplaneUpdateRequest request) {
        log.info("Updating airplane with ID: {}", id);
        
        AirplaneModel airplane = airplaneRepository.findById(id)
                .orElseThrow(() -> new AirplaneNotFoundException("Airplane not found with ID: " + id));
        
        if (request.getModel() != null) {
            airplane.setModel(request.getModel());
        }
        if (request.getAirline() != null) {
            airplane.setAirline(request.getAirline());
        }
        if (request.getSeatCapacity() != null) {
            airplane.setSeatCapacity(request.getSeatCapacity());
        }
        
        AirplaneModel savedAirplane = airplaneRepository.save(airplane);
        log.info("Successfully updated airplane with ID: {}", savedAirplane.getId());
        
        return mapToResponse(savedAirplane);
    }

    @Override
    @Transactional(readOnly = true)
    public AirplaneResponse getAirplaneById(Integer id) {
        log.info("Getting airplane by ID: {}", id);
        
        AirplaneModel airplane = airplaneRepository.findById(id)
                .orElseThrow(() -> new AirplaneNotFoundException("Airplane not found with ID: " + id));
        
        return mapToResponse(airplane);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirplaneResponse> getAllAirplanes(Pageable pageable) {
        log.info("Getting all airplanes with pagination");
        
        Page<AirplaneModel> airplanes = airplaneRepository.findAll(pageable);
        return airplanes.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirplaneResponse> getAllAirplanes() {
        log.info("Getting all airplanes without pagination");
        
        List<AirplaneModel> airplanes = airplaneRepository.findAll();
        return airplanes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAirplane(Integer id) {
        log.info("Deleting airplane with ID: {}", id);
        
        if (!airplaneRepository.existsById(id)) {
            throw new AirplaneNotFoundException("Airplane not found with ID: " + id);
        }
        
        airplaneRepository.deleteById(id);
        log.info("Successfully deleted airplane with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirplaneResponse> findByModel(String model, Pageable pageable) {
        log.info("Finding airplanes by model: {}", model);
        
        Page<AirplaneModel> airplanes = airplaneRepository.findByModelIgnoreCaseContaining(model, pageable);
        return airplanes.map(this::mapToResponse);
    }

    private AirplaneResponse mapToResponse(AirplaneModel airplane) {
        return AirplaneResponse.builder()
                .model(airplane.getModel())
                .seatCapacity(airplane.getSeatCapacity())
                .airline(airplane.getAirline())
                .createdAt(airplane.getCreatedAt())
                .updatedAt(airplane.getUpdatedAt())
                .build();
    }
}