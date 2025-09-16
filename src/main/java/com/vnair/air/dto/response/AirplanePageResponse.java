package com.vnair.air.dto.response;

import java.util.List;

import com.vnair.common.model.PageResponseAbstract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirplanePageResponse extends PageResponseAbstract {
    private List<AirplaneResponse> content;
}