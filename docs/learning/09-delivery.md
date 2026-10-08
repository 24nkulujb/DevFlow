# 09 · 测试、排错、Git与交付

[上一章](08-tasks.md) · [目录](../LEARNING.md) · [术语](10-glossary.md)

## 工程师工作流

用户场景→验收条件→数据/接口设计→最小闭环→成功/失败验证→审查差异→本地提交→推远端。复杂需求拆多轮，每轮可运行、可验证，而不是最后一起拼接。

先问谁能操作、输入什么、失败怎么办；写实现前列测试边界。

## 一段测试逐项读

[BackendApplicationTests](../../backend/src/test/java/com/devflow/backend/BackendApplicationTests.java)：

```java
mvc.perform(get("/api/projects").with(user("alice")))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.length()").value(1))
    .andExpect(jsonPath("$[0].role").value("ADMIN"));
```

MockMvc模拟HTTP，不是浏览器；with(user)模拟身份，没验证真实密码，另有authenticationAndLogout测真实登录。andExpect断言不符合就失败；JSONPath $是根，[0]第一项。

SpringBootTest加载应用，AutoConfigureMockMvc提供工具，ActiveProfiles("test")选隔离配置，测试Transactional通常结束回滚。BeforeEach每个测试前准备场景，Test标记用例。

测试用H2不是你的MySQL；测试配置有独立schema且禁Flyway，所以这组测试不验证实际迁移兼容。

## 分清检查范围

分别进入目录执行：

```sh
# backend目录：编译、测试、打包
./mvnw -B verify
```

```sh
# frontend目录：静态检查、构建
npm run check
npm run build
```

```sh
# 项目根目录，后端/开发数据库已运行
node scripts/smoke-test.mjs
```

前端构建通过不等于所有浏览器交互通过。smoke是真实HTTP，会创建保留验证项目/任务，需要demo账户，只在个人开发环境跑；非8080按[ACCEPTANCE](../ACCEPTANCE.md)设置DEVFLOW_URL。

Docker配置已有，但交付时未安装Docker、未实际运行；不把部署配方当已验证线上服务。

## 排错先缩小范围

| 现象           | 先看                               |
| -------------- | ---------------------------------- |
| 白屏           | Console、路由、前端错误            |
| 无法连接       | Network地址、后端进程、代理        |
| 401            | /me、Cookie、是否重启会话          |
| 403            | 角色、CSRF                         |
| 404            | 页面/API地址、资源归属             |
| 400            | Payload、字段错误、日期            |
| 409            | 旧版本、重复成员                   |
| 500            | 后端具体异常，不只启动日志         |
| 数据库认证失败 | 进程实际环境变量、主机、用户、端口 |

不要为了排错关闭安全校验、删库或重装所有软件。记录步骤、预期、实际、状态、最小复现；密码/Cookie/token打码，IDE配置可能有密码，不公开。

## Git实际含义

工作区当前文件；暂存区准备提交；commit本地版本；push发送远端。working tree clean仅本地无未提交差异，不单独证明GitHub已同步。

项目根目录先：

```sh
git status
git diff
```

确认无密码/.env/产物，指定相关文件。例如文档改动：

```sh
git add docs/LEARNING.md docs/learning
git commit -m "docs: expand beginner learning guide"
git push origin main
```

这是示例，不要求未改文件再提交。团队通常用分支/PR；个人也按小任务提交。不要reset --hard清除看不懂的改动。

v1.0.0固定标签，新教程在main。[CI](../../.github/workflows/ci.yml)push/PR后自动构建测试，不等于生产安全和全部UI都验证。

## 部署概念

开发Vite处理源码；生产build产dist，由Nginx提供静态页面并代理/api；Java jar仍运行，MySQL仍独立。compose统一配置服务，不意味着无需账户密码；持久化卷保留数据库，删除卷可能丢数据。

生产要HTTPS、正式账户、备份等，见[DEPLOYMENT](../DEPLOYMENT.md)。

## 毕业练习：项目卡片创建时间

需求：现有createdAt格式化显示，导航不受影响，检查/构建过。先Network确认字段→Project类型→formatDate→模板→手测→check/build→diff→提交。

先自己做，再看提示：

```ts
import { formatDate } from '../types'
```

```vue
<small>创建于 {{ formatDate(project.createdAt) }}</small>
```

仅局部示例。字段已有，只是显示，不改后端。新“项目归档状态”则涉及权限、迁移、DTO、SQL、类型、UI、测试，不能只塞按钮。

理解标准：能解释数据从哪里来，什么时候该改后端，而不是照抄整个文件。
