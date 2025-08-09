# ERP系统

## 项目简介

本项目是一个基于Java开发的企业资源规划(ERP)系统后端服务。该系统包含了销售、客户管理、物料管理、订单处理等核心功能模块，使用Spring Boot框架构建，采用MySQL作为数据存储。

主要功能包括：
- 销售订单管理
- 客户与联系人管理
- 物料与库存管理
- 询价单处理
- 业务伙伴关系管理

## 技术栈

- 后端框架：Spring Boot 2.7.x
- 数据库：MySQL 5.7+
- 构建工具：Maven 3.6+
- 编程语言：Java 8+
- ORM框架：MyBatis
- 日志框架：SLF4J + Logback
- API文档：SpringDoc OpenAPI (Swagger UI)
- 其他：Lombok、JWT、Druid连接池

## 运行环境要求

- Java 22
- MySQL 8.0或更高版本
- Maven 3.6或更高版本

## 如何在新电脑上运行项目

### 1. 环境准备

#### 安装JDK 22
1. 从Oracle官网或OpenJDK下载JDK 22
2. 安装JDK并配置环境变量：
    - 设置`JAVA_HOME`环境变量指向JDK安装目录
    - 将`%JAVA_HOME%\bin`添加到`PATH`环境变量

#### 安装Maven（可选，项目包含Maven Wrapper）
1. 从Apache Maven官网下载最新版本
2. 解压到指定目录
3. 设置`MAVEN_HOME`环境变量
4. 将`%MAVEN_HOME%\bin`添加到`PATH`环境变量

#### 安装MySQL 8.0
1. 从MySQL官网下载MySQL Community Server 8.0
2. 运行安装程序并按照提示完成安装
3. 启动MySQL服务


## 端到端测试（E2E）

本项目提供一个自动枚举 POST 接口的端到端测试框架，位于：
- src/test/java/webserver/e2e/PostEndpointsE2ETest.java

特点：
- 自动发现所有 @PostMapping / @RequestMapping(method=POST) 的端点
- 解析类级 @RequestMapping 前缀，自动拼接完整路径
- 通过示例请求体映射 sampleBodies 决定哪些端点实际发起请求并断言 200

### 运行方式

1) 在本地内存中运行（MockMvc，不走真实网络）
- 默认方式：
  - mvn -Dtest=webserver.e2e.PostEndpointsE2ETest test
- 说明：该方式会启动 Spring 上下文并用 MockMvc 调用控制器方法，不需要服务实际运行在 localhost:8080。

2) 直连已运行的远端服务（通过 baseUrl 开关）
- 在测试类里支持读取 `e2e.base-url`，有值则切换为 HTTP 直连：
  - mvn -Dtest=webserver.e2e.PostEndpointsE2ETest -De2e.base-url=http://<ip>:<port> test
- 注意：端点列表仍来自本地代码的路由定义；如果远端版本与本地代码不一致，可能出现 404。

### 添加/维护端点样例（JSON 文件）
- 样例文件路径：`src/test/resources/e2e/post-bodies.json`
- 支持两种写法：
  - 单个用例：value 直接是对象或数组
  - 多个用例：用 `{"cases": [ {...}, {...} ]}` 包装

示例：
```json
{
  "/api/login": { "cases": [
    { "username": "111", "password": "X123456789" },
    { "username": "222", "password": "X123456789" }
  ]},
  "/inquiry/items-tab-query": [ { "item": "1" }, { "item": "2" } ]
}
```
- 说明：
  - 如果请求体本身是数组（如 `/inquiry/items-tab-query`），保持数组写法即可
  - 同一路径包含多个用例时，测试会为每个用例生成一个 DynamicTest

### 可选：测试专用数据源/配置
- 如果你不希望测试依赖生产数据库，可以为测试 profile 提供独立配置，或引入 H2（test scope）并在测试上下文中覆盖 DataSource。
- 也可以在 CI 中使用 Docker 启动依赖的数据库后再运行上述测试。

