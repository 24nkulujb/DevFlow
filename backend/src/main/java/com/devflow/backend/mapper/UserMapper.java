package com.devflow.backend.mapper;

import com.devflow.backend.model.UserCredentials;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserCredentials findByUsername(
            @Param("username") String username
    );
}
