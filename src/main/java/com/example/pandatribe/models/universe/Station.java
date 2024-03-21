package com.example.pandatribe.models.universe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Station {
    private Integer stationId;
    private String stationName;
}
