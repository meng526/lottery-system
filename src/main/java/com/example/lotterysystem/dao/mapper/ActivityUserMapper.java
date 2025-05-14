package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityUserMapper {

    @Insert("<script>insert into `activity_user` (activity_id,user_id,user_name,status)"+
            " values<foreach collection='items' item = 'item' separator = ',' >"+
            " (#{item.activityId},#{item.userId},#{item.userName},#{item.status})"+
            " </foreach></script>")
    @Options(useGeneratedKeys=true,keyProperty = "id",keyColumn = "id")
    int batchInsert(@Param("items") List<ActivityUserDO> activityUserDOS);

    @Select("select * from activity_user where activity_id = #{activityId}")
    List<ActivityUserDO> selectActivityUserByActivityId(@Param("activityId") Long activityId);
    @Select("<script>select * from `activity_user` where activity_id = #{activityId} and user_id in"+
            " <foreach collection='ids' item = 'item' separator = ',' open='(' close=')' >"+
            " #{item}"+
            " </foreach></script>")
    List<ActivityUserDO> selectUserByIds(@Param("activityId")Long activityId,
                                         @Param("ids")List<Long> ids);

    @Update("<script>update `activity_user` set status = #{status} where activity_id = #{activityId} and user_id in"+
            " <foreach collection='ids' item = 'item' separator = ',' open='(' close=')' >"+
            " #{item}"+
            " </foreach></script>")
    void updateActivityUserStatus(@Param("activityId")Long activityId,
                                  @Param("ids")List<Long> userIds,
                                  @Param("status")ActivityUserStatusEnum targetUserStatus);
}
