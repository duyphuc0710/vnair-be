package com.vnair.air.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.air.dto.request.create.TicketTypeCreateRequest;
import com.vnair.air.dto.request.update.TicketTypeUpdateRequest;
import com.vnair.air.dto.response.TicketTypeResponse;
import com.vnair.air.exception.TicketTypeNotFoundException;
import com.vnair.air.model.TicketTypeModel;
import com.vnair.air.repository.TicketTypeRepository;
import com.vnair.air.service.TicketTypeService;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public TicketTypeResponse createTicketType(TicketTypeCreateRequest request) {
        log.info("Creating ticket type: {}", request.getName());
        
        TicketTypeModel ticketType = new TicketTypeModel();
        ticketType.setName(request.getName());
        ticketType.setPriceMultiplier(request.getPriceMultiplier());
        
        TicketTypeModel savedTicketType = ticketTypeRepository.save(ticketType);
        log.info("Successfully created ticket type with ID: {}", savedTicketType.getId());
        
        return mapToResponse(savedTicketType);
    }

    @Override
    public TicketTypeResponse updateTicketType(Integer id, TicketTypeUpdateRequest request) {
        log.info("Updating ticket type with ID: {}", id);
        
        TicketTypeModel ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found with ID: " + id));
        
        if (request.getName() != null) {
            ticketType.setName(request.getName());
        }
        if (request.getPriceMultiplier() != null) {
            ticketType.setPriceMultiplier(request.getPriceMultiplier());
        }
        
        TicketTypeModel savedTicketType = ticketTypeRepository.save(ticketType);
        log.info("Successfully updated ticket type with ID: {}", savedTicketType.getId());
        
        return mapToResponse(savedTicketType);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketTypeById(Integer id) {
        log.info("Getting ticket type by ID: {}", id);
        
        TicketTypeModel ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found with ID: " + id));
        
        return mapToResponse(ticketType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketTypeResponse> getAllTicketTypes(Pageable pageable) {
        log.info("Getting all ticket types with pagination");
        
        Page<TicketTypeModel> ticketTypes = ticketTypeRepository.findAll(pageable);
        return ticketTypes.map(this::mapToResponse);
    }

    @Override
    public void deleteTicketType(Integer id) {
        log.info("Deleting ticket type with ID: {}", id);
        
        if (!ticketTypeRepository.existsById(id)) {
            throw new TicketTypeNotFoundException("TicketType not found with ID: " + id);
        }
        
        ticketTypeRepository.deleteById(id);
        log.info("Successfully deleted ticket type with ID: {}", id);
    }

    @Override
    public TicketTypeResponse updateAvailableTickets(Integer id, Integer newCount) {
        log.warn("updateAvailableTickets not implemented - returning current ticket type");
        return getTicketTypeById(id);
    }

    @Override
    public TicketTypeResponse releaseTickets(Integer id, Integer quantity) {
        log.warn("releaseTickets not implemented - returning current ticket type");
        return getTicketTypeById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalAvailableTicketsByFlight(Long flightId) {
        log.warn("getTotalAvailableTicketsByFlight not implemented - returning 0");
        return 0;
    }

    private TicketTypeResponse mapToResponse(TicketTypeModel ticketType) {
        return TicketTypeResponse.builder()
                .name(ticketType.getName())
                .priceMultiplier(ticketType.getPriceMultiplier())
                .createdAt(ticketType.getCreatedAt())
                .updatedAt(ticketType.getUpdatedAt())
                .build();
    }
}