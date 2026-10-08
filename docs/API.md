# API 第一版

所有业务地址以 /api 开头。认证使用 Session Cookie，写请求需带 CSRF header。

## 认证

| 方法 | 地址         | 说明                                            |
| ---- | ------------ | ----------------------------------------------- |
| GET  | /auth/csrf   | 无需登录，返回 headerName、token                |
| POST | /auth/login  | 表单编码 username、password；成功 204，失败 401 |
| GET  | /auth/me     | 当前用户 id、username、displayName              |
| POST | /auth/logout | 清除会话，返回 204                              |

登录是 application/x-www-form-urlencoded；其他写请求用 application/json。

## 项目与成员

| 方法   | 地址                            | 权限 / 请求                        |
| ------ | ------------------------------- | ---------------------------------- |
| GET    | /projects                       | 当前用户参与的项目                 |
| POST   | /projects                       | name、description；自动成为管理员  |
| GET    | /projects/{id}                  | 项目成员                           |
| PUT    | /projects/{id}                  | 管理员；name、description          |
| GET    | /projects/{id}/members          | 项目成员                           |
| GET    | /projects/{id}/users?q=关键词   | 管理员；最多20名尚未加入的已有用户 |
| POST   | /projects/{id}/members          | 管理员；userId、role               |
| PATCH  | /projects/{id}/members/{userId} | 管理员；role                       |
| DELETE | /projects/{id}/members/{userId} | 管理员；禁止自移除、管理员需先降级 |

role 为 ADMIN / MEMBER。成员移除不删除用户账号。

## 任务与评论

| 方法  | 地址                                   | 说明                                          |
| ----- | -------------------------------------- | --------------------------------------------- |
| GET   | /projects/{id}/tasks                   | q、status、priority、assigneeId、overdue 筛选 |
| POST  | /projects/{id}/tasks                   | 创建任务，默认 TODO                           |
| GET   | /projects/{id}/tasks/{taskId}          | 详情                                          |
| PUT   | /projects/{id}/tasks/{taskId}          | 完整编辑，必须带 version                      |
| PATCH | /projects/{id}/tasks/{taskId}/status   | status、version                               |
| GET   | /projects/{id}/tasks/{taskId}/comments | 按时间返回评论                                |
| POST  | /projects/{id}/tasks/{taskId}/comments | content；返回最新评论列表                     |
| GET   | /projects/{id}/stats                   | 整个项目统计，不受当前筛选影响                |

任务请求包含 title、description、status、priority、assigneeId、dueDate、version（编辑必填）。
status: TODO / IN_PROGRESS / DONE；priority: LOW / MEDIUM / HIGH。
dueDate 为 YYYY-MM-DD 或 null；assigneeId 为成员用户 ID 或 null。
创建请求的 status 要传 TODO，后端强制初始状态 TODO。
任务只能由管理员、创建者或负责人编辑；所有成员可评论。

## 错误

- 400：参数/JSON/日期格式无效。
- 401：未登录或登录失效。
- 403：无操作权限或 CSRF 校验失败。
- 404：资源不存在或不属于当前用户可访问项目。
- 409：重复成员、最后管理员限制、任务版本冲突。
- 500：不向客户端暴露堆栈，详细原因只记录在后端日志。

业务错误响应为 {message,fields}，fields 是字段名到校验信息的映射。
