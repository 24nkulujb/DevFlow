# 07 · 成员、授权与并发边界

[上一章](06-auth.md) · [目录](../LEARNING.md) · [下一章](08-tasks.md)

## 先写权限表

| 操作                  | 非项目成员 | 普通成员       | 项目管理员 |
| --------------------- | ---------- | -------------- | ---------- |
| 看项目/任务/评论/统计 | 否         | 是             | 是         |
| 创建任务/评论         | 否         | 是             | 是         |
| 编辑项目/管理成员     | 否         | 否             | 是         |
| 修改任务              | 否         | 自己创建或负责 | 是         |

ADMIN/MEMBER属于某项目的成员关系。Alice可以是A管理员、B普通成员。不要给app_user全局isAdmin代替它。

## ID不代表访问许可

/api/projects/8中的8仅是访问目标。[ProjectAccess.member](../../backend/src/main/java/com/devflow/backend/service/ProjectAccess.java)查当前人关系，无关系404，合并“不存在/不能访问”避免泄露项目存在。

admin(role)再判ADMIN，不是所有授权失败都404：已知项目普通成员执行管理动作403。

## 子资源也要检查归属

[TaskMapper.xml](../../backend/src/main/resources/mapper/TaskMapper.xml)：

```sql
WHERE t.project_id=#{projectId} AND t.id=#{id}
```

有权进A也不能把B的taskId塞进A接口。TaskService.comments先member再existing(projectId,taskId)，再读评论。SQL外键不自动知道登录人权限。

## 成员操作逐步追

[ProjectService](../../backend/src/main/java/com/devflow/backend/service/ProjectService.java)：

- addMember：操作人管理员 → 用户存在 → 尚未加入 → 插关系。
- changeRole：管理员 → 目标成员存在 → 不把最后管理员降级 → 更新。
- removeMember：管理员 → 不能自己移除 → 目标存在且非ADMIN → 取消其未完成任务指派 → 删除成员关系。

移除是删关系，不删app_user或其其他项目。完成任务保留历史负责人。至少一管理员规则防项目无人可管理；另一管理员要先降级再移除。

## 为什么加行锁

两个管理员同时操作：一人确认Bob在项目准备指派，另一人移除Bob。检查和写入若没有协调，会留下离开的人负责新任务，叫竞态。

写操作在Transactional内调用lockMember，先：

```sql
SELECT id FROM project WHERE id=#{id} FOR UPDATE
```

锁项目行，再查成员和写入。遵守此协议的项目写操作排队，提交/回滚释放锁；不是锁整个数据库，也不保护不遵守协议的外部脚本。

这是小型项目的简单一致性方案，高并发可能瓶颈，更细锁需更多设计。

## 前端为什么也判断

TaskDrawer计算editable、成员页面隐藏按钮，让体验合理；但用户可能用旧页面或伪造请求，后端必须再次检查。前端友好，后端保证。

## 练习与答案

读测试listsOnlyMemberProjects、projectEditingRequiresAdmin、foreignProjectCannotReadTaskOrComment，解释风险：列表泄露、普通成员管理越权、跨项目任务/评论越权。

只测管理员成功不足。提交role=ADMIN也不能自己升级，因为接口先校验操作人已有角色和边界。
