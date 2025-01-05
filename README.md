# 背景

这是一个80%以上由Cursor开发的项目，其中HttpServer部分和Demo工程基本全部由Cursor开发完成，TCP部分由于本人开始对设备通讯不是特别熟悉，提示词不太好写，所以开始让Cursor完成大部分，然后手工调整部分，文档也是由Cursor写了大部分。

具体Cursor开发过程可以参考这个文章: https://aibook.ren/archives/use-cursor-dev-framework

更多相关AI知识的学习，可以访问AI全书：[AI全书 ](https://aibook.ren)



Gitee地址：[https://gitee.com/fengin/tiny.git](https://gitee.com/fengin/tiny.git)

GitHub地址：[https://github.com/fengin/tiny.git](https://github.com/fengin/tiny.git)



**项目目录说明**：

```
tiny/                               #根目录  
├── tiny/                           #框架代码
│── demo/                           #框架使用案例
│── docs/                           #相关文档，分成维护人员、使用人员
│── README.md                       #本说明文
```

Tiny Framework 是一个基于 Netty 的轻量级 Web 框架，专为物联网和边缘计算场景设计，对机器资源有限，web功能需求简单的场景非常适合。

**框架特点**

- 轻量级：最小化依赖，启动快速，默认采用本地SQLite存储，资源占用少
- 高性能：基于 Netty 的异步非阻塞架构
- 易扩展：模块化设计，支持自定义扩展
- 双协议：同时支持 HTTP 和 TCP 协议
- 安全性：内置多重安全机制

# 快速开始

1. 下载源码

2. 导入IDE

3. 运行DemoApplication

4. 访问http://localhost:8080/index.html

更多查看docs/目录下相关文档说明

# 相关文档

文档都在docs目录下面，比较丰富，主要分成几类文档

- 总体功能特性说明：[Features功能说明.md](docs/Features功能说明.md)

- 面向框架二次开发人员：[Develop二次开发说明.md](docs/Develop二次开发说明.md)

- 面向框架使用的开发人员：[Use Guide框架使用说明.md](docs/Use Guide框架使用说明.md) 和 [Demo示例说明.md](docs/Demo示例说明.md)

# 开源说明

本开源项目允许拷贝、复制和免费商用，但须保留作者来源。

作者：凌封

微信：fengin
