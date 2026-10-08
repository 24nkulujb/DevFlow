# 02 · SQL、表关系与 MyBatis

[上一章](01-language.md) · [目录](../LEARNING.md) · [下一章](03-read-projects.md)

## 先看关系，再看建表语法

表每行是一条记录，每列是字段，字段类型和约束限制能存什么。读 [V1](../../backend/src/main/resources/db/migration/V1__base_tables.sql) 与 [V2](../../backend/src/main/resources/db/migration/V2__collaboration.sql)。

```text
app_user ← project_member → project
project ← task.project_id
app_user ← task.creator_id / task.assignee_id
task ← task_comment.task_id；app_user ← task_comment.author_id
```

箭头表示引用。用户与项目多对多，用成员中间表；项目与任务一对多。角色属于成员关系，不是用户全局属性。

## 拆解真实字段

```sql
project_id BIGINT NOT NULL,
user_id BIGINT NOT NULL,
role VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
PRIMARY KEY (project_id,user_id)
```

BIGINT大整数，VARCHAR(10)最多10字符，NOT NULL禁止空，DEFAULT没提供时默认值，联合主键保证同项目同用户只加入一次，不禁止参加不同项目。

FOREIGN KEY保证引用记录存在；CHECK限制角色枚举；“至少一个管理员”涉及多行，不能靠这里的单行CHECK，交给Service。

任务assignee_id允许null，due_date是DATE只存日期，DATETIME存日期时间。AUTO_INCREMENT自动编号，删除3/4后继续5正常，ID不是展示序号，不要重置生产主键。

索引帮助查找，例如(project_id,status)；它也占空间、增加写入维护成本，不是每列加越多越好。

## 只读SQL实操

DBeaver开发连接运行：

```sql
SELECT DATABASE();
SELECT id,name,description FROM devflow.project ORDER BY id DESC;
SELECT project_id,user_id,role FROM devflow.project_member;
```

库名不同换成自己的。DATABASE()为null表示未选库；可在编辑器选库、先USE devflow或写完整表名。“执行语句”通常执行光标所在/选中语句，“脚本”执行一组，依工具设置而异；只执行CREATE没执行前面的USE可能得到No database selected。

SELECT选列、FROM指定表、WHERE筛选、ORDER BY排序、DESC降序。SQL字符串用单引号。

## JOIN如何隔离项目

[ProjectMapper.xml](../../backend/src/main/resources/mapper/ProjectMapper.xml)：

```sql
FROM project p JOIN project_member m
ON m.project_id=p.id AND m.user_id=#{userId}
```

p/m是别名。前一条件连接项目成员关系，后一条件只保留当前人，因此不是前端隐藏别人项目。

#{userId}是MyBatis占位符，不能原样贴DBeaver；手动测试换成自己的数字ID。它采用参数绑定，不把输入拼成SQL语法。不要用文本替换${...}拼用户输入。参数绑定防注入，也不等于自动判断业务权限。

TaskMapper用LEFT JOIN连负责人，未指派仍返回任务；普通JOIN会排除没匹配的行。

## Mapper如何关联XML

[Java接口](../../backend/src/main/java/com/devflow/backend/mapper/ProjectMapper.java)：

```java
List<Project> findForUser(@Param("userId") Long userId);
```

XML的namespace对应接口完整名，select的id对应方法名，@Param给参数命名，resultType指定每行映射为Project。配置的下划线转驼峰将created_at对应createdAt；JSON最终使用Java字段名称。

## 插入与主键

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
  INSERT INTO project(name,description) VALUES(#{name},#{description})
</insert>
```

MySQL生成ID，MyBatis写回Project.id。insert返回int是影响行数，新ID从p.getId()取。不要把返回1当ID=1。

## 迁移和演示数据

Flyway按V1/V2/V3执行并记录版本，新环境可重建相同结构。已执行脚本不要随意改，未来变更新增下一版本。本章不要求创建V4。

DemoDataInitializer准备演示记录，不是结构迁移；demo profile与一次性标记避免反复覆盖用户操作。正式业务数据由接口产生，不需要人工一条条INSERT。

## 练习与答案

只用SELECT查自己一个项目的成员、比较有/无负责人任务。不执行DROP/DELETE或重置主键。

同人可参加两项目，因为联合主键组合不同；外键保证负责人用户存在，不保证其是当前项目成员，Service还要查project_member。
