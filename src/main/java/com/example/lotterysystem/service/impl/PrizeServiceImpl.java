package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.PrizeMapper;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrizeServiceImpl implements PrizeService {
    private static final Logger logger = LoggerFactory.getLogger(PrizeServiceImpl.class);

    @Autowired
    PictureService pictureService;

    @Autowired
    private PrizeMapper prizeMapper;

    @Override
    public Long createPrize(CreatePrizeParam param, MultipartFile multipartFile) {
        String fileName = pictureService.savePicture(multipartFile);
        PrizeDO prizeDO = new PrizeDO();
        prizeDO.setName(param.getPrizeName());
        prizeDO.setDescription(param.getDescription());
        prizeDO.setImageUrl(fileName);
        prizeDO.setPrice(param.getPrice());
        prizeMapper.insertPrize(prizeDO);
        return prizeDO.getId();
    }

    @Override
    public PageListDTO<PrizeDTO> findPrizeList(PageParam param) {
        Integer total = prizeMapper.count();
        List<PrizeDTO> prizeDTOList = new ArrayList<>();
        List<PrizeDO> prizeDOList = prizeMapper.selectPrizeList(param.offset(),param.getPageSize());

        for(PrizeDO prizeDO : prizeDOList){
            PrizeDTO prizeDTO = new PrizeDTO();
            prizeDTO.setName(prizeDO.getName());
            prizeDTO.setDescription(prizeDO.getDescription());
            prizeDTO.setPrice(prizeDO.getPrice());
            prizeDTO.setImageUrl(prizeDO.getImageUrl());
            prizeDTO.setPrizeId(prizeDO.getId());
            prizeDTOList.add(prizeDTO);
        }
        return new PageListDTO<>(total,prizeDTOList);


    }

}
