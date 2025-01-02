# Tiny Framework 示例说明

## 1. 示例项目简述

本示例项目展示了如何使用 Tiny Framework 构建一个完整的物联网设备管理系统。该示例包含了 HTTP 服务和 TCP 服务的完整实现，展示了框架的核心功能。

业务功能（不完整，毕竟只是demo）见

### 1.1 依赖环境

- jdk >=1.8

- maven >=3.9

- 操作系统：支持 Windows、Linux、MacOS

### 1.2 快速启动

1、进入到tiny子目录，先运行tiny框架包安装到本地库，运行 mvn install

2、进入到demo工程，直接运行DemoApplication.java

3、访问http://localhost:8080/index.html，登录账户密码：admin/admin123

4、TCP可以运行cn.fengin.tiny.demo.tcp.client.CenterClient往服务端发消息

### 1.3 主要实现框架的功能点

- HTTP 服务：实例 Web 接口和静态资源访问
  - HTTP请求拦截注册、路由注册
  - HTTPServer启动
  - 简单的本地SQLite数据库访问
- TCP 服务：实现TCP服务端启动监听消息和模拟客户端发送TCP消息示例
  - 消息体定义
  - 消息编解码示例
  - 消息事件处理

### 1.4 系统架构

```
[设备] <--TCP--> [TCP服务器] <--> [消息处理器]
                     |
[用户] <--HTTP--> [HTTP服务器] <--> [业务处理器]
```

## 2. 项目结构

```
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cn/fengin/tiny/demo/
│   │   │       ├── DemoApplication.java        # 应用入口
│   │   │       ├── http/                       # HTTP相关
│   │   │       │   ├── controller/             # 控制器
│   │   │       │   ├── dao/                    # 数据访问层
│   │   │       │   ├── interceptor/            # 拦截器
│   │   │       │   ├── model/                  # 数据模型
│   │   │       │   ├── service/                # 服务层
│   │   │       │   └── RouterRegistry.java     # 路由注册
│   │   │       └── tcp/                        # TCP相关
│   │   │           ├── client/                 # 客户端模拟
│   │   │           ├── message/                # 消息定义
│   │   │           └── processor/              # 消息处理
│   │   └── resources/
│   │       ├── static/                         # 静态资源
│   │       │   ├── css/                        # 样式文件
│   │       │   ├── js/                         # JavaScript文件
│   │       │   └── index.html                  # 首页
│   │       └── application.yaml                # 配置文件
│   └── test/                                   # 测试代码
└── pom.xml                                     # 项目依赖
```

## 3. 功能实现

### 3.1 HTTP 服务实现

#### 服务启动

```java
    /**
     * 启动HTTP服务，根据实际需要启动步骤需要
     * 1、按需求情况是否注册http请求拦截器
     * 2、按实际情况注册路由
     * 3、启动服务
     */
    private static void startHttpServer() {
        //注册拦截器、路由信息
        RouterRegistry.register();
        // 启动HTTP服务器
        HttpServer httpServer = new HttpServer();
        new Thread(httpServer::start).start();
    }
```

#### 拦截器实现示例

```java
/**
 * API认证拦截器示例
 * 处理系统间调用的API鉴权，一般用于开放接口
 */
public class ApiAuthInterceptor implements Intercept
```

```java
/**
 * 用户认证拦截器
 * 处理需要用户登录的页面和API，一般用于本地页面访问接口
 */
public class UserAuthInterceptor implements Interceptor {
```

#### 拦截器和路由注册

```java
//1.注册拦截器
Router.addInterceptor(new ApiAuthInterceptor());

UserAuthInterceptor userAuthInterceptor = new UserAuthInterceptor();
userAuthInterceptor.setIgnorePaths(Arrays.asList("/api/admin/login","/api/open/**"));
Router.addInterceptor(userAuthInterceptor);

//2.注册路由
// 初始化服务和控制器
UserController userController = new UserController();

//登录接口
Router.post("/api/admin/login", userController::login);
```

### 3.2 TCP 服务实现

#### 服务启动

