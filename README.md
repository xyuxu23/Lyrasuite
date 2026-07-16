# LyraSuite

Java 安全利用链生成器 & JNDI 服务 — 集成多种反序列化利用链、JNDI 注入、Shiro/Fastjson/SnakeYAML 漏洞利用的综合工具。

## 功能模块

| 模块 | 说明 |
|------|------|
| **Gadget Generator** | 反序列化利用链 Payload 生成（CommonsCollections 1-7/K1/K2、CommonsBeanutils、SpringJackson、URLDNS 等） |
| **JNDI Service** | 内嵌 RMI + LDAP 服务器，支持多种 JNDI 绕过 |
| **Fastjson** | Fastjson 漏洞利用 Payload 生成 |
| **Shiro** | Apache Shiro 密钥爆破、利用链探测与命令执行 |
| **SnakeYAML** | SnakeYAML 反序列化 Payload 生成 |
| **Codec** | 编解码工具（Base64、Hex、URL 编码等） |
| **Memory Shell** | 内存马相关功能 |

## 环境要求

- JDK 21
- Maven 3.8+（已内置 Maven Wrapper）

## 快速开始

```bash
# 构建
./mvnw clean package

# 运行
java -jar target/LyraSuite-Web-2.0.0.jar

# 或直接通过 Maven 启动
./mvnw spring-boot:run
```

启动后访问 http://localhost:8080

**默认账号：** `admin` / `admin123`

## 技术栈

- **后端：** Spring Boot 3.3 + Spring Security + Thymeleaf
- **前端：** Tabler UI + axios + htmx
- **核心依赖：** Commons Collections 3.1/4.0、Commons Beanutils、Javassist、UnboundID LDAP SDK、Apache Shiro

## 项目结构

```
src/main/java/com/xyuxu/javasec/
├── controller/          # REST API 和页面路由
├── service/             # 业务逻辑
├── core/
│   ├── payload/gadgets/ # 反序列化利用链实现
│   ├── payload/jndi/    # JNDI 绕过技术
│   ├── generator/       # Payload 生成器
│   └── server/          # 内嵌 HTTP/JNDI 服务器
├── dto/                 # 请求/响应对象
├── utils/               # 工具类
└── config/              # Spring Security 配置
```

## 添加自定义利用链

在 `core/payload/gadgets/` 下创建类，实现 `ObjectPayload<T>` 接口即可自动注册：

```java
public class MyGadget implements ObjectPayload<YourType> {
    @Override
    public YourType getObject(String command) throws Exception {
        // 构造利用链
    }
}
```

如需支持字节码注入，同时实现 `BytecodePayload<T>` 接口。

## License

本项目仅供安全研究和授权测试使用。
