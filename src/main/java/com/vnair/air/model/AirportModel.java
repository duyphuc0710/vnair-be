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
@Table(name = "airport")
public class AirportModel extends AbstractEntity<Integer> {
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;
}
