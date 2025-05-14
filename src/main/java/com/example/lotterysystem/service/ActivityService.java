package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;

public interface ActivityService {

    CreateActivityDTO createActivity(CreateActivityParam param);

    PageListDTO<ActivityDTO> findActivityList(PageParam param);

    ActivityDetailDTO getActivityDetail(Long activityId);

    void cacheActivity(Long activityId);
}
