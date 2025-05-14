package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.stream.Collectors;


@RestController
public class PrizeController  {

    @Autowired
    PictureService pictureService;

    @Autowired
    PrizeService prizeService;

    private static final Logger logger= LoggerFactory.getLogger(PrizeController.class);

    @RequestMapping("/pic/upload")
    public String uploadPic(MultipartFile file){
        logger.info("uploadPic filename = {}",file.getOriginalFilename());
        String fileName = pictureService.savePicture(file);
        return fileName;
    }

    @RequestMapping("/prize/create")
    public CommonResult<Long> createPrize(@Validated @RequestPart("param") CreatePrizeParam param,
                                          @RequestPart("prizePic") MultipartFile prizeFile){
        logger.info("createPrize: param = {}", JacksonUtil.writeValueAsString(param));
       return CommonResult.success(prizeService.createPrize(param,prizeFile));
    }
    @RequestMapping("/prize/find-list")
    public CommonResult<FindPrizeListResult> findPrizeList(PageParam param){
        logger.info("findPrizeList PageParam:{}",
                JacksonUtil.writeValueAsString(param));
        PageListDTO<PrizeDTO> pageListDTO = prizeService.findPrizeList(param);
        return CommonResult.success(convertToFindPrizeListResult(pageListDTO));
    }

    private FindPrizeListResult convertToFindPrizeListResult(PageListDTO<PrizeDTO> pageListDTO) {
        if(null==pageListDTO){
            throw new ControllerException(ControllerErrorCodeConstants.FIND_PRIZE_LIST_ERROR);
        }
        FindPrizeListResult result = new FindPrizeListResult();
        result.setTotal(pageListDTO.getTotal());
        result.setRecords(
                pageListDTO.getRecords().stream()
                .map(prizeDTO -> {
                    FindPrizeListResult.PrizeInfo prizeInfo = new FindPrizeListResult.PrizeInfo();
                    prizeInfo.setPrizeName(prizeDTO.getName());
                    prizeInfo.setDescription(prizeDTO.getDescription());
                    prizeInfo.setImageUrl(prizeDTO.getImageUrl());
                    prizeInfo.setPrice(prizeDTO.getPrice());
                    prizeInfo.setPrizeId(prizeDTO.getPrizeId());
                    return prizeInfo;
                }).collect(Collectors.toList()));

        return result;

    }
}
