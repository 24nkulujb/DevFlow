# 03 · 查看项目：后端逐层拆解

[上一章](02-database.md) · [目录](../LEARNING.md) · [下一章](04-vue.md)

## 为什么分层

需求是“返回登录人参加的项目”。Controller处理HTTP，Service处理业务，Mapper处理SQL。分层让规则复用、查询易检查、测试不必每次开浏览器。这是本项目设计，不是唯一正确架构。

## 地址到方法

[ProjectController](../../backend/src/main/java/com/devflow/backend/controller/ProjectController.java)相关节选：

```java
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<Project> list(Authentication auth) {
        return service.listProjects(auth.getName());
    }
}
```

RestController把返回对象写响应体，不寻找HTML模板。RequestMapping给基础地址，无子路径GetMapping对应GET /api/projects。Authentication由Security提供当前身份，getName取得认证用户名，不让浏览器自报当前userId。

final字段不可重新绑定；构造器名与类相同，没有返回类型；this.service是对象字段，右边service是参数。

## 依赖注入：service哪来的

Spring扫描@Service等组件，创建对象并将所需ProjectService传入Controller，叫依赖注入。普通Java参数不会凭空生成，这是Spring管理对象的能力。

单构造器可直接被Spring使用，不必额外@Autowired。[BackendApplication](../../backend/src/main/java/com/devflow/backend/BackendApplication.java)的SpringBootApplication包含组件扫描，默认扫描入口所在包及子包，所以代码放com.devflow.backend下面。

## Service看似一行，继续追

[ProjectService](../../backend/src/main/java/com/devflow/backend/service/ProjectService.java)：

```java
public List<Project> listProjects(String username) {
    return projects.findForUser(access.userId(username));
}
```

先access.userId按认证用户名查用户ID，查不到抛401，再findForUser查成员项目。ProjectMapper虽保留findAll方法，这条链用findForUser，不要跟错函数。

SQL JOIN成员表是实际数据隔离，不能换成查全表再由页面隐藏。

## 返回值如何变JSON

```text
数据库每行 → Project对象 → List<Project> → JSON数组 → 浏览器JS数组
```

MyBatis映射行，Jackson序列化对象。“序列化”是对象转传输格式，“反序列化”反过来。role/memberCount/taskCount/doneCount来自查询角色与统计，并非project表都有这些列。

## IDEA断点实操

在Controller return行旁设断点，Debug启动后端，登录后刷新项目页。看auth，再用工具栏Step Into进入Service；键位以IDE提示为准。

暂停时浏览器会等待甚至超时，这是断点效果，不代表服务启动慢。观察后继续运行并取消不需要的断点。

## 练习与答案

写出完整链：
getProjects → request → ProjectController.list → ProjectService.listProjects → ProjectAccess.userId → ProjectMapper.findForUser → XML → MySQL。

为什么不让请求参数决定当前userId？用户可伪造他人ID；身份必须来自认证上下文。项目ID只是目标，仍要校验访问权限。
