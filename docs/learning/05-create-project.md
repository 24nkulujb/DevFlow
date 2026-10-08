# 05 · 创建项目的完整写入闭环

[上一章](04-vue.md) · [目录](../LEARNING.md) · [下一章](06-auth.md)

## 先写验收规则

登录用户可创建；名称非空最多50；描述最多500；创建者自动管理员；成功刷新列表；失败保留输入。这比“加个按钮”更完整，先定规则再设计接口、表与页面。

## 表单和请求

[ProjectsView](../../frontend/src/views/ProjectsView.vue)简化结构：

```vue
<form @submit.prevent="create">
  <input v-model="name" required maxlength="50" />
  <textarea v-model="description" maxlength="500"></textarea>
  <button :disabled="saving">创建项目</button>
</form>
```

v-model双向同步，submit表单事件，.prevent阻止浏览器默认整页跳转。取消按钮要type=button，否则可能提交。saving防当前页面连点，不是服务器幂等保证。

```ts
await createProjectApi({ name: name.value.trim(), description: description.value.trim() })
```

API再request<Project>，POST，JSON.stringify(input)转JSON文本；请求封装设置Content-Type。ID/创建时间/角色不是用户提交。成功关闭弹窗并load重查，因为输入没有服务器生成字段。

## Controller和DTO

[ProjectController](../../backend/src/main/java/com/devflow/backend/controller/ProjectController.java)：

```java
@PostMapping
public ResponseEntity<Project> create(
    @Valid @RequestBody ProjectInput input,
    Authentication auth
) {
    return ResponseEntity.status(201).body(service.createProject(input, auth.getName()));
}
```

RequestBody解析JSON，Valid触发DTO校验，ResponseEntity设置状态与体。WorkspaceRequests.ProjectInput的NotBlank拒绝空白/null，Size限制长度；描述没NotBlank，可以不填。

DTO是允许客户端写入字段清单，不直接用带id/role等的响应Project，避免混入不可修改字段。前端校验可绕过，后端必须独立校验。

## Service逐行理解

[ProjectService](../../backend/src/main/java/com/devflow/backend/service/ProjectService.java)：

```java
@Transactional
public Project createProject(ProjectInput input, String username) {
    Long userId = access.userId(username);
    Project p = new Project();
    p.setName(input.name().strip());
    p.setDescription(clean(input.description()));
    projects.insert(p);
    members.add(p.getId(), userId, "ADMIN");
    return projects.findAccessible(p.getId(), userId);
}
```

身份转用户ID → 构建对象 → 清理空白/空描述 → INSERT并写回主键 → 用主键建管理员关系 → 重查完整响应。

## 为什么需要事务

项目插入成功、成员失败会留下无人可见项目。Transactional使两条写入整体提交或回滚。默认常见运行时异常触发回滚；吞掉异常正常返回可能提交。

注解通过Spring代理生效，本项目Controller调用Service符合使用方式；同一对象内部自调用不会简单再经过代理。不是任意地方贴注解就有效。

事务不等于禁止多人同时访问，具体并发还要锁/隔离设计。

## 错误回到页面

ApiExceptionHandler将校验错误转400和message/fields，http.ts读取，页面formError显示，失败不清空输入。

写请求网络超时不证明未写入，可能服务成功但响应丢了；先刷新核对再重试。严格防重复要幂等键等后续设计，不能靠disabled按钮保证。

## 实操：绕过前端验证后端

个人开发库、登录后的前端页面Console运行：

```js
const csrf = await fetch('/api/auth/csrf').then((r) => r.json())
const result = await fetch('/api/projects', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
  body: JSON.stringify({ name: '   ', description: '校验练习' }),
})
console.log(result.status, await result.json())
```

只粘贴理解过的代码，不贴陌生来源脚本。预期400，fields.name有错误，没有新项目。insert返回1不是ID=1，是影响一行；主键从p.getId取。
