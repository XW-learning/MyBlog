/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80039
 Source Host           : localhost:3306
 Source Schema         : blog_system

 Target Server Type    : MySQL
 Target Server Version : 80039
 File Encoding         : 65001

 Date: 06/12/2025 22:36:53
*/

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `blog_system`
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

-- 使用数据库
USE `blog_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_article
-- ----------------------------
DROP TABLE IF EXISTS `t_article`;
CREATE TABLE `t_article`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `user_id` bigint(0) NOT NULL COMMENT '作者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章正文(富文本/Markdown)',
  `views` int(0) NULL DEFAULT 0 COMMENT '浏览量',
  `likes` int(0) NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发布时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '最后更新时间',
  `category_id` bigint(0) NULL DEFAULT NULL COMMENT '文章所属分类ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `fk_article_category`(`category_id`) USING BTREE,
  CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_article
-- ----------------------------
INSERT INTO `t_article` VALUES (4, 1, 'Java 内存模型 (JMM) 详解：理解并发的基石', '深入剖析 Java 内存模型的工作原理，包括主内存与工作内存、volatile 关键字、happens-before 规则，以及如何避免缓存可见性问题，是编写高性能并发代码的关键。', '深入剖析 Java 内存模型的工作原理，包括主内存与工作内存、volatile 关键字、happens-before 规则，以及如何避免缓存可见性问题，是编写高性能并发代码的关键。', 163, 17, 1, '2025-12-06 00:29:18', '2025-12-06 21:51:38', 1);
INSERT INTO `t_article` VALUES (5, 1, 'Spring Boot 2.x 升级指南：从传统 XML 到全注解配置', '详细记录将传统 Spring 项目迁移至 Spring Boot 的步骤和配置要点。重点对比了 XML 配置与 Java Config 的优劣，并介绍了 Starter POM 的魔力。', '详细记录将传统 Spring 项目迁移至 Spring Boot 的步骤和配置要点。重点对比了 XML 配置与 Java Config 的优劣，并介绍了 Starter POM 的魔力。', 96, 12, 1, '2025-12-06 00:29:18', '2025-12-06 21:38:49', 1);
INSERT INTO `t_article` VALUES (6, 1, 'Servlet 生命周期与核心 API 实践', '本文总结了 Servlet 容器（如 Tomcat）如何管理 Servlet 的 init()、service() 和 destroy() 方法，并深度探讨了 RequestDispatchers 的使用。', '本文总结了 Servlet 容器（如 Tomcat）如何管理 Servlet 的 init()、service() 和 destroy() 方法，并深度探讨了 RequestDispatchers 的使用。', 154, 23, 1, '2025-12-06 00:29:18', '2025-12-06 12:15:08', 1);
INSERT INTO `t_article` VALUES (7, 1, 'RESTful API 设计规范：让你的接口更具可读性', '遵循 REST 原则的最佳实践，讨论了资源命名、HTTP 方法语义化（GET, POST, PUT, DELETE）以及状态码的正确使用，是前后端分离项目的必备知识。', '遵循 REST 原则的最佳实践，讨论了资源命名、HTTP 方法语义化（GET, POST, PUT, DELETE）以及状态码的正确使用，是前后端分离项目的必备知识。', 209, 47, 1, '2025-12-06 00:29:18', '2025-12-06 21:38:52', 1);
INSERT INTO `t_article` VALUES (8, 1, 'MySQL 索引优化：提高查询效率的 5 个实用技巧', '针对 B+ 树索引结构，分析了联合索引的最左匹配原则、覆盖索引的应用、以及如何通过 EXPLAIN 命令分析慢查询日志并进行优化。', '针对 B+ 树索引结构，分析了联合索引的最左匹配原则、覆盖索引的应用、以及如何通过 EXPLAIN 命令分析慢查询日志并进行优化。', 97, 19, 1, '2025-12-06 00:29:18', '2025-12-06 12:27:04', 1);
INSERT INTO `t_article` VALUES (9, 1, 'JWT (JSON Web Token) 在前后端分离中的应用与安全考量', '介绍 JWT 的结构（Header, Payload, Signature），演示了如何使用 JWT 实现无状态的身份验证，并探讨了 Token 刷新和黑名单机制的安全设计。', '介绍 JWT 的结构（Header, Payload, Signature），演示了如何使用 JWT 实现无状态的身份验证，并探讨了 Token 刷新和黑名单机制的安全设计。', 188, 30, 1, '2025-12-06 00:29:18', '2025-12-06 12:15:10', 1);
INSERT INTO `t_article` VALUES (10, 1, 'MyBatis 动态 SQL：解决复杂查询的利器', '详细讲解 <if>、<where>、<foreach> 等动态标签的使用场景，帮助开发者灵活构建 SQL 语句，避免手动拼接字符串带来的注入风险。', '详细讲解 <if>、<where>、<foreach> 等动态标签的使用场景，帮助开发者灵活构建 SQL 语句，避免手动拼接字符串带来的注入风险。', 111, 20, 1, '2025-12-06 00:29:18', '2025-12-06 12:15:13', 1);
INSERT INTO `t_article` VALUES (11, 1, 'Java 异常处理的艺术：Checked vs Unchecked 最佳实践', '对比了受检异常（Checked Exception）和非受检异常（Unchecked Exception）的设计哲学，并给出了在 Service 层和 Controller 层处理异常的建议。', '对比了受检异常（Checked Exception）和非受检异常（Unchecked Exception）的设计哲学，并给出了在 Service 层和 Controller 层处理异常的建议。', 65, 8, 1, '2025-12-06 00:29:18', '2025-12-06 12:15:11', 1);
INSERT INTO `t_article` VALUES (12, 1, 'Web 前端安全入门：XSS 与 CSRF 攻击的原理与防御', '针对 JavaWeb 应用，详细阐述了跨站脚本攻击 (XSS) 和跨站请求伪造 (CSRF) 的攻击流程，并提供了后端（Servlet/Filter）和前端的防御措施。', '针对 JavaWeb 应用，详细阐述了跨站脚本攻击 (XSS) 和跨站请求伪造 (CSRF) 的攻击流程，并提供了后端（Servlet/Filter）和前端的防御措施。', 136, 25, 1, '2025-12-06 00:29:18', '2025-12-06 21:39:33', 1);
INSERT INTO `t_article` VALUES (13, 1, '浅谈 Tomcat 调优：高并发下的线程池配置', '探讨 Tomcat 连接器（Connector）的工作模式，如何合理配置 maxThreads、acceptCount 等参数，以应对高并发场景，提高服务器的吞吐量和稳定性。', '探讨 Tomcat 连接器（Connector）的工作模式，如何合理配置 maxThreads、acceptCount 等参数，以应对高并发场景，提高服务器的吞吐量和稳定性。', 161, 35, 1, '2025-12-06 00:29:18', '2025-12-06 14:47:35', 1);
INSERT INTO `t_article` VALUES (16, 1, 'fasd ', 'asdf asd', 'asdf asd', 6, 1, 1, '2025-12-06 20:46:06', '2025-12-06 21:39:10', 3);
INSERT INTO `t_article` VALUES (18, 1, '发撒的发烧', '啊发水电费阿斯顿发asd', '啊发水电费阿斯顿发asd', 4, 0, 1, '2025-12-06 21:39:46', '2025-12-06 22:30:54', 5);

-- ----------------------------
-- Table structure for t_article_like
-- ----------------------------
DROP TABLE IF EXISTS `t_article_like`;
CREATE TABLE `t_article_like`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `article_id` bigint(0) NOT NULL COMMENT '文章ID',
  `user_id` bigint(0) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_user`(`article_id`, `user_id`) USING BTREE COMMENT '联合唯一索引，防止重复点赞'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_article_like
