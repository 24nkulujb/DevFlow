
package com.devflow.backend;

import com.devflow.backend.mapper.UserMapper;
import com.devflow.backend.model.UserCredentials;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.devflow.backend.mapper.ProjectMapper;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void databaseConnectionWorks() {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1", Integer.class
        );

        assertEquals(Integer.valueOf(1), result);
    }

    @Autowired
    private ProjectMapper projectMapper;

    @Test
    void canCountProject() {
        long count = projectMapper.countProjects();

        System.out.println("当前项目数量: " + count);

        assertTrue(count > 0, "应能查询到之前插入的演示项目");

    }

    @Autowired
    private UserMapper userMapper;

    @Test
    void canVerifyDemoUsers() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        for (String username : new String[]{"alice", "bob"}) {
            UserCredentials user = userMapper.findByUsername(username);

            assertNotNull(user, "演示账号不存在：" + username);
            assertEquals(username, user.username());

            assertTrue(
                    encoder.matches("DevFlow123!", user.passwordHash()),
                    "演示密码验证失败：" + username
            );

            assertFalse(
                    encoder.matches("wrong-password", user.passwordHash()),
                    "错误密码不应该通过验证"
            );
        }
    }

    @Test
    void missingUserReturnsNull() {
        UserCredentials user = userMapper.findByUsername(
                "__missing_test_user__"
        );

        assertNull(user);
    }
}