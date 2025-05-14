package com.example.lotterysystem.service.activityStatus.operater;

import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

public abstract class AbstractActivityOperator {
    public abstract Integer sequence();
    public abstract Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO);
    public abstract Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO);
}
