# Update Record

## 2026-04-12
- 新增优惠券模块：MySQL 新增 `coupon`、`user_coupon` 表；`orders` 表增加 `coupon_id`、`original_amount`、`discount_amount` 字段
- 后端新增优惠券接口：管理端发布+Redis预热、用户端Lua抢券+异步落库、下单用券校验与支付失败/取消回退
- 新增定时消费任务 `CouponClaimTask`：从 Redis 队列批量落库并更新库存余量
- 管理端前端新增“优惠券”页面：发布优惠券、查看库存与有效期
- 更新接口HTML文档：补充管理端/用户端优惠券相关接口

