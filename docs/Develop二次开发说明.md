# Tiny Framework 维护说明

## 1. 项目概述

Tiny Framework 是一个基于 Netty 的轻量级 Web 框架，专为物联网和边缘计算场景设计。本文档面向框架维二次开发人员，提供框架的维护指南。

## 2. 项目结构

```
tiny/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cn/fengin/tiny/
│   │   │       ├── config/         # 配置类
│   │   │       ├── context/        # 上下文管理
│   │   │       ├── http/           # HTTP服务相关
│   │   │       ├── tcp/            # TCP服务相关
│   │   │       └── util/           # 工具类
│   │   └── resources/
│   │       └── application-template.yaml    # 配置文件模板
│   └── test/
│       └── java/
│           └── cn/fengin/tiny/test/
│               ├── http/           # HTTP测试
│               └── tcp/            # TCP测试
└── pom.xml                         # 项目依赖管理
```

### 2.1 核心模块说明

#### HTTP 模块 (cn.fengin.tiny.http)

- `HttpServer.java`: HTTP服务器实现
- `Router.java`: 路由管理
- `HttpServerInitializer.java`: 通道初始化器
- `HttpRequest.java`: 请求封装
- `HttpResponse.java`: 响应封装
- `HttpResponseUtil.java`: 响应工具类

#### TCP 模块 (cn.fengin.tiny.tcp)

- `TcpServer.java`: TCP服务器实现
- `ConnectionManager.java`: 连接管理
- `message/`: 消息相关类
- `handler/`: 处理器相关类
- `register/`: 注册相关类

#### 配置模块 (cn.fengin.tiny.config)

- `ServerConfig.java`: 服务器配置
- `ConfigLoader.java`: 配置加载器

#### 上下文模块 (cn.fengin.tiny.context)

- `ApplicationContext.java`: 应用上下文
- `ContextInitializer.java`: 上下文初始化器

## 3. 开发环境配置

### 3.1 必要条件

- JDK >=1.8
- Maven >=3.9

### 3.2 开发环境设置

1. 克隆代码库
   
   ```bash
   git clone [repository-url]
   ```

2. 导入项目
- IntelliJ IDEA: File -> Open -> 选择项目目录

- Eclipse: File -> Import -> Existing Maven Projects
3. 安装依赖
   
   ```bash
   mvn clean install
   ```

## 4. 代码规范

### 4.1 Java 代码规范

- 使用 阿里巴巴JAVA代码规范
- 类名使用 PascalCase
- 方法名和变量名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 包名全小写

### 4.2 注释规范

- 类注释：说明类的用途、作者和版本
- 方法注释：说明方法的功能、参数和返回值
- 关键代码注释：说明复杂逻辑
- 使用 Javadoc 格式

### 4.3 提交规范

- 提交信息格式：`[类型] 描述`
- 类型：feat/fix/docs/style/refactor/test/chore
- 描述使用现在时态
- 每次提交专注于单一改动

## 5. 测试规范

### 5.1 单元测试

- 使用 JUnit 5
- 测试类命名：`*Test.java`
- 测试方法命名：`test[Feature]`
- 保持测试独立性
- 使用断言验证结果

## 6. 发布流程

### 6.1 版本管理

- 遵循语义化版本 (SemVer)
- 主版本号：不兼容的 API 修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

### 6.2 发布步骤

1. 更新版本号
   
   ```bash
   mvn versions:set -DnewVersion=x.y.z
   ```

2. 运行测试
   
   ```bash
   mvn clean test
   ```

3. 打包
   
   ```bash
   mvn clean package
   ```

4. 提交发布
   
   ```bash
   git tag vx.y.z
   git push origin vx.y.z
   ```

## 7. 性能优化建议

### 7.1 网络优化

- 使用零拷贝
- 启用 TCP_NODELAY
- 合理设置 SO_BACKLOG
- 优化缓冲区大小

### 7.2 内存优化

- 使用对象池
- 避免频繁创建对象
- 合理设置堆大小
- 控制缓存大小

### 7.3 线程优化

- 合理配置线程池
- 避免线程阻塞
- 使用非阻塞算法
- 控制线程数量