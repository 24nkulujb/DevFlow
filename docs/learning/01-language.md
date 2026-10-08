# 01 · 读懂代码的语法工具箱

[上一章](00-start.md) · [目录](../LEARNING.md) · [下一章](02-database.md)

本章示例用于理解语法，不要求覆盖业务文件。不会的语法回来看，不必一次背完。

## 变量、类型、函数

```java
String name = "学习项目";
Long id = 8L;
boolean allowed = true;
```

```ts
const name: string = '学习项目'
let count: number = 0
count = count + 1
```

变量是给值起名字。Java类型在名字前，TS类型通常在名字后。String/string是文本，boolean是真假，8L是Java长整数。const不可重新绑定，let可以；const对象内部仍可能修改。

Java long不能null，Long可以。前端 number | null 表示数字或空，任务负责人允许空因为可暂不指派。JS普通number不是任意精度整数，超大BIGINT项目需另定传输方式，本项目小ID暂无此问题。

```java
public String clean(String value) {
    return value == null ? "" : value.strip();
}
```

public是访问范围，String是返回类型，clean是函数名，括号是参数，花括号是函数体。return交出结果并结束；void表示不返回业务值。三元表达式是“条件 ? 成立值 : 不成立值”。strip删除首尾空白；不能对null调用方法。

## 类、对象、接口、record

[Project.java](../../backend/src/main/java/com/devflow/backend/model/Project.java) 是类，new Project()创建具体对象。p.setName设置字段，p.getId读取；点号访问成员。private字段限制外部访问，getter/setter也让MyBatis/Jackson按约定映射数据。

Java interface声明能力。[ProjectMapper](../../backend/src/main/java/com/devflow/backend/mapper/ProjectMapper.java) 声明insert方法，MyBatis运行时生成代理执行XML，不需要自己补一个MapperImpl。

```java
public record ProjectInput(String name, String description) {}
```

这是DTO简化示例。record自动生成构造器、访问器等，用input.name()而非getName()。记录组件不可重新赋值，不代表所有嵌套对象深度不可变。

```ts
interface ProjectInput {
  name: string
  description: string
}
const input: ProjectInput = { name: '作品集', description: '练习' }
```

TS interface描述形状，只参与开发时类型检查，不会自动验证网络数据。前端类型不能替代后端输入校验。

## 列表、泛型、导入与注解

Java List<Project>是项目列表；TS Project[]类似。request<Project[]>说明预期返回类型，帮助编辑器检查，不会运行时验证响应结构。

import引入名称；export导出名称；import type只引入类型。Java package声明命名空间，通常和目录一致。

@Service、@GetMapping是Java注解，给框架元信息，不是普通函数调用。不同注解用途不同，后面逐个拆解。

## 条件和空值

Java字符串内容比较用.equals，不用==。!"ADMIN".equals(role)表示不是ADMIN，这种写法role=null也不空指针。&&是且，||是或，!是取反。

JS一般用===、!==。auth.user?.id中?.遇到null/undefined不继续访问，得到undefined。value || ''对0/空串等假值也给默认；value ?? ''只对null/undefined给默认。

version?: number表示字段可以缺省；assigneeId: number | null表示字段必须存在但值可空。缺省、null、空串不是一回事。

## 箭头函数和集合处理

```ts
const names = projects.map((p) => p.name)
const selected = projects.filter((p) => p.name.includes('学习'))
const total = projects.reduce((sum, p) => sum + p.taskCount, 0)
```

(p)=>p.name是返回名字的函数。map转换，filter保留满足条件项，reduce累积，0是初始值。这些不会自动查数据库。

const {name, description}=input叫解构；{...options, headers}是浅复制，后写同名属性覆盖前面，嵌套对象不一定复制。Java username -> {...}也是函数行为表达，叫lambda。

## 异步与异常

```ts
async function load() {
  loading.value = true
  try {
    projects.value = await getProjects()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
```

这是项目查询简化版。Promise代表未来成功/失败；async函数返回Promise；await等待结果但不冻结整个浏览器。try执行可能失败操作，catch处理失败，finally总做收尾。throw抛出错误、中断正常链，交给外层处理。

e as Error是类型断言，不是运行时转换；本项目请求层约定抛Error，公共库更应检查异常类型。

## 练习与答案

找ProjectsView中的filtered和totals，解释filter/reduce：前者对已有项目按关键词筛选，后者累加任务数和完成数。

List<Project>不是单个项目；version?不等于number|null；await不等于事务，前者异步等待，后者数据库整体提交/回滚。
