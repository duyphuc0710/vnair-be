package com.vnair.air.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.vnair.common.model.AbstractEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ticket_type")
public class TicketTypeModel extends AbstractEntity<Integer> {
    @Column(name = "name")
    private String name;

    @Column(name = "price_multiplier")
    private BigDecimal priceMultiplier;
}
