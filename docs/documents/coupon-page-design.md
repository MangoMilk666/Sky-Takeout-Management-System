# 优惠券模块-页面设计说明（Desktop First）

## Global Styles（全局样式与设计令牌）
- Background：#0B1020（深色） / 卡片 #121A33
- Brand/Accent：#6D5EF7
- Success：#22C55E；Warning：#F59E0B；Danger：#EF4444
- Typography：
  - H1 24/32 Semibold；H2 18/28 Semibold；Body 14/22 Regular；Caption 12/18
- Buttons：
  - Primary：#6D5EF7（hover #5B4BE6，disabled 40% opacity）
  - Secondary：描边 #2B3566，hover 背景 #18224A
- Link：#8B85FF（hover underline）
- Spacing：8px 基准栅格；卡片内边距 16~20px
- Layout：内容最大宽度 1200px；两侧留白自适应

---

## Page 1：商家端-优惠券管理页
### Layout
- 采用「顶部导航 + 左侧菜单 + 右侧内容区」的 Dashboard 布局。
- 内容区使用 CSS Grid：上方筛选区（1 行）+ 下方列表区（自适应）。

### Meta Information
- title：优惠券管理 - 商家后台
- description：创建与发布优惠券模板，预热缓存并查看发放核销。

### Page Structure
1) Topbar：商家名称、环境标识、账号菜单
2) Sidebar：营销/优惠券（高亮）
3) Content Header：页面标题 +「新建优惠券」主按钮
4) Filters：状态、有效期、关键字、创建人（可选）
5) Table List：模板列表
6) Drawer/Modal：新建/编辑模板表单
7) Detail Panel：统计概览

### Sections & Components
- 模板列表表格（Table）
  - 列：标题、类型、门槛/面额、有效期、库存（剩余/总）、单人限领、状态、操作
  - 操作：编辑、发布、下线、查看统计
- 新建/编辑模板表单（Form）
  - 字段分组：基础信息、优惠规则、库存与限领、有效期、适用范围
  - 校验：必填、数值范围、折扣/面额互斥、valid_from < valid_to
- 发布确认弹窗（Confirm Modal）
  - 明示动作：发布后将触发 Redis 预热
  - 失败提示：显示错误原因与重试按钮
- 统计卡片（Cards）
  - 发放数、核销数、回退数、异常数

### Interaction States
- 发布按钮：发布中显示 loading；发布失败显示可复制错误码
- 表格空态：提示“暂无优惠券，去创建”

---

## Page 2：用户端-领券中心页
### Layout
- 采用「顶部轻导航 + 内容居中」的单列布局。
- 券列表为 Card Grid：桌面端 3 列（>=1200），2 列（>=900），1 列（<900）。

### Meta Information
- title：领券中心
- description：浏览并领取可用优惠券，查看我的优惠券。
- Open Graph：og:title=领券中心；og:type=website

### Page Structure
1) Header：标题 +「我的优惠券」入口（右侧按钮）
2) Tabs：可领取 / 我的优惠券（也可使用独立路由，但保持最少页面）
3) Coupon Card Grid：券卡片列表
4) Toast：领取结果提示（成功/失败原因）

### Sections & Components
- 券卡片（CouponCard）
  - 信息：标题、面额/折扣、门槛、有效期、适用范围摘要
  - 库存提示：如“剩余紧张/已抢光”
  - CTA：领取按钮（disabled 状态）
- 领取交互
  - 点击后立即进入 loading
  - 返回码映射：
    - OUT_OF_STOCK：已抢光
    - EXCEED_LIMIT：已达限领
    - NOT_ACTIVE：未到领取时间/已结束
    - OK：领取成功（提示“可能稍后出现在我的优惠券”）
- 我的优惠券列表
  - 分组：可用/已锁定/已使用/已过期
  - 可用券提供「去使用」按钮跳转下单页

---

## Page 3：下单/收银台页（用券与回退）
### Layout
- 典型结算两栏：左侧订单明细（占 8/12），右侧支付/优惠（占 4/12）。
- 使用 Flexbox + sticky 右侧摘要卡。

### Meta Information
- title：确认订单
- description：选择优惠券并完成支付。

### Page Structure
1) Order Items：商品清单、配送信息
2) Coupon Selector：可用券推荐与选择
3) Price Summary：原价、优惠、应付
4) Pay Actions：支付按钮与支付状态反馈

### Sections & Components
- Coupon Selector（下拉/抽屉）
  - 默认推荐最优券（按优惠金额/折扣与可用性）
  - 选择后触发“锁券”请求，成功则更新价格
  - 锁券失败提示：券不可用/已被使用/已过期
- 支付状态处理
  - 支付成功：提示“已核销优惠券”并跳转订单详情
  - 支付失败/取消/超时：触发回退接口，提示“优惠券已释放，可重新使用”

### Interaction States
- 锁券中：禁用支付按钮，防止价格与状态不一致
- 异常兜底：若回退失败，提示“稍后自动恢复”，并建议刷新查看