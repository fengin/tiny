# Tiny Framework 开发者使用指南

## 1. 快速开始

以下开发都可以参考Demo工程里面的实现。

### 1.1 环境要求

- JDK >=1.8
- Maven >= 3.9
- 操作系统：支持 Windows、Linux、MacOS

### 1.2 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>cn.fengin</groupId>
    <artifactId>tiny</artifactId>
    <version>{version}</version>
</dependency>
```

version为具体打包的版本

### 1.3 创建配置文件

在 `src/main/resources` 目录下创建 `application.yaml`：

```yaml
server:
  http:
    port: 8080
    boss-threads: 1
    worker-threads: 4
    backlog: 128
    keep-alive: true
  tcp-servers:
    - name: default
      port: 8081
      boss-threads: 1
      worker-threads: 4
      backlog: 128
      device-idle-time: 60
      device-idle-timeout: 180
```

更多配置项见Demo里面的application.yaml

### 1.4 创建启动类

```java
public class Application {
    public static void main(String[] args) {
        // 启动 HTTP 服务器
        HttpServer httpServer = new HttpServer();
        new Thread(httpServer::start).start();

        // 启动 TCP 服务器
        TcpRegistry registry = new TcpRegistry("default");
        registry.registerCodec(new YourMessageCodec())
               .registerProcessor(new YourMessageProcessor());
        TcpServer tcpServer = new TcpServer(registry);
        new Thread(tcpServer::start).start();
    }
}
```

注意，启动TCP服务，需要有编解码的MessageCodec以及处理器MessageProcesor实现类注入，不然启动时检查会出错停止服务。

## 2. HTTP 服务开发

### 2.1 路由注册

```java
// 注册 GET 请求处理器
Router.get("/api/test", (ctx, request) -> {
    HttpResponseUtil.send(ctx, "Hello World!");
});

// 注册 POST 请求处理器
Router.post("/api/data", (ctx, request) -> {
    String data = request.getParameter("data");
    HttpResponseUtil.sendJson(ctx, "{\"status\":\"success\"}");
});
```

正常情况下，一般会仿照springboot编写controller类，实现具体的方法，本框架本着比较简单的原则，没有在这方面做丰富的注解功能和方法自定义封装，需要自行实现以下参数为作入参的方法

```java
ChannelHandlerContext ctx, HttpRequest request
```

以下是一个示例

```java
public class UserController {
    public void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
        // 获取请求参数
        String id = request.getParameter("id");

        // 处理业务逻辑
        User user = userService.getUser(id);

        // 返回响应
        HttpResponseUtil.sendJson(ctx, JsonUtil.toJson(user));
    }
}
```

### 2.3 静态资源访问

静态资源默认从 `src/main/resources/static` 目录加载：

```
static/
├── css/
├── js/
├── images/
└── index.html
```

支持的资源文件类型，见Demo里面application的static部分配置。

### 2.4 请求拦截插件

需要在服务器启动时使用Router.addInterceptor方法注册拦截插件。

可以参考Demo里面的两个插件，业务上使用比较多：

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

## 3. TCP 服务开发

TCP开发实现，主要用于连接管理TCP设备，以及和TCP设备上下行消息通信的功能。

一般上线步骤：

1. 设备连接到同TCP部署的同个网络（有线连接网络或公网直接访问）；

2. 设备配置TCP服务的IP地址和端口号；

3. 设备上线，往TCP服务端发送消息或者心跳包。

一般正常开发，需要处理设备的连接、连接管理、心跳等面向设备侧的通讯机制，门槛比较高，本tiny框架基本把这些工作完成了，TCP服务的开发只要做以下两个工作：

- TCP通讯协议的开发（消息编解码，协议开发）

- 针对不同通讯消息的业务实现处理

### 3.1 消息编解码器

实现MessageCodec的encode和decode方法即可，具体参考不同设备的协议编码实现。

需要注意一点，如果收到的数据包不完整，需要返回null。

```java
public class MessageCodec implements MessageCodec {
    @Override
    public Message decode(ByteBuf in) throws Exception {
        // 实现消息解码逻辑
        if (in.readableBytes() < 4) return null;

        // 读取消息内容
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        // 返回解码后的消息
        return decodeMessage(in, length);
    }

    @Override
    public void encode(Message msg, ByteBuf out) throws Exception {
        // 实现消息编码逻辑
        byte[] data = encodeMessage(msg);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
```

具体可参考Demo里的实现。 

### 3.2 消息处理器

主要用于业务处理，业务代码按照自己需要实现

```java
public class MessageProcessor implements MessageProcessor {
    @Override
    public void process(Message msg, Channel channel) {
        // 处理接收到的消息
    }

    @Override
    public void onConnected(String deviceId) {
        // 处理设备连接事件
        logger.info("Device connected: {}", deviceId);
    }

    @Override
    public void onDisconnected(String deviceId) {
        // 处理设备断开事件
        logger.info("Device disconnected: {}", deviceId);
    }
}
```

### 3.3 下行消息

 下行消息，需要通过业务保存的deviceId拿到消息连接通道，再往通道里写下行消息。

连接通道是由ConnectionManager维护的，可以直接用deviceId拿到对应设备的连接，示例如下：

```java
ConnectionManager manager = ConnectionManager.getInstance();
//拿到对应设备的连接
ConnectionManager.Connection conn = manager.getConnection("deviceId");
//发送下行消息
conn.writeAndFlush(new Message(){
    @Override
    public int getMessageType() {
        return 0;
    }
    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
    @Override
    public String getDeviceId() {
        return "deviceId";
    }
});
```

下行消息，代码需要自己实现Message类

## 4. 配置说明

### 4.1 HTTP 配置

```yaml
server:
  http:
    port: 8080                # 服务端口
    boss-threads: 1           # 接收连接的线程数
    worker-threads: 4         # 处理 IO 的线程数
    backlog: 128             # 连接队列大小
    keep-alive: true         # 是否保持长连接
```

### 4.2 TCP 配置

```yaml
server:
    # 可以配置多个TCP服务（每个服务可以配置不同的协议
    # 不同设备上行消息协议可能不同，要按协议都解析判断太耗性能，不如每个服务都按自己的协议解析）
    # 支持多服务启动，但是和设备的连接管理是共享的
  tcp-servers:
    - name: default          # 服务器名称
      port: 8081            # 服务端口
      boss-threads: 1       # 接收连接的线程数
      worker-threads: 4     # 处理 IO 的线程数
      backlog: 128         # 连接队列大小
      device-idle-time: 60  # 空闲检测时间(秒)
      device-idle-timeout: 180  # 空闲超时时间(秒)
```

## 5. 高级特性

### 5.1 数据库使用

默认使用本地SQLite数据库存储，支持数据库连接池，需实现AbstractDao

```java
/**
 * 抽象DAO实现
 * 提供通用的数据库操作实现
 *
 * @author fengin
 * @since 1.0.0
 */
public abstract class AbstractDao<T, ID> implements BaseDao<T, ID> {
```

### 5.2 本地缓存

实现了一个简单的本地缓存功能，支持缓存过期时间，具体使用CacheManager

```java
/**
 * 缓存管理器
 *
 * @author fengin
 * @since 1.0.0
 */
public class CacheManager {
```

### 5.3 性能优化

 参考Demo application.yaml里面的参数配置，不同业务量场景下选择不同的参数配置

# 