package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WinningRecordMapper {

    @Select("select * from winning_record where activity_id = #{activityId}")
    List<WinningRecordDO> selectByActivityId(@Param("activityId")Long activityId);
    @Select("select count(1) from winning_record where activity_id = #{activityId} and prize_id = #{prizeId}")
    int countById(@Param("activityId")Long activityId, @Param("prizeId")Long prizeId);

    @Delete("<script>delete from winning_record where activity_id = #{activityId} "+
    "<if test = \"prizeId!=null\">  and prize_id =  #{prizeId}</if></script>")
    void deleteRecords(@Param("activityId")Long activityId, @Param("prizeId")Long prizeId);

    @Select("<script>select * from winning_record where activity_id = #{activityId} "+
            "<if test = \"prizeId!=null\">  and prize_id =  #{prizeId}</if></script>")
    List<WinningRecordDO> selectByActivityIdOrPrizeId(Long activityId, Long prizeId);

    @Insert("<script>" +
            " insert into winning_record (activity_id, activity_name," +
            " prize_id, prize_name, prize_tier," +
            " winner_id, winner_name, winner_email, winner_phone_number, winning_time)" +
            " values <foreach collection = 'items' item='item' index='index' separator=','>" +
            " (#{item.activityId}, #{item.activityName}," +
            " #{item.prizeId}, #{item.prizeName}, #{item.prizeTier}," +
            " #{item.winnerId}, #{item.winnerName}, #{item.winnerEmail}, #{item.winnerPhoneNumber}, #{item.winningTime})" +
            " </foreach>" +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int batchInsert(@Param("items") List<WinningRecordDO> winningRecordDOList);
}
