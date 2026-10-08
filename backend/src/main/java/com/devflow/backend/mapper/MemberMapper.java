package com.devflow.backend.mapper;

import com.devflow.backend.model.WorkspaceViews.Member;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MemberMapper {
    String role(@Param("projectId") Long projectId, @Param("userId") Long userId);
    List<Member> list(@Param("projectId") Long projectId);
    int add(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId,
        @Param("role") String role
    );
    int changeRole(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId,
        @Param("role") String role
    );
    int remove(@Param("projectId") Long projectId, @Param("userId") Long userId);
    long countAdmins(@Param("projectId") Long projectId);
}
