package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.PrizeDO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface PrizeMapper {

    @Insert("insert into prize (name,price,description,image_url) "
            +"values (#{name},#{price},#{description},#{imageUrl})")
    @Options(useGeneratedKeys = true,keyProperty = "id",keyColumn = "id")
    int insertPrize(PrizeDO prizeDO);

    @Select("select count(1) from prize")
    Integer count();

    @Select("select * from prize order by id desc limit #{offset},#{pageSize}")
    List<PrizeDO> selectPrizeList(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("<script>"+
                " select id from `prize` where id in"+
                " <foreach item = 'item' collection = 'items' open = '(' close = ')' separator = ','>"+
                " #{item}"+
                " </foreach></script>")
    List<Long> selectExistByIds(@Param("items") List<Long> Ids);

    @Select("<script>select * from `prize` where id in "+
        " (<foreach collection='items' item='item' separator=','>"+
        " #{item}"+
        " </foreach>)</script>")
    List<PrizeDO> batchSelectByIds(@Param("items") List<Long> Ids);

    @Select("select * from prize where id = #{id}")
    PrizeDO selectPrizeById(@Param("id")Long prizeId);
}
