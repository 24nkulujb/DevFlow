# 启动、迁移与部署

## 已有开发数据库

正常启动后端即可。Flyway baseline 0 兼容此前手建 project/app_user；V1 使用 IF NOT EXISTS，V2/V3 增量添加新表。不要重复运行旧教学 SQL。
旧项目在 demo 首次启动时归 Alice 管理，Bob/Charlie 加入为成员；所有原记录保留。
演示初始化由 app_setting 标记，仅执行一次。修改角色、移除成员、改项目名后重启不会恢复这些修改。
不删除 flyway_schema_history 或 app_setting 来“重新初始化”。

## IDEA 与命令行

IDEA 的应用配置和测试配置相互独立。应用配置需要 DB_USERNAME/DB_PASSWORD；隔离自动测试不需要这些变量。
终端环境变量不会自动从 IDEA 读取；.env 文件也不会被 Spring Boot 自动加载。
本机默认端口8080，前端5173。验收过程中可用 VITE_API_TARGET 指向其他后端地址，再启动 Vite。
示例：VITE_API_TARGET=http://127.0.0.1:18080 npm run dev -- --port 5174

## Docker Compose 本地演示

1. 安装 Docker Desktop，复制 .env.example 到 .env，替换数据库密码。
2. 根目录执行 docker compose up --build -d。
3. 访问 http://127.0.0.1:8088。前端 Nginx 将 /api 转到 backend。
4. 查看日志：docker compose logs -f backend。
5. 停止：docker compose down。默认保留数据库卷，不使用 down -v。
   MySQL 和后端没有暴露主机端口；前端仅绑定127.0.0.1。不是公开互联网部署。

## 生产注意事项

- 使用 APP_PROFILE=prod，VITE_DEMO=false；prod 不初始化公开演示账号。
- 使用全新数据库或在确认正确的已有数据库上手动建立 Flyway baseline，prod 禁止自动接管未知 schema。
- 新建正式账号时生成自己的 BCrypt 哈希，不使用仓库中的演示密码。
- 在 Nginx 前配置可信 HTTPS 终止；prod Session Cookie 为 Secure，只能经 HTTPS 使用。
- 使用最小数据库权限的应用账号，密码通过环境/密钥管理注入。
- 当前适合单实例、小团队；多实例需共享 Session，公开服务还应增加登录限流、监控与备份。
- 不把数据库密码、IDEA配置、真实数据库备份提交到Git。
- 日志中的错误堆栈只在服务端查看，接口返回统一错误。

## 第一版演示截图

使用干净浏览器窗口，捕获登录页、项目空间、三列看板、任务抽屉评论、成员管理和进度概览。
不要把浏览器扩展聊天面板、密码保存提示或个人标签页拍进去。
