package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper {

    @Select("select count(*) from user where email = #{email}")
    int countByMail(@Param("email") String email);

    @Select("select count(*) from user where phone_number=#{phoneNumber}")
    int countByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    @Insert("insert into `user` (user_name,email,phone_number,password,identity)"+
            " VALUES(#{userName},#{email},#{phoneNumber},#{password},#{identity})")
    @Options(useGeneratedKeys = true,keyProperty = "id",keyColumn = "id")
    void insert(UserDO userDO);

    @Select("select * from `user` where phone_number = #{phoneNumber}")
    UserDO queryByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    @Select("select * from `user` where email = #{email}")
    UserDO queryByEmail(String email);

    @Select("<script>select * from `user` <if  test = \"identity != null\"> where identity = #{identity} </if> order by id desc</script>")
    List<UserDO> selectUserListByIdentity(String identity);

    @Select("<script>select id from `user` where id in "
            +" <foreach collection = 'items' item = 'item' open = '(' close = ')' separator = ','> "
            +" #{item}"
            +" </foreach></script>")
    List<Long> selectExistByIds(@Param("items") List<Long> Ids);

    @Select("<script>select * from `user` where id in "
            +" <foreach collection = 'ids' item = 'item' open = '(' close = ')' separator = ','> "
            +" #{item}"
            +" </foreach></script>")
    List<UserDO> selectUserByIds(List<Long> ids);
}
