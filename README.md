# DevFlow · 团队项目与任务协作平台

Vue 3 + TypeScript + Java 17 + Spring Boot 4.1 + MyBatis + MySQL 的全栈作品集。第一版聚焦一个完整协作流程：登录 → 项目 → 成员 → 任务 → 评论 → 进度。

## 第一版功能

- 登录、退出、当前用户、Session 与 CSRF；未登录不能访问业务接口。
- 项目列表、创建、详情、编辑；创建者自动成为管理员，列表只返回参与的项目。
- 项目成员添加、角色调整、移除；禁止管理员移除自己，禁止移除最后一名管理员。
- 任务创建、详情、编辑、指派、优先级、截止日期、状态切换。
- 三列看板；任务标题、状态、负责人、优先级与逾期筛选。
- 成员评论；项目总量、进行中、完成率与逾期统计。
- 后端验证权限、参数与负责人归属；任务版本冲突返回 409。
- 响应式布局、原生模态框焦点管理、加载/空/失败状态。
- Flyway 增量迁移、隔离测试、真实 MySQL 冒烟脚本、GitHub Actions 与 Docker Compose。

[新手分章教材：代码、语法与设计思路](docs/LEARNING.md) · [架构与数据库](docs/ARCHITECTURE.md) · [接口说明](docs/API.md) · [验收清单](docs/ACCEPTANCE.md) · [部署说明](docs/DEPLOYMENT.md)

## 本机启动（你当前的开发方式）

需要 JDK 17、Node 24、MySQL。新机器先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS devflow CHARACTER SET utf8mb4;
```

不用重复执行 database/ 内旧教学 SQL；启动时 Flyway 自动建表和升级。

### 后端

IDEA 打开 backend/pom.xml，等待 Maven 同步。在 BackendApplication 的运行配置设置 DB_USERNAME 和 DB_PASSWORD（名称不能带前后空格）。可选 DB_URL，默认 jdbc:mysql://127.0.0.1:3306/devflow。
启动 BackendApplication，看到 Started BackendApplication。
也可在 backend 中执行 ./mvnw spring-boot:run，前提是当前终端已设置上述环境变量。

### 前端

```bash
cd frontend
npm ci
npm run dev
```

打开终端打印的地址，通常 http://127.0.0.1:5173。
如果端口有旧进程，先停止旧的前端。代码更新后也要在 IDEA 停止并重启后端，否则新页面可能调用旧接口。

### 演示账号

默认 profile 是 demo。首次启动初始化 alice / bob / charlie，密码均为 DevFlow123!。
已有账号不会被覆盖密码；此前创建的 Alice/Bob 账号继续使用原密码。

- Alice 管理旧的团队项目；Bob、Charlie 为成员。
- Bob 另外管理一个独立项目，Alice 看不到，用于演示数据隔离。
- 旧项目只在首次演示初始化时补成员，之后不会恢复被移除的成员或重建已修改的数据。
- 演示凭据公开，仅用于本地作品集。prod profile 不创建演示账号。

## 测试与构建

```bash
cd backend
./mvnw verify
```

测试自动使用 H2 隔离数据库，不依赖本地 root 密码，也不改本地 MySQL。

```bash
cd frontend
npm run check
npm run build
```

后端和真实 MySQL 已启动时，在根目录执行：

```bash
node scripts/smoke-test.mjs
```

可通过 DEVFLOW_URL 修改目标地址。此脚本使用演示账号，会创建一个独立验收项目及两条任务，保留供展示；不要对正式环境运行。

## 容器演示

安装 Docker 后：

```bash
cp .env.example .env
```

编辑 .env，替换两个数据库密码，然后：

```bash
docker compose up --build -d
```

访问 http://127.0.0.1:8088。MySQL 数据保存在具名卷。容器启动配方已提供，但当前电脑没有 Docker，未在本机实际运行容器。
这里的 .env 用于 Compose，不会自动给 IDEA 注入环境变量。

## 第一版边界

项目和任务不提供删除；评论不提供编辑/删除。不含注册、附件、通知、拖拽、AI、操作日志。
当前是单实例 Session，服务重启后需要重新登录。生产部署应使用 HTTPS、关闭演示初始化、建立正式账号，并按需要扩展登录限流与集中 Session。
当前任务列表适合小团队；大规模数据的分页与检索优化留待下一版。

## 演示顺序

1. Alice 登录，查看项目卡片与统计。
2. 打开项目，看板创建任务，指派 Bob，选择日期和优先级。
3. 点击任务标题，查看抽屉详情并发表评论。
4. 在两页同时编辑任务，展示 409 版本冲突。
5. 展示成员管理与进度概览。
6. Bob 登录，展示不同角色与独立项目。
7. 退出后直接请求业务接口，展示后端认证。

截图建议：登录、项目空间、看板、详情抽屉、成员管理、统计概览；截取干净页面，避开浏览器扩展浮窗。
