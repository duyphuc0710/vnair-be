package com.vnair.air.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.vnair.common.model.AbstractEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "airplane")
public class AirplaneModel extends AbstractEntity<Integer> {
    @Column(name = "model")
    private String model;

    @Column(name = "seat_capacity")
    private Integer seatCapacity;

    @Column(name = "airline")
    private String airline;
}
