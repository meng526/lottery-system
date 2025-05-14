package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.util.List;
@Data
public class CreateActivityParam {

    private String activityName;

    private String description;

    private List<CreatePrizeByActivityParam> activityPrizeList;

    private List<CreateUserByActivityParam> activityUserList;

}
