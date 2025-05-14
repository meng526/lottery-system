package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PrizeService {

    Long createPrize(CreatePrizeParam param, MultipartFile multipartFile);

    PageListDTO<PrizeDTO> findPrizeList(PageParam param);
}
