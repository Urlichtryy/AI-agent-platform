# AI 代码助手（全栈示例）

## 结构
- backend/demo: Spring Boot 服务（Web + Security + JPA + JWT）
- frontend: Vue 3 + Vite 前端
- database/init: MySQL 初始化 SQL
- docker-compose.yml: 一键启动 MySQL / 后端 / 前端

## 运行

### 方式一：Docker 一键启动
1. 准备环境变量（千问 API KEY 可选）：

在项目根目录创建 `.env`（或导出到环境变量）：

```
MYSQL_ROOT_PASSWORD=rootpass
MYSQL_DATABASE=cap_db
MYSQL_USER=cap_user
MYSQL_PASSWORD=cap_pass
DASHSCOPE_API_KEY=你的DashScopeKey
```

2. 构建并启动：

```
docker compose up -d --build
```

3. 访问：
- 前端： http://localhost:5173
- 后端： http://localhost:8080

### 方式二：本地运行
- 数据库：本地启动 MySQL 并执行 `database/init` 目录下 SQL。
- 后端：
  - 设置环境变量：`DASHSCOPE_API_KEY`（可选）
  - `cd backend/demo && ./mvnw spring-boot:run`
- 前端：
  - `cd frontend && npm i && npm run dev`

## API 约定
- 统一响应：`{ code, message, data, timestamp }`
- 用户：注册/登录/查询信息/更新资料/修改密码
- 会话：创建/列表/删除/发送消息/查询消息
- LLM：调用阿里云通义千问兼容端点 `https://dashscope.aliyuncs.com/compatible-mode/v1`（默认模型：`qwen-max`）

## 说明
- JWT 密钥与有效期在 `application.yml` 中通过环境变量可覆盖。
- LLM 未配置时，后端将返回占位回复，便于前后端联调。
