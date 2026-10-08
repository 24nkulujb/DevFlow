package com.devflow.backend.mapper;

import com.devflow.backend.model.UserCredentials;
import com.devflow.backend.model.WorkspaceViews.UserOption;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    UserCredentials findByUsername(@Param("username") String username);
    UserCredentials findById(@Param("id") Long id);
    List<UserOption> candidates(@Param("projectId") Long projectId, @Param("query") String query);
    int insert(
        @Param("username") String username,
        @Param("hash") String hash,
        @Param("displayName") String displayName
    );
}
