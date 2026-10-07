-- 仅用于开发和演示。
-- 在新建的空表上执行一次，重复执行会产生重复数据。

USE devflow;

INSERT INTO project (name, description)
VALUES
    ('DevFlow 开发', '完成团队项目与任务协作平台'),
    ('个人作品集', '整理项目介绍、演示截图与部署说明');