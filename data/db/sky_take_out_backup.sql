/*
 Navicat Premium Data Transfer

 Source Server         : local-dev
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : sky_take_out

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 14/04/2026 01:07:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address_book
-- ----------------------------
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `consignee` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细地址',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='地址簿';

-- ----------------------------
-- Records of address_book
-- ----------------------------
BEGIN;
INSERT INTO `address_book` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (2, 4, 'SS', '0', '15857991234', '33', '浙江省', '3301', '杭州市', '330106', '西湖区', '杭州大学清溪1-501', '3', 0);
INSERT INTO `address_book` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES (3, 4, '陈曦', '1', '13089011234', '11', '北京市', '1101', '市辖区', '110108', '海淀区', '北京大学竹园1舍205', '1', 1);
COMMIT;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int DEFAULT NULL COMMENT '类型   1 菜品分类 2 套餐分类',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '顺序',
  `status` int DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='菜品及套餐分类';

-- ----------------------------
-- Records of category
-- ----------------------------
BEGIN;
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (11, 1, '酒水饮料', 10, 1, '2022-06-09 22:09:18', '2022-06-09 22:09:18', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (12, 1, '传统主食', 9, 1, '2022-06-09 22:09:32', '2022-06-09 22:18:53', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (13, 2, '人气套餐', 12, 1, '2022-06-09 22:11:38', '2022-06-10 11:04:40', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (15, 2, '商务套餐', 13, 1, '2022-06-09 22:14:10', '2022-06-10 11:04:48', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (16, 1, '蜀味烤鱼', 4, 1, '2022-06-09 22:15:37', '2026-04-12 14:26:58', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (17, 1, '蜀味牛蛙', 5, 1, '2022-06-09 22:16:14', '2022-08-31 14:39:44', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (18, 1, '特色蒸菜', 6, 1, '2022-06-09 22:17:42', '2022-06-09 22:17:42', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (19, 1, '新鲜时蔬', 7, 1, '2022-06-09 22:18:12', '2022-06-09 22:18:28', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (20, 1, '水煮鱼', 8, 1, '2022-06-09 22:22:29', '2022-06-09 22:23:45', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (21, 1, '汤类', 11, 1, '2022-06-10 10:51:47', '2022-06-10 10:51:47', 1, 1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (23, 1, '西式简餐', 14, 1, '2026-02-01 15:30:39', '2026-02-01 15:31:34', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券名称',
  `discount_type` tinyint NOT NULL DEFAULT '1' COMMENT '折扣类型：1-满减金额，2-折扣比例',
  `discount` decimal(10,2) NOT NULL COMMENT '折扣值：满减金额(元) 或 折扣比例(如0.80)',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '优惠券发布总量',
  `remained_count` int NOT NULL DEFAULT '0' COMMENT '优惠券剩余数量',
  `begin_time` datetime DEFAULT NULL COMMENT '有效期开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '有效期结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_begin_end_time` (`begin_time`,`end_time`) COMMENT '有效期索引'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券定义表';

-- ----------------------------
-- Records of coupon
-- ----------------------------
BEGIN;
INSERT INTO `coupon` (`id`, `name`, `discount_type`, `discount`, `total_count`, `remained_count`, `begin_time`, `end_time`, `create_time`, `update_time`) VALUES (1, '测试八折券', 1, 0.80, 3, 2, '2026-04-13 00:00:00', '2026-04-16 23:59:59', '2026-04-13 11:47:03', '2026-04-13 18:36:07');
INSERT INTO `coupon` (`id`, `name`, `discount_type`, `discount`, `total_count`, `remained_count`, `begin_time`, `end_time`, `create_time`, `update_time`) VALUES (2, '测试直减券', 2, 10.00, 3, 2, '2026-04-13 00:00:00', '2026-04-16 23:00:00', '2026-04-13 16:06:50', '2026-04-13 18:36:03');
COMMIT;

-- ----------------------------
-- Table structure for dish
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '菜品名称',
  `category_id` bigint NOT NULL COMMENT '菜品分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品价格',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `status` int DEFAULT '1' COMMENT '0 停售 1 起售',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dish_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='菜品';

-- ----------------------------
-- Records of dish
-- ----------------------------
BEGIN;
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (46, '王老吉', 11, 6.00, 'http://localhost:8080/admin/common/upload/e22c1ffe09294ce593582d8e3e974aff.jpg', '', 1, '2022-06-09 22:40:47', '2026-02-01 15:33:52', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (47, '北冰洋', 11, 4.00, 'http://localhost:8080/admin/common/upload/12e6f6b4abf7452a84dfac774d699823.jpg', '还是小时候的味道', 1, '2022-06-10 09:18:49', '2026-02-01 12:28:58', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (48, '雪花啤酒', 11, 4.00, 'http://localhost:8080/admin/common/upload/2e4ed6a29a304435a62eff2af4de25a7.jpg', '', 1, '2022-06-10 09:22:54', '2026-02-01 15:34:07', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (49, '米饭', 12, 2.00, 'http://localhost:8080/admin/common/upload/af7eaf4b899f48c3b84935231ecacb5d.jpg', '精选五常大米', 1, '2022-06-10 09:30:17', '2026-02-01 12:28:46', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (50, '馒头', 12, 1.00, 'http://localhost:8080/admin/common/upload/74319127e2114faaa3a3041da03658ae.jpg', '优质面粉', 1, '2022-06-10 09:34:28', '2026-02-01 12:28:34', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (51, '老坛酸菜鱼', 20, 56.00, 'http://localhost:8080/admin/common/upload/4fac3680d82b4c178404a91582ced5e9.jpg', '原料：汤，草鱼，酸菜', 1, '2022-06-10 09:40:51', '2026-02-01 15:37:00', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (52, '经典酸菜鮰鱼', 20, 66.00, 'http://localhost:8080/admin/common/upload/c592c9143d21406cb9f4dc21c67d0652.jpg', '原料：酸菜，江团，鮰鱼', 1, '2022-06-10 09:46:02', '2026-02-01 15:36:50', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (53, '蜀味水煮草鱼', 20, 38.00, 'http://localhost:8080/admin/common/upload/d42dbe56c08545dfa708a3d6de048b89.jpg', '原料：草鱼，汤', 1, '2022-06-10 09:48:37', '2026-02-01 15:36:38', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (54, '清炒小油菜', 19, 18.00, 'http://localhost:8080/admin/common/upload/4ca1da768a144123a0fb1e252ab7efb6.jpeg', '原料：小油菜', 1, '2022-06-10 09:51:46', '2026-02-01 15:34:35', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (55, '蒜蓉娃娃菜', 19, 18.00, 'http://localhost:8080/admin/common/upload/7d64f35f98d14a868275b72fb1cde8d8.jpg', '原料：蒜，娃娃菜', 1, '2022-06-10 09:53:37', '2026-03-22 23:50:45', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (56, '清炒西兰花', 19, 18.00, 'http://localhost:8080/admin/common/upload/94d274a2ddad4da89fd2264e95e4f1bb.jpg', '原料：西兰花', 1, '2022-06-10 09:55:44', '2026-02-01 15:34:47', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (57, '炝炒圆白菜', 19, 18.00, 'http://localhost:8080/admin/common/upload/0806e0c62e384f6cbc301751010d2a7c.jpg', '原料：圆白菜', 1, '2022-06-10 09:58:35', '2026-02-01 15:34:18', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (58, '清蒸鲈鱼', 18, 98.00, 'http://localhost:8080/admin/common/upload/498f6c6cb2044bf682a402f6fa21d6d1.jpg', '原料：鲈鱼', 1, '2022-06-10 10:12:28', '2026-02-01 15:35:56', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (59, '东坡肘子', 18, 138.00, 'http://localhost:8080/admin/common/upload/c073c4721d6a45179642bb8ea86657a2.jpg', '原料：猪肘棒', 1, '2022-06-10 10:24:03', '2026-02-01 15:35:16', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (60, '梅菜扣肉', 18, 58.00, 'http://localhost:8080/admin/common/upload/09b27f1f39604a08afef33652b904920.jpg', '原料：猪肉，梅菜', 1, '2022-06-10 10:26:03', '2026-02-01 12:28:20', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (61, '剁椒鱼头', 18, 66.00, 'http://localhost:8080/admin/common/upload/e642e508ccec42d7941069eaea398da0.jpg', '原料：鲢鱼，剁椒', 1, '2022-06-10 10:28:54', '2026-02-01 15:35:29', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (62, '金汤酸菜牛蛙', 17, 89.00, 'http://localhost:8080/admin/common/upload/0dfbbfba36db4bb189bfa7f4390a3bb1.jpg', '原料：鲜活牛蛙，酸菜', 1, '2022-06-10 10:33:05', '2026-02-01 15:35:46', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (63, '香锅牛蛙', 17, 88.00, 'http://localhost:8080/admin/common/upload/1c815571fc2948018c3a37c8148c1f1b.jpeg', '配料：鲜活牛蛙，莲藕，青笋', 1, '2022-06-10 10:35:40', '2026-02-01 12:28:04', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (64, '馋嘴牛蛙', 17, 88.00, 'http://localhost:8080/admin/common/upload/a9b01326e82a4d58bb6e016c204649ae.jpeg', '配料：鲜活牛蛙，丝瓜，黄豆芽', 1, '2022-06-10 10:37:52', '2026-02-01 15:36:23', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (65, '草鱼2斤', 16, 68.00, 'http://localhost:8080/admin/common/upload/c9c94434775a4cd48cdad47e6e62d1e6.jpg', '原料：草鱼，黄豆芽，莲藕', 1, '2022-06-10 10:41:08', '2026-02-01 15:36:10', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (66, '江团鱼2斤', 16, 109.00, 'http://localhost:8080/admin/common/upload/aff37674ad27409d9597995ebe6af000.jpg', '配料：江团鱼，黄豆芽，莲藕', 1, '2022-06-10 10:42:42', '2026-02-01 12:27:50', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (67, '鮰鱼2斤', 16, 72.00, 'http://localhost:8080/admin/common/upload/655f0e750fc044c1b1d000936c1098dc.jpg', '原料：鮰鱼，黄豆芽，莲藕', 1, '2022-06-10 10:43:56', '2026-02-01 15:33:37', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (68, '鸡蛋汤', 21, 5.00, 'http://localhost:8080/admin/common/upload/1c84779b401b49e4bcb0dac1ce161fb5.jpg', '配料：鸡蛋，紫菜', 1, '2022-06-10 10:54:25', '2026-02-04 15:02:16', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (69, '平菇豆腐汤', 21, 6.00, 'http://localhost:8080/admin/common/upload/9a1d4d05855d4912bce3afc4109c735d.jpg', '配料：豆腐，平菇', 1, '2022-06-10 10:55:02', '2026-02-01 15:32:39', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (72, '旺仔牛奶', 11, 3.00, 'http://localhost:8080/admin/common/upload/cfc70805593e473b880f22ef1286329b.jpeg', '经典旺仔牛奶', 1, '2026-01-18 15:53:22', '2026-02-01 12:26:33', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (75, '美式牛肉汉堡', 23, 19.00, 'http://localhost:8080/admin/common/upload/3ec7337888f947799b19569dd03e6d75.jpeg', '牛肉汉堡', 1, '2026-02-01 12:34:32', '2026-02-01 15:32:00', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (77, '薯条', 23, 13.00, 'http://localhost:8080/admin/common/upload/0284ee3d5ca84db7aa03367d8aa00d94.jpeg', '现炸薯条，香浓酥脆', 1, '2026-02-01 15:45:40', '2026-02-01 15:45:45', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (78, '鸡米花', 23, 13.00, 'http://localhost:8080/admin/common/upload/6f2a2a35dfb2431bb824c512540d8c47.jpg', '酥香松脆鸡米花', 1, '2026-02-07 16:59:18', '2026-02-07 17:05:10', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (79, '鸡肉卷', 23, 19.90, 'http://localhost:8080/admin/common/upload/6c4aeb624ca54aa4aabf062ad3facc4a.jpg', '风味鸡肉卷', 1, '2026-02-07 17:01:29', '2026-02-07 17:04:36', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (80, '那不勒斯披萨', 23, 29.90, 'http://localhost:8080/admin/common/upload/61022b7e5ff34bb3bf67ef6a829d045a.jpeg', '经典那不勒斯披萨，由番茄、香肠、罗勒叶调味', 1, '2026-02-07 17:03:30', '2026-02-07 17:04:06', 1, 1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (81, '可口可乐', 11, 4.50, 'http://localhost:8080/admin/common/upload/baed66d03e444133bbac463843a41b1a.jpg', '原味可乐', 1, '2026-02-07 17:07:26', '2026-02-07 17:07:35', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for dish_flavor
-- ----------------------------
DROP TABLE IF EXISTS `dish_flavor`;
CREATE TABLE `dish_flavor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint NOT NULL COMMENT '菜品',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '口味名称',
  `value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '口味数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='菜品口味关系表';

-- ----------------------------
-- Records of dish_flavor
-- ----------------------------
BEGIN;
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (40, 10, '甜味', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (41, 7, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (42, 7, '温度', '[\"热饮\",\"常温\",\"去冰\",\"少冰\",\"多冰\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (45, 6, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (46, 6, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (47, 5, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (48, 5, '甜味', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (49, 2, '甜味', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (50, 4, '甜味', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (51, 3, '甜味', '[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (52, 3, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (117, 72, '温度', '[\"热饮\",\"常温\",\"少冰\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (118, 55, '忌口', '[\"不要蒜\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (119, 66, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (120, 63, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (121, 60, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (123, 75, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (124, 67, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (125, 57, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (126, 54, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (127, 56, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (128, 65, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (129, 65, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (130, 64, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (131, 53, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (132, 53, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (133, 52, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (134, 52, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (135, 51, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (136, 51, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (137, 78, '辣度', '[\"不辣\",\"微辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (138, 79, '忌口', '[\"不要葱\",\"不要蒜\",\"不要香菜\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (139, 79, '辣度', '[\"不辣\",\"微辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (140, 81, '温度', '[\"常温\",\"少冰\"]');
COMMIT;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='员工信息';

-- ----------------------------
-- Records of employee
-- ----------------------------
BEGIN;
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812312312', '1', '110101199001010047', 1, '2022-02-15 15:51:20', '2022-02-17 09:16:20', 10, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (2, '花子欣', 'huazixin', 'e10adc3949ba59abbe56e057f20f883e', '13812312313', '1', '110101199001010048', 1, '2026-01-12 21:10:13', '2026-01-12 21:10:13', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (3, '李四', 'lisi', 'e10adc3949ba59abbe56e057f20f883e', '13812312314', '0', '110101199001010049', 1, '2026-01-12 21:54:38', '2026-01-12 21:54:38', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (4, '张三', 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '13812312315', '1', '110101199001010050', 1, '2026-01-12 21:55:27', '2026-01-12 21:55:27', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (7, '王五', 'wangwu', 'e10adc3949ba59abbe56e057f20f883e', '13812312316', '1', '110101199001010051', 1, '2026-01-12 22:44:36', '2026-01-13 12:28:37', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (8, '宋江', 'songjiang', 'e10adc3949ba59abbe56e057f20f883e', '16957044273', '1', '110101199001010052', 1, '2026-01-12 23:29:36', '2026-01-13 14:58:19', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (9, '彤义轩', 'tongyixuan', 'e10adc3949ba59abbe56e057f20f883e', '14581700199', '1', '110101199001010053', 0, '2026-01-12 23:30:22', '2026-01-13 12:32:26', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (10, '封国香', 'fengguoxiang', 'e10adc3949ba59abbe56e057f20f883e', '04712584494', '0', '110101199001010054', 1, '2026-01-12 23:30:53', '2026-01-12 23:30:53', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (11, '己明', 'jiming', 'e10adc3949ba59abbe56e057f20f883e', '15949486155', '1', '110101199001010055', 1, '2026-01-12 23:31:37', '2026-01-12 23:31:37', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (12, '圭紫林', 'guizilin', 'e10adc3949ba59abbe56e057f20f883e', '47287339679', '1', '110101199001010056', 1, '2026-01-12 23:32:00', '2026-01-12 23:32:00', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (13, '钱六', 'qianliu', 'e10adc3949ba59abbe56e057f20f883e', '17287339680', '1', '119101199001010057', 1, '2026-01-13 00:16:33', '2026-01-15 12:23:49', 1, 8);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (14, '赵七', 'zhaoqi', 'e10adc3949ba59abbe56e057f20f883e', '47287339681', '1', '110101199001010058', 1, '2026-01-13 00:17:05', '2026-01-13 00:17:05', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (15, '欧比旺', 'oubiwan', 'e10adc3949ba59abbe56e057f20f883e', '15888114514', '1', '110101199001010059', 1, '2026-01-13 00:19:14', '2026-01-13 14:57:23', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (16, '安纳金', 'anakin', 'e10adc3949ba59abbe56e057f20f883e', '47287339683', '1', '110101199001010060', 1, '2026-01-13 00:19:41', '2026-01-13 00:19:41', 1, 1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (17, '卢俊义', 'lujunyi', 'e10adc3949ba59abbe56e057f20f883e', '13961341234', '1', '110101199001010061', 1, '2026-01-15 12:23:31', '2026-01-15 12:23:31', 8, 8);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (18, '李逵', 'likui', 'e10adc3949ba59abbe56e057f20f883e', '13312341234', '1', '110101199001010062', 1, '2026-01-15 12:24:30', '2026-04-12 14:29:19', 8, 1);
COMMIT;

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '名字',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `dish_flavor` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '口味',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单明细表';

-- ----------------------------
-- Records of order_detail
-- ----------------------------
BEGIN;
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (5, '江团鱼2斤', 'http://localhost:8080/admin/common/upload/aff37674ad27409d9597995ebe6af000.jpg', 4, 66, NULL, '重辣', 1, 109.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (6, '清炒小油菜', 'http://localhost:8080/admin/common/upload/4ca1da768a144123a0fb1e252ab7efb6.jpeg', 4, 54, NULL, '不要蒜', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (7, '可口可乐', 'http://localhost:8080/admin/common/upload/baed66d03e444133bbac463843a41b1a.jpg', 4, 81, NULL, '少冰', 1, 4.50);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (8, '米饭', 'http://localhost:8080/admin/common/upload/af7eaf4b899f48c3b84935231ecacb5d.jpg', 5, 49, NULL, NULL, 1, 2.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (9, '西式套餐A', 'http://localhost:8080/admin/common/upload/68caae5f6ef74b7b9f9bef1c4ee4a792.jpg', 5, NULL, 34, NULL, 2, 49.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (10, '馋嘴牛蛙', 'http://localhost:8080/admin/common/upload/a9b01326e82a4d58bb6e016c204649ae.jpeg', 6, 64, NULL, '重辣', 1, 88.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (11, '米饭', 'http://localhost:8080/admin/common/upload/af7eaf4b899f48c3b84935231ecacb5d.jpg', 6, 49, NULL, NULL, 1, 2.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (12, '经典酸菜鮰鱼', 'http://localhost:8080/admin/common/upload/c592c9143d21406cb9f4dc21c67d0652.jpg', 7, 52, NULL, '不要香菜,微辣', 1, 66.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (13, '可口可乐', 'http://localhost:8080/admin/common/upload/baed66d03e444133bbac463843a41b1a.jpg', 7, 81, NULL, '少冰', 1, 4.50);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (14, '米饭', 'http://localhost:8080/admin/common/upload/af7eaf4b899f48c3b84935231ecacb5d.jpg', 7, 49, NULL, NULL, 1, 2.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (15, '炝炒圆白菜', 'http://localhost:8080/admin/common/upload/0806e0c62e384f6cbc301751010d2a7c.jpg', 7, 57, NULL, '不要辣', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (16, '梅菜扣肉', 'http://localhost:8080/admin/common/upload/09b27f1f39604a08afef33652b904920.jpg', 8, 60, NULL, '不要香菜', 1, 58.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (17, '雪花啤酒', 'http://localhost:8080/admin/common/upload/2e4ed6a29a304435a62eff2af4de25a7.jpg', 8, 48, NULL, NULL, 1, 4.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (18, '清炒西兰花', 'http://localhost:8080/admin/common/upload/94d274a2ddad4da89fd2264e95e4f1bb.jpg', 8, 56, NULL, '不要蒜', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (19, '馒头', 'http://localhost:8080/admin/common/upload/74319127e2114faaa3a3041da03658ae.jpg', 8, 50, NULL, NULL, 2, 1.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (20, '西式套餐A', 'http://localhost:8080/admin/common/upload/68caae5f6ef74b7b9f9bef1c4ee4a792.jpg', 9, NULL, 34, NULL, 1, 49.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (21, '清炒小油菜', 'http://localhost:8080/admin/common/upload/4ca1da768a144123a0fb1e252ab7efb6.jpeg', 9, 54, NULL, '不要蒜', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (24, '江团鱼2斤', 'http://localhost:8080/admin/common/upload/aff37674ad27409d9597995ebe6af000.jpg', 11, 66, NULL, '重辣', 1, 109.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (25, '清炒小油菜', 'http://localhost:8080/admin/common/upload/4ca1da768a144123a0fb1e252ab7efb6.jpeg', 11, 54, NULL, '不要蒜', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (26, '可口可乐', 'http://localhost:8080/admin/common/upload/baed66d03e444133bbac463843a41b1a.jpg', 11, 81, NULL, '少冰', 1, 4.50);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (27, '清炒西兰花', 'http://localhost:8080/admin/common/upload/94d274a2ddad4da89fd2264e95e4f1bb.jpg', 12, 56, NULL, '不要葱', 1, 18.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (28, '香锅牛蛙', 'http://localhost:8080/admin/common/upload/1c815571fc2948018c3a37c8148c1f1b.jpeg', 12, 63, NULL, '重辣', 1, 88.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (29, '米饭', 'http://localhost:8080/admin/common/upload/af7eaf4b899f48c3b84935231ecacb5d.jpg', 12, 49, NULL, NULL, 2, 2.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (30, '商务单人餐', 'http://localhost:8080/admin/common/upload/edd311068e5446538998bc9048cdfad7.jpg', 13, NULL, 32, NULL, 1, 29.90);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (31, '商务单人餐', 'http://localhost:8080/admin/common/upload/edd311068e5446538998bc9048cdfad7.jpg', 14, NULL, 32, NULL, 1, 29.90);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (32, '梅菜扣肉', 'http://localhost:8080/admin/common/upload/09b27f1f39604a08afef33652b904920.jpg', 15, 60, NULL, '不要香菜', 1, 58.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (33, '清蒸鲈鱼', 'http://localhost:8080/admin/common/upload/498f6c6cb2044bf682a402f6fa21d6d1.jpg', 15, 58, NULL, NULL, 1, 98.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (34, '经典酸菜鮰鱼', 'http://localhost:8080/admin/common/upload/c592c9143d21406cb9f4dc21c67d0652.jpg', 16, 52, NULL, '不要葱,重辣', 1, 66.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (35, '江团鱼2斤', 'http://localhost:8080/admin/common/upload/aff37674ad27409d9597995ebe6af000.jpg', 16, 66, NULL, '重辣', 1, 109.00);
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES (36, '江团鱼2斤', 'http://localhost:8080/admin/common/upload/aff37674ad27409d9597995ebe6af000.jpg', 17, 66, NULL, '不辣', 1, 109.00);
COMMIT;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单号',
  `status` int NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `address_book_id` bigint NOT NULL COMMENT '地址id',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `original_amount` decimal(10,2) NOT NULL COMMENT '订单原价',
  `coupon_id` bigint DEFAULT NULL COMMENT '订单使用优惠券id',
  `discount_amount` decimal(10,2) NOT NULL COMMENT '优惠后金额',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '地址',
  `user_name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `delivery_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '配送状态  1立即送出  0选择具体时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `pack_amount` int DEFAULT NULL COMMENT '打包费',
  `tableware_number` int DEFAULT NULL COMMENT '餐具数量',
  `tableware_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '餐具数量状态  1按餐量提供  0选择具体数量',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '实付金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='订单表';

-- ----------------------------
-- Records of orders
-- ----------------------------
BEGIN;
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (4, '1772383013615', 6, 4, 2, '2026-03-02 00:36:54', NULL, 1, 0, 140.50, NULL, 140.50, '', '15857991234', '杭州大学清溪1-501', 'SS', 'SS', '订单超时，自动取消', NULL, '2026-03-19 01:18:00', '2026-03-02 01:35:00', 0, NULL, 3, 1, 0, 140.50);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (5, '1772383441537', 6, 4, 2, '2026-03-02 00:44:02', NULL, 1, 0, 109.00, NULL, 109.00, 'remark测试', '15857991234', '杭州大学清溪1-501', NULL, 'SS', '订单超时，自动取消', NULL, '2026-03-19 01:18:00', '2026-03-02 01:43:00', 0, NULL, 3, 0, 0, 109.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (6, '1773070264563', 6, 4, 2, '2026-03-09 23:31:05', NULL, 1, 0, 98.00, NULL, 98.00, '', '15857991234', '杭州大学清溪1-501', NULL, 'SS', '订单超时，自动取消', NULL, '2026-03-19 01:18:00', '2026-03-09 00:30:00', 0, NULL, 2, 0, 0, 98.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (7, '1773071879528', 5, 4, 2, '2026-03-09 23:58:00', '2026-03-09 23:58:18', 1, 1, 100.50, NULL, 100.50, '请小哥放在外卖柜里', '15857991234', '杭州大学清溪1-501', NULL, 'SS', NULL, NULL, NULL, '2026-03-09 00:57:00', 0, '2026-03-19 01:17:36', 4, 0, 0, 100.50);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (8, '1773576687859', 5, 4, 2, '2026-03-15 20:11:28', '2026-03-15 20:11:32', 1, 1, 93.00, NULL, 93.00, '', '15857991234', '杭州大学清溪1-501', NULL, 'SS', NULL, NULL, NULL, '2026-03-15 21:11:00', 0, '2026-03-19 01:17:36', 5, 1, 0, 93.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (9, '1773577243147', 5, 4, 3, '2026-03-15 20:20:43', '2026-03-15 20:20:53', 1, 1, 75.00, NULL, 75.00, '测试，跳过支付校验逻辑', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, NULL, '2026-03-15 21:20:00', 0, '2026-03-19 01:17:36', 2, 0, 0, 75.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (11, '1773578876350', 5, 4, 3, '2026-03-15 20:47:56', '2026-03-15 20:47:58', 1, 1, 140.50, NULL, 140.50, '', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, '2026-03-15 20:48:05', '2026-03-15 21:47:00', 0, '2026-03-19 01:17:36', 3, 0, 0, 140.50);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (12, '1774166847693', 4, 4, 3, '2026-03-22 16:07:28', '2026-03-22 16:07:30', 1, 1, 120.00, NULL, 120.00, '测试111', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, NULL, '2026-03-22 17:07:00', 0, NULL, 4, 2, 0, 120.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (13, '1774167002457', 4, 4, 2, '2026-03-22 16:10:02', '2026-03-22 16:10:05', 1, 1, 36.90, NULL, 36.90, '重辣！！！', '15857991234', '杭州大学清溪1-501', NULL, 'SS', NULL, NULL, NULL, '2026-03-22 20:00:00', 0, NULL, 1, 1, 0, 36.90);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (14, '1774167707205', 4, 4, 2, '2026-03-22 16:21:47', '2026-03-22 16:21:49', 1, 1, 36.90, NULL, 36.90, '', '15857991234', '杭州大学清溪1-501', NULL, 'SS', NULL, NULL, NULL, '2026-03-22 17:21:00', 0, NULL, 1, 0, 0, 36.90);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (15, '1776070073934', 1, 4, 3, '2026-04-13 16:47:54', NULL, 1, 0, 164.00, NULL, 164.00, '', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, NULL, '2026-04-13 17:47:00', 0, NULL, 2, 0, 0, 164.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (16, '1776072499560', 2, 4, 3, '2026-04-13 17:28:20', '2026-04-13 17:28:22', 1, 1, 183.00, NULL, 183.00, '', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, NULL, '2026-04-13 18:28:00', 0, NULL, 2, 0, 0, 183.00);
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `original_amount`, `coupon_id`, `discount_amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`, `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`, `amount`) VALUES (17, '1776077053027', 2, 4, 3, '2026-04-13 18:44:13', '2026-04-13 18:44:15', 1, 1, 92.80, 1, 74.24, '', '13089011234', '北京大学竹园1舍205', NULL, '陈曦', NULL, NULL, NULL, '2026-04-13 19:43:00', 0, NULL, 1, 0, 0, 74.24);
COMMIT;

-- ----------------------------
-- Table structure for setmeal
-- ----------------------------
DROP TABLE IF EXISTS `setmeal`;
CREATE TABLE `setmeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '菜品分类id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '套餐价格',
  `status` int DEFAULT '1' COMMENT '售卖状态 0:停售 1:起售',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '描述信息',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_setmeal_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐';

-- ----------------------------
-- Records of setmeal
-- ----------------------------
BEGIN;
INSERT INTO `setmeal` (`id`, `category_id`, `name`, `price`, `status`, `description`, `image`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (1, 13, '假日双人餐', 108.00, 1, '双人套餐，包含一荤一素，米饭，饮料', 'http://localhost:8080/admin/common/upload/dcc28fb79ad545f4bf523c2eef9ce862.jpeg', '2026-01-18 22:18:30', '2026-02-01 15:48:34', 1, 1);
INSERT INTO `setmeal` (`id`, `category_id`, `name`, `price`, `status`, `description`, `image`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (32, 15, '商务单人餐', 29.90, 1, '更适合上班族的单人套餐', 'http://localhost:8080/admin/common/upload/edd311068e5446538998bc9048cdfad7.jpg', '2026-01-20 11:24:04', '2026-02-07 16:50:57', 1, 1);
INSERT INTO `setmeal` (`id`, `category_id`, `name`, `price`, `status`, `description`, `image`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (34, 13, '西式套餐A', 49.00, 1, '双人套餐A', 'http://localhost:8080/admin/common/upload/68caae5f6ef74b7b9f9bef1c4ee4a792.jpg', '2026-02-07 17:12:01', '2026-04-12 14:25:30', 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for setmeal_dish
-- ----------------------------
DROP TABLE IF EXISTS `setmeal_dish`;
CREATE TABLE `setmeal_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品id',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '菜品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品单价（冗余字段）',
  `copies` int DEFAULT NULL COMMENT '菜品份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='套餐菜品关系';

-- ----------------------------
-- Records of setmeal_dish
-- ----------------------------
BEGIN;
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (59, 33, 67, '鮰鱼2斤', 72.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (63, 1, 54, '清炒小油菜', 18.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (64, 1, 49, '米饭', 2.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (65, 1, 46, '王老吉', 6.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (66, 1, 72, '旺仔牛奶', 3.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (67, 1, 62, '金汤酸菜牛蛙', 89.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (68, 32, 54, '清炒小油菜', 18.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (69, 32, 49, '米饭', 2.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (70, 32, 48, '雪花啤酒', 4.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (71, 32, 60, '梅菜扣肉', 58.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (76, 34, 75, '美式牛肉汉堡', 19.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (77, 34, 77, '薯条', 13.00, 1);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (78, 34, 81, '可口可乐', 4.50, 2);
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES (79, 34, 79, '鸡肉卷', 19.90, 1);
COMMIT;

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint NOT NULL COMMENT '主键',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品id',
  `setmeal_id` bigint DEFAULT NULL COMMENT '套餐id',
  `dish_flavor` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '口味',
  `number` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='购物车';

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '微信用户唯一标识',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='用户信息';

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`) VALUES (4, 'olWUy3bttM2SSgHcXo9YuUozW8n8', NULL, NULL, NULL, NULL, NULL, '2026-01-30 17:34:21');
COMMIT;

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未用，1-已用，2-过期',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求幂等ID（防重复领取）',
  `create_time` datetime DEFAULT NULL COMMENT '领取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`) USING BTREE COMMENT '幂等唯一索引',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户查询索引',
  KEY `idx_coupon_id` (`coupon_id`) USING BTREE COMMENT '优惠券查询索引'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户领券记录表';

-- ----------------------------
-- Records of user_coupon
-- ----------------------------
BEGIN;
INSERT INTO `user_coupon` (`id`, `user_id`, `coupon_id`, `status`, `used_time`, `request_id`, `create_time`) VALUES (1, 4, 2, 0, NULL, '1776076563811_3a16c2e3b164b', '2026-04-13 18:36:04');
INSERT INTO `user_coupon` (`id`, `user_id`, `coupon_id`, `status`, `used_time`, `request_id`, `create_time`) VALUES (2, 4, 1, 1, '2026-04-13 18:44:13', '1776076566847_127e2571bc78c', '2026-04-13 18:36:07');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
