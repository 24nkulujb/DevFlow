# 00 · 工程地图：一次点击发生了什么

[目录](../LEARNING.md) · [下一章](01-language.md)

## 目标：先分清三个角色

前端是浏览器中的 Vue 程序，负责按钮、表单和显示。后端是 Java/Spring Boot 程序，负责规则、身份、权限和访问数据库。MySQL 是独立数据库服务，负责长期保存记录。

```text
点击 → Vue → HTTP 请求 → Java → SQL → MySQL
显示 ← JS 对象 ← JSON 响应 ← Java 对象 ← 查询结果
```

MyBatis 是 Java 执行 SQL 的工具，不是数据库。DBeaver 是数据库管理客户端，关闭它不等于关闭 MySQL。浏览器不能直接拿数据库密码连 MySQL，否则凭据暴露，规则也容易绕过。

## 工程文件夹怎么读

| 路径                                                 | 职责                         |
| ---------------------------------------------------- | ---------------------------- |
| frontend/src/views                                   | 整页组件，项目列表/登录/详情 |
| frontend/src/components                              | 任务抽屉、成员面板等部件     |
| frontend/src/api                                     | 发请求与处理响应             |
| frontend/src/types.ts                                | 前端数据形状                 |
| backend/src/main/java/com/devflow/backend/controller | HTTP 入口                    |
| 同上 Java 根目录下 service                           | 业务规则、权限与事务         |
| 同上 Java 根目录下 mapper                            | SQL 访问接口                 |
| backend/src/main/resources/mapper                    | 实际 MyBatis SQL             |
| backend/src/main/resources/db/migration              | 版本化建表脚本               |
| backend/src/test                                     | 自动化测试                   |

package.json 记录前端依赖和命令；pom.xml 记录 Java 依赖与构建；node_modules 和 target 是安装/编译产物，不是主要学习对象，不提交 Git。

## 两个网址不是两个重复页面

正常启动前端通常为 http://127.0.0.1:5173，后端为 http://127.0.0.1:8080；实际以启动日志为准。临时预览可能用 5174/18080，不是必须端口。

前端 /projects 表示显示哪个页面；后端 /api/projects 提供 JSON 数据。开发时 Vite 把 /api 转发后端，见 [vite.config.ts](../../frontend/vite.config.ts)。访问后端根路径得到404可能仅因没有定义根接口，不能据此判断启动失败。

## 亲眼看一次请求

先按 [README](../../README.md) 启动并登录。Chrome 按 Command+Option+I，进入 Network，选择 Fetch/XHR，刷新项目页，找到 /api/projects：

1. Headers 看地址、GET、状态码。
2. Response 看 JSON 文本。
3. Preview 看对象结构。
4. 创建项目后看 POST 的 Payload：这就是发送给后端的内容。

JSON 是数据文本，不是网页。例如教学数据：

```json
{ "id": 8, "name": "学习项目", "description": "练习" }
```

字符串双引号、数字不加引号、null 表示没有值；数组是多条值，写成 []。JSON 不包含函数和注释。

GET 通常读取；POST 通常新增/执行动作；本项目 PUT 提交完整编辑字段，PATCH 局部修改，DELETE 移除成员。方法名不会自动执行对应 SQL，仍需实现。

200 成功；201 创建；204 成功无响应体；400 输入问题；401 未登录；403 权限或CSRF问题；404 不存在或不可访问；409 冲突；500 未预期服务错误。服务启动成功与某条请求成功不是同一件事。

## 练习和答案

观察一次查询和创建，记录方法、路径、请求体、响应体和状态，不记录密码、Cookie、token。

答案应类似：GET /api/projects 返回200数组；POST /api/projects 发送名称/描述，返回201新项目。写请求前可能出现 /api/auth/csrf，第06章解释。ID取决于你的库，不要求连续。

关后端后新数据请求失败；关前端后新的页面加载失败；已提交数据库记录不会因关闭前后端自动消失。
