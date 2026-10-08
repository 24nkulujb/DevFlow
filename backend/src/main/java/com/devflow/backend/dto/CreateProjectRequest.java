package com.devflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest (
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 50, message = "项目名称最多50个字符")
        String name,

        @Size(max = 50, message = "项目描述最多50个字符")
        String description

        ){

}
