# LikeBoom - 高并发点赞系统

LikeBoom是一个基于Spring Boot 3 + Java 21构建的高并发点赞系统，适用于社交平台、内容社区等场景。项目从基础功能开发到高并发优化再到企业级高可用架构，完整覆盖了点赞系统的核心技术实现。

## 技术栈

- **基础框架**：Spring Boot 3.4.4
- **Java版本**：Java 21
- **数据库**：MySQL/TiDB
- **缓存**：Redis + Caffeine多级缓存
- **消息队列**：Apache Pulsar
- **ORM框架**：MyBatis-Plus 3.5.11
- **认证授权**：Sa-Token 1.42.0
- **API文档**：Knife4j 4.6.0
- **工具类库**：Hutool 5.8.37
- **对象存储**：MinIO 8.5.14
- **热点数据识别**：HeavyKeeper算法
- **可观测性**：Prometheus + Grafana
- **容器化**：Docker

## 核心特性

- **多级缓存策略**：集成Caffeine本地缓存与Redis分布式缓存，构建高效的多级缓存架构
- **消息队列削峰**：利用Pulsar消息队列实现异步处理，有效应对高并发流量
- **分布式数据库**：支持TiDB分布式数据库，提供水平扩展能力
- **热点数据识别**：基于HeavyKeeper算法实现热点数据智能识别，提高缓存命中率
- **完整监控体系**：集成Prometheus + Grafana构建全方位系统监控
- **容器化部署**：提供Docker容器化部署方案，简化环境搭建

## 项目结构

```
likeboom
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── tenyon
│   │   │           └── lb
│   │   │               ├── MainApplication.java
│   │   │               ├── controller     # 控制器层
│   │   │               ├── service        # 服务层
│   │   │               ├── mapper         # 数据访问层
│   │   │               ├── domain         # 领域模型
│   │   │               ├── common         # 公共组件
│   │   │               ├── core           # 核心功能
│   │   │               └── oss            # 对象存储服务
│   │   └── resources
│   │       ├── mapper                     # MyBatis XML映射文件
│   │       ├── application.yml            # 应用配置
│   │       ├── application-local.properties # 本地环境配置
│   │       ├── logback-spring.xml         # 日志配置
│   │       └── banner.txt                 # 启动Banner
│   └── test                               # 测试目录
├── pom.xml                                # 项目依赖
└── README.md                              # 项目文档
```

## 功能模块

- 点赞/取消点赞接口
- 获取点赞数接口
- 获取用户点赞列表
- 点赞实时计数与定时持久化
- 系统监控与性能指标

## 快速开始

### 本地开发
1. 克隆项目
```bash
# 构建项目
git clone https://github.com/tenyon61/likeboom.git
cd likeboom
```
2. 根据application.yml添加application-local.properties

### Docker部署

```bash
# 构建Docker镜像
docker build -t likeboom:1.0.0 .

# 运行容器
docker run -d -p 8080:8072 --name likeboom likeboom:1.0.0
```

更多配置选项和部署方案请参考项目详细文档。

## 贡献指南
欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证
本项目采用 MIT 许可证，详情请参见 LICENSE 文件。

## 联系方式
如有任何问题或建议，请通过以下方式联系我们：

## 提交 Issue
发送邮件至：[tenyon@cqbo.com]

## 致谢
感谢所有为本项目做出贡献的开发者。