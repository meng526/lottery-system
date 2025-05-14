package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@Component
public class PictureServiceImpl implements PictureService {

    private static final Logger logger = LoggerFactory.getLogger(PictureServiceImpl.class);

    @Value("${pic.local-path}")
    private  String localPath;

    @Override
    public String savePicture(MultipartFile multipartFile) {

        File file=new File(localPath);
        if(!file.exists()){
            file.mkdirs();
        }

        String fileName = multipartFile.getOriginalFilename();
        assert fileName!=null;
        String subFile = fileName.substring(fileName.lastIndexOf("."));
        fileName = UUID.randomUUID()+subFile;
        logger.info("savePicture : fileName = {}   localPath = {}",fileName,localPath);
        try {
            multipartFile.transferTo(new File(localPath + "/" + fileName));
        } catch (IOException e) {
            throw new ServiceException(ServiceErrorCodeConstants.PIC_UPLOAD_ERROR);
        }
        return fileName;
    }



}
