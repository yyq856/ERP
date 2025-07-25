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


