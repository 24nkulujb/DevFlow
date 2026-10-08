# 08 · 任务：组件、筛选、并发与统计

[上一章](07-permissions.md) · [目录](../LEARNING.md) · [下一章](09-delivery.md)

## 从已知闭环扩展

任务仍是表单→API→Controller→DTO→Service→Mapper→数据库→响应→界面，新增难点是状态、负责人、权限与多人编辑。

顺序：[types.ts](../../frontend/src/types.ts) Task/TaskInput → [TaskDrawer](../../frontend/src/components/TaskDrawer.vue) → [TaskController](../../backend/src/main/java/com/devflow/backend/controller/TaskController.java) → [TaskService](../../backend/src/main/java/com/devflow/backend/service/TaskService.java) → [TaskMapper.xml](../../backend/src/main/resources/mapper/TaskMapper.xml)。

## 组件按职责拆

ProjectDetailView管项目、看板、筛选；TaskDrawer管单任务编辑/讨论；MembersPanel管成员；StatsPanel显示数字；ModalShell处理弹窗和焦点。不是随便按行数分文件。

```ts
const props = defineProps<{
  open: boolean
  projectId: number
  taskId: number | null
  members: Member[]
  role: string
}>()
const emit = defineEmits<{ close: []; saved: [] }>()
```

真实抽屉节选：props是父传入只读参数，taskId=null新建、数字编辑；emit('saved')通知父刷新，emit('close')请求关闭，不直接改父变量。

defineProps/defineEmits是script setup编译宏，无需import。模板:project-id对应projectId。

## watch与并行请求

watch监听open/taskId/projectId，变化时加载，immediate=true建立时也执行。computed算值，watch做请求等副作用。

```ts
const [data, discussion] = await Promise.all([
  getTask(props.projectId, props.taskId),
  getComments(props.projectId, props.taskId),
])
```

两独立读取并行，返回顺序匹配输入；一个失败整体reject，本版不显示半份详情。generation递增忽略旧响应，避免A慢请求覆盖刚打开B；并没有取消服务器工作。

## 表单值转接口值

```ts
assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
dueDate: form.dueDate || null,
```

输入控件空串转成null，负责人文本转数字。更新附旧version；新建不用旧version。DTO允许version缺省，但Service更新明确拒绝缺版本。

TaskService.create即使收到合法DONE也设TODO，体现新建待处理服务器规则。

## 乐观锁防旧页面覆盖

Alice/Bob都读version=0，Alice成功后变1，Bob仍交0要拒绝。

```sql
UPDATE task SET title=#{title}, version=version+1
WHERE id=#{id} AND project_id=#{projectId} AND version=#{version}
```

这是省略其他字段的教学SQL，不是替换文件。更新匹配旧版本，过期影响0行。TaskService.save检查!=1抛409。

项目行锁协调成员与写入事务，任务version检测用户过期快照；即使写操作排队，后一个仍可能拿旧页面，所以两者不重复。失败不自动合并，刷新前提示会覆盖未保存编辑。

## 历史负责人两路径区别

移除成员解除未完成指派、version+1；完成任务保留历史。完整编辑DONE且仍DONE可保留原历史负责人。

状态PATCH重新打开时清空离开成员负责人；完整PUT若重新打开还带离开的人，assignee校验拒绝，要选暂不指派/现成员。不要笼统认为所有重开自动清空。

## 筛选、评论、统计

API用URLSearchParams编码筛选；XML <if>有条件才加AND，是动态SQL，不是Vue v-if。Service先检查枚举合法。

评论作者来自认证用户，先查成员和任务归属，再写。API返回完整评论数组，成功清输入，失败保留。

统计COUNT计总数，SUM(CASE WHEN ... THEN 1 ELSE 0 END)计各状态，COALESCE把空合计转0。完成率done/total×100，空项目0避免除零。逾期是北京时间截止早于今天且非DONE；今天不算逾期。统计为整个项目，不随局部看板筛选变。

## 练习与答案

两标签打开测试任务，先后保存，第二次409。读taskPermissionsAndOptimisticLocking，Bob成功version1，Alice用0失败，最终仍IN_PROGRESS。

只改“已完成”显示文案不用改DONE；增加新状态需类型、DTO、SQL约束、Service、看板、统计、测试共同更新。