```java
/**
 * 启动TCP服务，根据实际需要启动步骤需要
 * 1、注册消息编解码器
 * 2、注册业务处理器
 * 3、启动服务
 */
private static void startTcpServer() {
    //启动TCP服务器，指定使用default的连接配置
    TcpRegistry registry = new TcpRegistry("default");
    //加载编解码器、处理器等驱动
    registry.registerCodec(new CenterMessageCodec())
            .registerProcessor(new CenterMessageProcessor());
    //启动服务
    TcpServer tcpServer = new TcpServer(registry);
    new Thread(tcpServer::start);
}
```

#### 消息定义实现（协议相关，设备决定）

```java
/**
 * 集中器消息示例
 *
 */
public class CenterMessage implements Message {
```

#### 消息编解码（协议相关，设备决定）

```java
/**
 * 集中器的消息编解码器示例
 * 实现MessageCodec接口，处理消息的序列化和反序列化
 * 消息格式(协议)：
 * [总长度(4字节)][设备ID长度(4字节)][设备ID][消息类型(4字节)][消息内容]
 */
public class CenterMessageCodec implements MessageCodec {
```

#### 消息处理器

```java
/**
 * 消息处理器DEMO, 收到相关消息事件后，在这里丢给你的业务逻辑去处理
 * 一种处理器对应一种消息编解码器CodeC
 */
public class CenterMessageProcessor implements MessageProcessor {
```

## 4. 配置说明

### 4.1 应用配置

```yaml
server:
  http:
    port: 8080
    boss-threads: 1
    worker-threads: 4
    backlog: 128
    keep-alive: true
  tcp-servers:
    # 可以配置多个TCP服务（每个服务可以配置不同的协议
    # 不同设备上行消息协议可能不同，要按协议都解析判断太耗性能，不如每个服务都按自己的协议解析）
    # 支持多服务启动，但是和设备的连接管理是共享的
    - name: default
      port: 8081
      boss-threads: 1
      worker-threads: 4
      backlog: 128
      device-idle-time: 60
      device-idle-timeout: 180
```

## 5. 示例业务功能

### 5.1 功能概况

边缘侧设备管理系统是一个数据采集平台，主要用于采集用户的用电数据信息，以提供给需要此类数据信息的接口平台。面向设备侧，它通过远程通信信道对远端电表数据进行集中抄读和下发指令控制；向面业务平台侧，提供开放接口，允许平台下发设备基础信息、数据采集策略、设备控制指令；设备管理除了作为业务平台和设备通讯的中间”代理“外，还会根据数据采集策略定时采集设备数据上报。

### 5.2 已实现的界面功能

#### 5.2.1 登录

服务部署时，确定好一个唯一的账号密码，不需要支持多用户、不需要存入数据库，简单登录进入后台；

#### 5.2.2 首页

- 界面分成简单的菜单和首页展示，具有页头和页尾，页头有产品logo和产品名字，页尾有版权信息。

- 首页展示内容包括：
  
  - 系统状态数据
    
    - 启动时间
    
    - 运行时长
    
    - 占用内存大小
    
    - 所用硬盘空间
    
    - 心跳上报时间
    
    - 当前IP
    
    - HTTP端口号
    
    - TCP端口号
  
  - 设备数据总况
    
    - 集中器数量（在线数/离线数）
    
    - 电表数量（在线数/离线数）
    
    - 上次采集电表时间
    
    - 采集电表成功和失数量(成功数量/失败数量)
  
  - 操作功能
    
    - 配置
      
      - 平台IP/域名
      
      - 平台远程端口号
      
      - 平台密钥
      
      - 对公网IP
      
      - 当前机器密钥
    
    - 重新同步数据，系统后台收到请求，会去业务平台拉取集中器、电表等数据

#### 5.2.3 集中器设备(center)列表

- 列表支持翻页、按集中器名称或者集中器地址查询

- 列表单条数据显示

#### 5.2.4 电表设备(ammeter)列表

- 列表支持翻页、按集中器地址或者电表地址查询

- 列表单条数据显示

- 点击列表里面单条数据的表址弹出层查看表详情

- 支持多条电表选中，点击批量采集上报数据