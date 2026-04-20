
# Smart-Dining

## 项目简介

Smart-Dining 是一个外卖/点餐业务的后端服务项目，涵盖用户端下单流程、管理端运营与数据统计等能力，**基于开源外卖项目构建**。

## 主要功能

用户端：
- 微信登录：对接微信登录流程，登录后签发用户端 JWT
- 分类/菜品/套餐浏览：按分类展示菜品与套餐信息
- 购物车：加入/移除/清空购物车
- 下单：提交订单、查看订单详情与历史订单
- 订单支付：预留微信支付能力，对接支付回调
- 优惠券领取：配合Redis中间件，保证用户高并发抢券的可用性
- 优惠券查询：查询可领取优惠券、查看我的可用优惠券
- 地址簿：新增/修改/删除/设置默认地址
- 催单：用户催单触发服务端通知逻辑

管理端：
- 员工登录：基于 JWT 的管理端鉴权
- 员工管理：新增/编辑/分页查询、启用/禁用账号
- 分类管理：菜品/套餐分类管理
- 菜品管理：菜品新增/编辑/上下架
- 套餐管理：套餐新增/编辑/上下架
- 订单管理：订单搜索，展示接单/拒单/取消、派送/完成状态
- 门店营业状态：门店开/打烊状态切换与查询
- 工作台：展示今日数据，包括订单/菜品/套餐概览
- 数据统计：展示营业额/用户/订单统计，销量 Top10
- 文件上传：提供上传接口
- 优惠券发布：发布优惠券并初始化库存，管理当前发布的优惠券

异步与通知：
- 抢券持久化削峰：抢券成功后写入 Redis 队列，异步消费落库并扣减数据库库存
- WebSocket 推送：提供 WebSocket 服务端能力，用于消息通知（包含定时推送）

## 技术栈

- Java / Spring Boot 2.7.3
- Spring MVC / Spring AOP
- MyBatis + PageHelper
- MySQL
- Redis（Spring Data Redis）+ Lua 脚本 + Spring Cache
- JWT（jjwt）
- WebSocket（spring-boot-starter-websocket + javax.websocket）
- Knife4j（Swagger/接口文档）
- Druid 连接池
- 微信支付 API v3（wechatpay-apache-httpclient）+ Apache HttpClient
- Fastjson
- Apache POI（报表/导出能力）
- Maven 多模块工程
- 测试数据生成：JavaFaker（sky-data-generator）

## 项目结构

- `backend/`：后端 Maven 多模块工程
  - `sky-server`：核心业务服务（Controller/Service/Mapper）
  - `sky-common`：通用工具、常量、异常、配置
  - `sky-pojo`：实体/DTO/VO 等模型定义
  - `sky-data-generator`：测试数据生成工具
- `docs/`：接口文档、原型与设计文档
- `data/db/`：数据库备份/初始化脚本
