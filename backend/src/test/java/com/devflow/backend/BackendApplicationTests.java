
package com.devflow.backend;

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
}