-- ----------------------------
INSERT INTO `t_article_like` VALUES (4, 8, 1, '2025-12-06 12:27:03');
INSERT INTO `t_article_like` VALUES (12, 4, 1, '2025-12-06 13:45:13');
INSERT INTO `t_article_like` VALUES (14, 15, 1, '2025-12-06 14:09:46');
INSERT INTO `t_article_like` VALUES (15, 7, 1, '2025-12-06 14:47:30');
INSERT INTO `t_article_like` VALUES (17, 4, 6, '2025-12-06 15:07:19');
INSERT INTO `t_article_like` VALUES (18, 7, 6, '2025-12-06 15:20:47');
INSERT INTO `t_article_like` VALUES (23, 16, 1, '2025-12-06 21:38:24');

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称，如 Java, 前端, 数据库',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_category
-- ----------------------------
INSERT INTO `t_category` VALUES (1, 'Java', '2025-12-06 09:33:37');
INSERT INTO `t_category` VALUES (2, '数据库', '2025-12-06 09:33:37');
INSERT INTO `t_category` VALUES (3, '前端技术', '2025-12-06 09:33:37');
INSERT INTO `t_category` VALUES (4, '系统设计', '2025-12-06 09:33:37');
INSERT INTO `t_category` VALUES (5, '编程思想', '2025-12-06 09:33:37');

-- ----------------------------
-- Table structure for t_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `article_id` bigint(0) NOT NULL COMMENT '所属文章ID',
  `user_id` bigint(0) NOT NULL COMMENT '评论者ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父评论ID(用于多级回复)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE,
  INDEX `fk_comment_parent`(`parent_id`) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `t_comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_comment
-- ----------------------------
INSERT INTO `t_comment` VALUES (7, 15, 1, 'ADSF ', NULL, '2025-12-06 14:36:56');
INSERT INTO `t_comment` VALUES (8, 15, 1, 'ASDF ASD', 7, '2025-12-06 14:37:00');
INSERT INTO `t_comment` VALUES (9, 15, 1, 'ASDF ', 8, '2025-12-06 14:37:05');
INSERT INTO `t_comment` VALUES (10, 15, 1, 'DFASD ', 7, '2025-12-06 14:37:10');
INSERT INTO `t_comment` VALUES (11, 15, 1, 'ASDF ASD', 7, '2025-12-06 14:37:16');
INSERT INTO `t_comment` VALUES (29, 4, 1, '阿达', NULL, '2025-12-06 15:18:59');
INSERT INTO `t_comment` VALUES (30, 4, 1, '阿斯顿发生的', 29, '2025-12-06 15:19:05');
INSERT INTO `t_comment` VALUES (32, 7, 1, '是哒是哒发水电费', NULL, '2025-12-06 15:20:04');
INSERT INTO `t_comment` VALUES (33, 7, 6, 'dasfasd', 32, '2025-12-06 15:20:30');
INSERT INTO `t_comment` VALUES (34, 7, 6, 'asdfa sd', 33, '2025-12-06 15:20:35');
INSERT INTO `t_comment` VALUES (35, 7, 6, '敬爱的发多少', NULL, '2025-12-06 15:20:56');
INSERT INTO `t_comment` VALUES (36, 16, 1, 'dfadsf ', NULL, '2025-12-06 21:19:10');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', '123456', 'XW', NULL, '2025-12-05 12:22:23');
INSERT INTO `t_user` VALUES (5, 'junit_test_user', '123456', '测试员小张', NULL, '2025-12-05 12:52:23');
INSERT INTO `t_user` VALUES (6, 'admin1', '111111', '天上', NULL, '2025-12-11 14:48:55');

SET FOREIGN_KEY_CHECKS = 1;
