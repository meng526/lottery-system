package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface ActivityMapper {

    @Insert("insert into `activity` (activity_name,description,status) "+
    " values (#{activityName},#{description},#{status})")
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    int insert(ActivityDO activityDO);
    @Select("select count(1) from `activity`")
    int count();
    @Select("select * from `activity` order by id desc"+
            " limit #{offset} , #{pageSize}")
    List<ActivityDO> selectActivityList(@Param("offset") Integer currentPage,
                                        @Param("pageSize") Integer pageSize);
    @Select("select * from activity where id = #{id}")
    ActivityDO selectActivityById(@Param("id") Long id);

    @Update("update activity set status = #{status} where id=#{activityId}")
    void updateActivityStatus(@Param("activityId")Long activityId,
                              @Param("status")ActivityStatusEnum targetActivityStatus);

}
