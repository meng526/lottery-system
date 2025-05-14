package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageParam;
import com.example.lotterysystem.controller.result.CreateActivityResult;
import com.example.lotterysystem.controller.result.FindActivityListResult;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.controller.result.GetActivityDetailResult;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
public class ActivityController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    ActivityService activityService;

    @RequestMapping("/activity/create")
    public CommonResult<CreateActivityResult> createActivity(@RequestBody @Validated CreateActivityParam param){
        logger.info("createActivity : CreateActivityParam = {}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(
                convertToCreateActivityResult(activityService.createActivity(param))
        );

    }

    private CreateActivityResult convertToCreateActivityResult(CreateActivityDTO activity) {
        if(null==activity){
            throw new ControllerException(ControllerErrorCodeConstants.CREATE_ACTIVITY_ERROR);
        }
        CreateActivityResult result = new CreateActivityResult();
        result.setActivityId(activity.getActivityId());
        return result;
    }
    @RequestMapping("/activity/find-list")
    private CommonResult<FindActivityListResult> findActivityList(PageParam param){
        logger.info("findActivityList : param = {}",JacksonUtil.writeValueAsString(param));
        return CommonResult.success(
                convertToFindActivityResult(
                        activityService.findActivityList(param)
                ));
    }
    private FindActivityListResult convertToFindActivityResult(PageListDTO<ActivityDTO> pageListDTO){
            if(null==pageListDTO){
                throw new ControllerException(ControllerErrorCodeConstants.FIND_ACTIVITY_LIST_ERROR);
            }
            FindActivityListResult result = new FindActivityListResult();
            result.setTotal(pageListDTO.getTotal());
            result.setRecords(
                    pageListDTO.getRecords()
                    .stream()
                    .map(activityDTO -> {
                        FindActivityListResult.ActivityInfo activityInfo = new FindActivityListResult.ActivityInfo();
                        activityInfo.setActivityId(activityDTO.getActivityId());
                        activityInfo.setActivityName(activityDTO.getActivityName());
                        activityInfo.setDescription(activityDTO.getDescription());
                        activityInfo.setValid(activityDTO.valid());
                        return activityInfo;
                    }).collect(Collectors.toList())
            );
            logger.info("convertToFindActivityResult : FindActivityListResult = {}",JacksonUtil.writeValueAsString(result));
            return result;
    }

    @RequestMapping("/activity-detail/find")
    private CommonResult<GetActivityDetailResult> getActivityDetail(Long activityId){
        logger.info("getActivityDetail : activityId = {}",activityId);
        ActivityDetailDTO activityDetailDTO = activityService.getActivityDetail(activityId);
        return CommonResult.success(
                convertToGetActivityDetailResult(activityDetailDTO)
        );
    }

    private GetActivityDetailResult convertToGetActivityDetailResult(ActivityDetailDTO activityDetailDTO) {
        GetActivityDetailResult result = new GetActivityDetailResult();
        result.setActivityId(activityDetailDTO.getActivityId());
        result.setActivityName(activityDetailDTO.getActivityName());
        result.setDescription(activityDetailDTO.getActivityName());
        result.setValid(activityDetailDTO.valid());
        result.setPrizes(activityDetailDTO.getPrizeDTOList()
            .stream()
            .sorted(Comparator.comparingInt(prizeDTO -> prizeDTO.getTiers().getCode()))
            .map(prizeDTO -> {
                GetActivityDetailResult.Prize prize = new GetActivityDetailResult.Prize();
                prize.setPrizeId(prizeDTO.getPrizeId());
                prize.setName(prizeDTO.getName());
                prize.setImageUrl(prizeDTO.getImageUrl());
                prize.setPrice(prizeDTO.getPrice());
                prize.setDescription(prizeDTO.getDescription());
                prize.setPrizeTierName(prizeDTO.getTiers().getMessage());
                prize.setPrizeAmount(prizeDTO.getPrizeAmount());
                prize.setValid(prizeDTO.valid());
                return prize;
            }).collect(Collectors.toList())
        );
        result.setUsers(activityDetailDTO.getUserDTOList()
            .stream()
            .map(userDTO -> {
                GetActivityDetailResult.User user = new GetActivityDetailResult.User();
                user.setUserId(userDTO.getUserId());
                user.setUserName(userDTO.getUserName());
                user.setValid(userDTO.valid());
                return user;
            }).collect(Collectors.toList())
        );
        return result;
    }

}
