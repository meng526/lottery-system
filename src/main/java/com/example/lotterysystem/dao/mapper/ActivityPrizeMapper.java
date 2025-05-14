package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityPrizeMapper {

    @Insert("<script>insert into `activity_prize` (activity_id,prize_id,prize_amount,prize_tiers,status)"+
            " values<foreach collection='items' item = 'item' separator = ',' >"+
            " (#{item.activityId},#{item.prizeId},#{item.prizeAmount},#{item.prizeTiers},#{item.status})"+
            " </foreach></script>")
    @Options(useGeneratedKeys=true,keyProperty = "id",keyColumn = "id")
    int batchInsert(@Param("items") List<ActivityPrizeDO> activityPrizeDOS);

    @Select("select * from activity_prize where activity_id = #{activityId}")
    List<ActivityPrizeDO> selectActivityPrizeByActivityId(@Param("activityId") Long activityId);

    @Select("select * from activity_prize where activity_id = #{activityId} and prize_id = #{prizeId}")
    ActivityPrizeDO selectActivityPrizeByActivityIdAndPrizeId(@Param("activityId") Long activityId,
                                                              @Param("prizeId") Long prizeId);

    @Select("select count(1) from activity_prize where activity_id = #{activityId} and status = #{status}")
    int countPrize(@Param("activityId")Long activityId,
                   @Param("status")ActivityPrizeStatusEnum status);
    @Select("select * from activity_prize where activity_id = #{activityId} and prize_id = #{prizeId}")
    ActivityPrizeDO selectPrizeById(@Param("activityId")Long activityId,
                                    @Param("prizeId")Long prizeId);
    @Update("update activity_prize set status = #{status} where activity_id = #{activityId} and prize_id = #{prizeId}")
    void updatePrizeStatus(@Param("activityId")Long activityId,
                           @Param("prizeId")Long prizeId,
                           @Param("status")ActivityPrizeStatusEnum targetPrizeStatus);
}
