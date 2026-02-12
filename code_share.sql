/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : code_share

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 31/07/2025 23:17:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for answers
-- ----------------------------
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '回答唯一ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回答内容',
  `user_id` int NOT NULL COMMENT '回答者ID',
  `like_count` int NULL DEFAULT NULL COMMENT '点赞数',
  `question_id` int NOT NULL COMMENT '关联的问题ID',
  `is_accepted` int NULL DEFAULT 0 COMMENT '是否被采纳：0-未采纳，1-已采纳',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问题回答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of answers
-- ----------------------------
INSERT INTO `answers` VALUES (1, '我觉得等于2', 2, 10, 1, 0, '2025-05-01 19:58:39');
INSERT INTO `answers` VALUES (2, '不会', 6, NULL, 1, 1, '2025-05-01 20:17:32');
INSERT INTO `answers` VALUES (3, '111111111111111111111111111', 6, NULL, 1, 1, '2025-05-01 20:20:32');
INSERT INTO `answers` VALUES (4, '22222', 6, NULL, 1, 0, '2025-05-01 20:34:21');
INSERT INTO `answers` VALUES (5, '1111', 9, NULL, 5, 0, '2025-05-04 12:05:48');
INSERT INTO `answers` VALUES (6, '1111', 6, NULL, 6, 1, '2025-05-04 13:22:16');
INSERT INTO `answers` VALUES (7, '1111', 6, NULL, 7, 0, '2025-05-04 14:19:56');
INSERT INTO `answers` VALUES (8, '22222222', 6, NULL, 8, 1, '2025-05-04 15:02:05');
INSERT INTO `answers` VALUES (9, '222', 6, NULL, 8, 1, '2025-05-04 15:02:15');
INSERT INTO `answers` VALUES (10, '222222', 9, NULL, 8, 1, '2025-05-04 15:02:20');

-- ----------------------------
-- Table structure for blogs
-- ----------------------------
DROP TABLE IF EXISTS `blogs`;
CREATE TABLE `blogs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '博客唯一ID',
  `data_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档id',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '博客标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '博客内容（支持文字/图片/代码块）',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览数',
  `author_id` int NOT NULL COMMENT '作者ID',
  `likes_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `section_id` int NOT NULL COMMENT '所属板块ID',
  `status` int NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
  `version` int NULL DEFAULT 1 COMMENT '当前版本号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '博客创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技术博客表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blogs
-- ----------------------------
INSERT INTO `blogs` VALUES (1, 'b185b3f9635e4af484f27d0d1ed1e783', '1', '1', 0, 1, 0, 1, 0, 0, '2025-04-27 12:24:14', '2025-05-04 02:11:26');
INSERT INTO `blogs` VALUES (3, '6abe0b7042b649ef9809068c2364716c', '1', '1', 6, 6, 0, 1, 1, 0, '2025-04-27 13:39:39', '2025-05-03 02:07:34');
INSERT INTO `blogs` VALUES (5, '80928ab56fc040c5858ce371a307b8db', '测试博客', '### 2.4.3.权限管理\n\n#### 2.4.3.1.资源管理\n\n\n![【哲风壁纸】Chris插画-插画 (1).png](http://110.42.213.154:9000/code-share/blogImage/20250427_b3f7a3e572ad4497a8417348f4211bad.png \"【哲风壁纸】Chris插画-插画 (1).png\")\n\n\n\n  \n\n```sql\n-- 创建数据库\nCREATE DATABASE IF NOT EXISTS tech_platform DEFAULT CHARSET utf8mb4;\nUSE tech_platform;\n\n-- 用户表（管理员与普通用户共用，通过 role 字段标识）\nCREATE TABLE users (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'用户唯一ID\',\n    username VARCHAR(50) UNIQUE NOT NULL COMMENT \'用户名\',\n    password_hash VARCHAR(255) NOT NULL COMMENT \'加密后的密码\',\n    email VARCHAR(100) UNIQUE NOT NULL COMMENT \'用户邮箱\',\n    avatar_url VARCHAR(255) COMMENT \'用户头像URL\',\n    role INT NOT NULL DEFAULT 0 COMMENT \'用户角色：0-普通用户，1-管理员\',\n    is_active INT DEFAULT 0 COMMENT \'账号状态：0-启用，1-禁用\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'账号创建时间\',\n    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT \'最后更新时间\',\n    last_login DATETIME COMMENT \'最后登录时间\'\n) COMMENT=\'用户信息表\';\n\n-- 板块表\nCREATE TABLE sections (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'板块唯一ID\',\n    name VARCHAR(100) NOT NULL COMMENT \'板块名称\',\n    description TEXT COMMENT \'板块描述\',\n    admin_id INT NOT NULL COMMENT \'创建板块的管理员ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'板块创建时间\',\n    FOREIGN KEY (admin_id) REFERENCES users(id)\n) COMMENT=\'技术交流板块表\';\n\n-- 博客表（状态字段改为数字标识）\nCREATE TABLE blogs (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'博客唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'博客标题\',\n    content LONGTEXT NOT NULL COMMENT \'博客内容（支持文字/图片/代码块）\',\n    author_id INT NOT NULL COMMENT \'作者ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    status INT DEFAULT 0 COMMENT \'状态：0-草稿，1-已发布\',\n    version INT DEFAULT 1 COMMENT \'当前版本号\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'博客创建时间\',\n    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT \'最后更新时间\',\n    FOREIGN KEY (author_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'技术博客表\';\n\n-- 博客版本历史表\nCREATE TABLE blog_versions (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'版本记录ID\',\n    blog_id INT NOT NULL COMMENT \'关联的博客ID\',\n    content LONGTEXT NOT NULL COMMENT \'历史版本内容\',\n    version INT NOT NULL COMMENT \'版本号\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'版本保存时间\',\n    FOREIGN KEY (blog_id) REFERENCES blogs(id)\n) COMMENT=\'博客版本历史记录表\';\n\n-- 评论表\nCREATE TABLE comments (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'评论唯一ID\',\n    content TEXT NOT NULL COMMENT \'评论内容\',\n    user_id INT NOT NULL COMMENT \'评论者ID\',\n    target_type ENUM(\'blog\', \'question\') NOT NULL COMMENT \'关联类型：blog-博客，question-提问\',\n    target_id INT NOT NULL COMMENT \'关联目标ID\',\n    likes INT DEFAULT 0 COMMENT \'点赞数\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'评论时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id)\n) COMMENT=\'评论表\';\n\n-- 提问表（状态字段改为数字标识）\nCREATE TABLE questions (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'提问唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'提问标题\',\n    content TEXT NOT NULL COMMENT \'提问内容\',\n    user_id INT NOT NULL COMMENT \'提问者ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    is_resolved INT DEFAULT 0 COMMENT \'是否已解决：0-未解决，1-已解决\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'提问时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'技术问题提问表\';\n\n-- 回答表（状态字段改为数字标识）\nCREATE TABLE answers (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'回答唯一ID\',\n    content TEXT NOT NULL COMMENT \'回答内容\',\n    user_id INT NOT NULL COMMENT \'回答者ID\',\n    question_id INT NOT NULL COMMENT \'关联的问题ID\',\n    is_accepted INT DEFAULT 0 COMMENT \'是否被采纳：0-未采纳，1-已采纳\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'回答时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (question_id) REFERENCES questions(id)\n) COMMENT=\'问题回答表\';\n\n-- 电子书表（状态字段改为数字标识）\nCREATE TABLE ebooks (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'电子书唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'电子书标题\',\n    description TEXT COMMENT \'电子书描述\',\n    file_path VARCHAR(255) NOT NULL COMMENT \'文件存储路径\',\n    is_published INT DEFAULT 1 COMMENT \'上架状态：0-下架，1-上架\',\n    admin_id INT NOT NULL COMMENT \'上传的管理员ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'创建时间\',\n    FOREIGN KEY (admin_id) REFERENCES users(id)\n) COMMENT=\'电子书资源表\';\n\n-- 视频表（无需状态字段，但保留示例）\nCREATE TABLE videos (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'视频唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'视频标题\',\n    description TEXT COMMENT \'视频描述\',\n    video_path VARCHAR(255) NOT NULL COMMENT \'视频存储路径\',\n    cover_path VARCHAR(255) COMMENT \'封面图路径\',\n    user_id INT NOT NULL COMMENT \'上传用户ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'上传时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'视频资源表\';\n\n-- 通知表（状态字段改为数字标识）\nCREATE TABLE notifications (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'通知唯一ID\',\n    user_id INT NOT NULL COMMENT \'接收用户ID\',\n    type ENUM(\'comment\', \'like\', \'answer\', \'accept\') NOT NULL COMMENT \'通知类型\',\n    source_id INT COMMENT \'关联来源ID（如评论ID/回答ID）\',\n    is_read INT DEFAULT 0 COMMENT \'是否已读：0-未读，1-已读\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'通知时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id)\n) COMMENT=\'用户通知表\';\n\n-- 索引优化（保持不变）\nCREATE INDEX idx_blogs_author ON blogs(author_id);\nCREATE INDEX idx_comments_target ON comments(target_type, target_id);\n```', 4, 6, 0, 6, 0, 0, '2025-04-27 15:37:14', '2025-05-02 23:59:30');
INSERT INTO `blogs` VALUES (6, '43915ad5223c43688b386a5109a2ab85', '测试博客', '### 2.4.3.权限管理\n\n#### 2.4.3.1.资源管理\n\n\n![【哲风壁纸】Chris插画-插画 (1).png](http://110.42.213.154:9000/code-share/blogImage/20250427_b3f7a3e572ad4497a8417348f4211bad.png \"【哲风壁纸】Chris插画-插画 (1).png\")\n\n\n\n  \n\n```sql\n-- 创建数据库\nCREATE DATABASE IF NOT EXISTS tech_platform DEFAULT CHARSET utf8mb4;\nUSE tech_platform;\n\n-- 用户表（管理员与普通用户共用，通过 role 字段标识）\nCREATE TABLE users (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'用户唯一ID\',\n    username VARCHAR(50) UNIQUE NOT NULL COMMENT \'用户名\',\n    password_hash VARCHAR(255) NOT NULL COMMENT \'加密后的密码\',\n    email VARCHAR(100) UNIQUE NOT NULL COMMENT \'用户邮箱\',\n    avatar_url VARCHAR(255) COMMENT \'用户头像URL\',\n    role INT NOT NULL DEFAULT 0 COMMENT \'用户角色：0-普通用户，1-管理员\',\n    is_active INT DEFAULT 0 COMMENT \'账号状态：0-启用，1-禁用\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'账号创建时间\',\n    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT \'最后更新时间\',\n    last_login DATETIME COMMENT \'最后登录时间\'\n) COMMENT=\'用户信息表\';\n\n-- 板块表\nCREATE TABLE sections (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'板块唯一ID\',\n    name VARCHAR(100) NOT NULL COMMENT \'板块名称\',\n    description TEXT COMMENT \'板块描述\',\n    admin_id INT NOT NULL COMMENT \'创建板块的管理员ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'板块创建时间\',\n    FOREIGN KEY (admin_id) REFERENCES users(id)\n) COMMENT=\'技术交流板块表\';\n\n-- 博客表（状态字段改为数字标识）\nCREATE TABLE blogs (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'博客唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'博客标题\',\n    content LONGTEXT NOT NULL COMMENT \'博客内容（支持文字/图片/代码块）\',\n    author_id INT NOT NULL COMMENT \'作者ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    status INT DEFAULT 0 COMMENT \'状态：0-草稿，1-已发布\',\n    version INT DEFAULT 1 COMMENT \'当前版本号\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'博客创建时间\',\n    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT \'最后更新时间\',\n    FOREIGN KEY (author_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'技术博客表\';\n\n-- 博客版本历史表\nCREATE TABLE blog_versions (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'版本记录ID\',\n    blog_id INT NOT NULL COMMENT \'关联的博客ID\',\n    content LONGTEXT NOT NULL COMMENT \'历史版本内容\',\n    version INT NOT NULL COMMENT \'版本号\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'版本保存时间\',\n    FOREIGN KEY (blog_id) REFERENCES blogs(id)\n) COMMENT=\'博客版本历史记录表\';\n\n-- 评论表\nCREATE TABLE comments (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'评论唯一ID\',\n    content TEXT NOT NULL COMMENT \'评论内容\',\n    user_id INT NOT NULL COMMENT \'评论者ID\',\n    target_type ENUM(\'blog\', \'question\') NOT NULL COMMENT \'关联类型：blog-博客，question-提问\',\n    target_id INT NOT NULL COMMENT \'关联目标ID\',\n    likes INT DEFAULT 0 COMMENT \'点赞数\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'评论时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id)\n) COMMENT=\'评论表\';\n\n-- 提问表（状态字段改为数字标识）\nCREATE TABLE questions (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'提问唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'提问标题\',\n    content TEXT NOT NULL COMMENT \'提问内容\',\n    user_id INT NOT NULL COMMENT \'提问者ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    is_resolved INT DEFAULT 0 COMMENT \'是否已解决：0-未解决，1-已解决\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'提问时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'技术问题提问表\';\n\n-- 回答表（状态字段改为数字标识）\nCREATE TABLE answers (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'回答唯一ID\',\n    content TEXT NOT NULL COMMENT \'回答内容\',\n    user_id INT NOT NULL COMMENT \'回答者ID\',\n    question_id INT NOT NULL COMMENT \'关联的问题ID\',\n    is_accepted INT DEFAULT 0 COMMENT \'是否被采纳：0-未采纳，1-已采纳\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'回答时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (question_id) REFERENCES questions(id)\n) COMMENT=\'问题回答表\';\n\n-- 电子书表（状态字段改为数字标识）\nCREATE TABLE ebooks (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'电子书唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'电子书标题\',\n    description TEXT COMMENT \'电子书描述\',\n    file_path VARCHAR(255) NOT NULL COMMENT \'文件存储路径\',\n    is_published INT DEFAULT 1 COMMENT \'上架状态：0-下架，1-上架\',\n    admin_id INT NOT NULL COMMENT \'上传的管理员ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'创建时间\',\n    FOREIGN KEY (admin_id) REFERENCES users(id)\n) COMMENT=\'电子书资源表\';\n\n-- 视频表（无需状态字段，但保留示例）\nCREATE TABLE videos (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'视频唯一ID\',\n    title VARCHAR(200) NOT NULL COMMENT \'视频标题\',\n    description TEXT COMMENT \'视频描述\',\n    video_path VARCHAR(255) NOT NULL COMMENT \'视频存储路径\',\n    cover_path VARCHAR(255) COMMENT \'封面图路径\',\n    user_id INT NOT NULL COMMENT \'上传用户ID\',\n    section_id INT NOT NULL COMMENT \'所属板块ID\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'上传时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id),\n    FOREIGN KEY (section_id) REFERENCES sections(id)\n) COMMENT=\'视频资源表\';\n\n-- 通知表（状态字段改为数字标识）\nCREATE TABLE notifications (\n    id INT PRIMARY KEY AUTO_INCREMENT COMMENT \'通知唯一ID\',\n    user_id INT NOT NULL COMMENT \'接收用户ID\',\n    type ENUM(\'comment\', \'like\', \'answer\', \'accept\') NOT NULL COMMENT \'通知类型\',\n    source_id INT COMMENT \'关联来源ID（如评论ID/回答ID）\',\n    is_read INT DEFAULT 0 COMMENT \'是否已读：0-未读，1-已读\',\n    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \'通知时间\',\n    FOREIGN KEY (user_id) REFERENCES users(id)\n) COMMENT=\'用户通知表\';\n\n-- 索引优化（保持不变）\nCREATE INDEX idx_blogs_author ON blogs(author_id);\nCREATE INDEX idx_comments_target ON comments(target_type, target_id);\n```', 64, 6, 0, 6, 1, 0, '2025-04-27 15:37:19', '2025-05-02 23:59:35');
INSERT INTO `blogs` VALUES (7, '8ddf5de362b24c06afba66eaa3fd91b0', '微服务架构的核心概念与挑战', '# 第一篇：微服务架构的核心概念与挑战\n\n## 什么是微服务架构？\n微服务架构是一种将单一应用程序划分为**一组小型服务**的方法，每个服务：\n- 运行在自己的进程中\n- 通过轻量级机制通信（通常是HTTP API）\n- 围绕业务能力构建\n- 可独立部署\n- 使用不同的编程语言/数据存储技术\n\n## 核心优势\n✅ **技术异构性**：不同服务可采用最适合的技术栈  \n✅ **弹性**：单个服务故障不会导致整个系统崩溃  \n✅ **可扩展性**：可按服务维度独立扩展  \n✅ **部署简单**：单个服务更新无需全站发布  \n\n## 常见挑战\n⚠ **分布式系统复杂度**：网络延迟、故障转移、消息一致性  \n⚠ **数据一致性**：需要采用Saga模式或CQRS等解决方案  \n⚠ **测试难度**：需要完善的Mock服务和契约测试  \n⚠ **运维成本**：需要成熟的监控和日志聚合系统\n\n## 典型技术栈\n| 类别       | 技术示例                  |\n|------------|--------------------------|\n| 服务框架   | Spring Cloud, Go Micro   |\n| 服务发现   | Consul, Eureka, Zookeeper|\n| 配置中心   | Nacos, Apollo            |\n| API网关    | Kong, Spring Cloud Gateway|\n\n# 第二篇：高性能缓存策略实践指南\n\n## 缓存层级设计\n```mermaid\ngraph TD\n    A[客户端] -->|CDN| B(边缘缓存)\n    B -->|反向代理| C[Nginx缓存]\n    C -->|应用层| D[进程内缓存]\n    D -->|分布式| E[Redis集群]\n    E -->|持久层| F[数据库]', 86, 6, 0, 6, 1, 0, '2025-04-28 22:23:37', '2025-05-02 23:59:41');
INSERT INTO `blogs` VALUES (8, '73e5b7462b4e4a50b5a122d74f8cfa66', '微服务架构的核心概念与挑战', '# 第一篇：微服务架构的核心概念与挑战\n\n## 什么是微服务架构？\n微服务架构是一种将单一应用程序划分为**一组小型服务**的方法，每个服务：\n- 运行在自己的进程中\n- 通过轻量级机制通信（通常是HTTP API）\n- 围绕业务能力构建\n- 可独立部署\n- 使用不同的编程语言/数据存储技术\n\n## 核心优势\n✅ **技术异构性**：不同服务可采用最适合的技术栈  \n✅ **弹性**：单个服务故障不会导致整个系统崩溃  \n✅ **可扩展性**：可按服务维度独立扩展  \n✅ **部署简单**：单个服务更新无需全站发布  \n\n## 常见挑战\n⚠ **分布式系统复杂度**：网络延迟、故障转移、消息一致性  \n⚠ **数据一致性**：需要采用Saga模式或CQRS等解决方案  \n⚠ **测试难度**：需要完善的Mock服务和契约测试  \n⚠ **运维成本**：需要成熟的监控和日志聚合系统\n\n## 典型技术栈\n| 类别       | 技术示例                  |\n|------------|--------------------------|\n| 服务框架   | Spring Cloud, Go Micro   |\n| 服务发现   | Consul, Eureka, Zookeeper|\n| 配置中心   | Nacos, Apollo            |\n| API网关    | Kong, Spring Cloud Gateway|\n\n# 第二篇：高性能缓存策略实践指南\n\n## 缓存层级设计\n```mermaid\ngraph TD\n    A[客户端] -->|CDN| B(边缘缓存)\n    B -->|反向代理| C[Nginx缓存]\n    C -->|应用层| D[进程内缓存]\n    D -->|分布式| E[Redis集群]\n    E -->|持久层| F[数据库]', 0, 6, 0, 6, 0, 0, '2025-04-28 22:23:40', '2025-05-02 23:59:45');
INSERT INTO `blogs` VALUES (9, '8c4759f098874dffae9fd53f255a4f2e', '微服务架构的核心概念与挑战', '# 第一篇：微服务架构的核心概念与挑战\n\n## 什么是微服务架构？\n微服务架构是一种将单一应用程序划分为**一组小型服务**的方法，每个服务：\n- 运行在自己的进程中\n- 通过轻量级机制通信（通常是HTTP API）\n- 围绕业务能力构建\n- 可独立部署\n- 使用不同的编程语言/数据存储技术\n\n## 核心优势\n✅ **技术异构性**：不同服务可采用最适合的技术栈  \n✅ **弹性**：单个服务故障不会导致整个系统崩溃  \n✅ **可扩展性**：可按服务维度独立扩展  \n✅ **部署简单**：单个服务更新无需全站发布  \n\n## 常见挑战\n⚠ **分布式系统复杂度**：网络延迟、故障转移、消息一致性  \n⚠ **数据一致性**：需要采用Saga模式或CQRS等解决方案  \n⚠ **测试难度**：需要完善的Mock服务和契约测试  \n⚠ **运维成本**：需要成熟的监控和日志聚合系统\n\n## 典型技术栈\n| 类别       | 技术示例                  |\n|------------|--------------------------|\n| 服务框架   | Spring Cloud, Go Micro   |\n| 服务发现   | Consul, Eureka, Zookeeper|\n| 配置中心   | Nacos, Apollo            |\n| API网关    | Kong, Spring Cloud Gateway|\n\n# 第二篇：高性能缓存策略实践指南\n\n## 缓存层级设计\n```mermaid\ngraph TD\n    A[客户端] -->|CDN| B(边缘缓存)\n    B -->|反向代理| C[Nginx缓存]\n    C -->|应用层| D[进程内缓存]\n    D -->|分布式| E[Redis集群]\n    E -->|持久层| F[数据库]', 2, 6, 0, 6, 0, 0, '2025-04-28 22:23:42', '2025-05-02 23:59:50');
INSERT INTO `blogs` VALUES (10, 'd4f419d5c2bf40499f10d6b4b6e4d033', '大模型时代：机遇、挑战与未来展望', '# 大模型时代：机遇、挑战与未来展望\n\n## 引言\n![AI Landscape](https://example.com/ai-banner.jpg)  \n*图：AI技术发展路线图*\n\n过去两年，以ChatGPT为代表的AI大模型引发了全球科技革命。这些参数规模达千亿级的神经网络，正在重塑我们与技术交互的方式。\n\n## 一、核心技术解析\n\n### 1.1 大模型的三大支柱\n```mermaid\ngraph TD\n    A[算力] --> B[Transformer架构]\n    B --> C[海量数据]\n    C --> D[大模型]\n    1.2 关键技术突破\nScaling Law：模型性能随参数增加持续提升\n\nRLHF：人类反馈强化学习\n\nMoE架构：专家混合模型（如GPT-4推测架构）\n\n二、行业应用现状\n2.1 典型应用场景\n领域	应用案例	效益提升\n医疗	医学文献分析/辅助诊断	40%+\n金融	智能投研/反欺诈	35%\n教育	个性化学习助手	50%\n2.2 企业落地挑战\n三、前沿研究方向\n3.1 六大热点方向\n多模态融合（文本/图像/视频）\n\n小样本学习（Few-shot Learning）\n\n模型蒸馏（Knowledge Distillation）\n\n可解释性（XAI）\n\n持续学习（Continual Learning）\n\n能源效率（Green AI）\n\n四、伦理与社会影响\n\"AI既不是善也不是恶，它是工具。与火或核能一样，关键在于我们如何使用它\"\n—— Andrew Ng\n\n争议焦点：\n\n就业替代效应\n\n生成内容版权\n\n算法偏见问题\n\n能源消耗（单次训练≈3000辆汽车年碳排放）\n\n五、未来展望\n2024-2030预测：\n\n✅ 边缘设备部署微型大模型\n\n✅ 多智能体协作系统\n\n⚠️ 可能出现\"模型即服务\"(MaaS)垄断\n\n❓ AGI是否在本十年内实现？\n\n结语\n大模型正在开启人机协作的新纪元。作为开发者，我们既要拥抱技术创新，也要保持对技术伦理的思考。正如Linus定律所说：\"足够多的眼睛，可使所有bug浮现\"，在AI发展道路上，需要全球协作的智慧与责任。', 12, 9, 0, 4, 1, 0, '2025-05-01 21:06:46', '2025-05-02 23:59:56');
INSERT INTO `blogs` VALUES (11, '89b91b86c58a440da72c4cc30a62bdff', '大模型时代：机遇、挑战与未来展望', '# 大模型时代：机遇、挑战与未来展望\n\n## 引言\n![AI Landscape](https://example.com/ai-banner.jpg)  \n*图：AI技术发展路线图*\n\n过去两年，以ChatGPT为代表的AI大模型引发了全球科技革命。这些参数规模达千亿级的神经网络，正在重塑我们与技术交互的方式。\n\n## 一、核心技术解析\n\n### 1.1 大模型的三大支柱\n```mermaid\ngraph TD\n    A[算力] --> B[Transformer架构]\n    B --> C[海量数据]\n    C --> D[大模型]\n    1.2 关键技术突破\nScaling Law：模型性能随参数增加持续提升\n\nRLHF：人类反馈强化学习\n\nMoE架构：专家混合模型（如GPT-4推测架构）\n\n二、行业应用现状\n2.1 典型应用场景\n领域	应用案例	效益提升\n医疗	医学文献分析/辅助诊断	40%+\n金融	智能投研/反欺诈	35%\n教育	个性化学习助手	50%\n2.2 企业落地挑战\n三、前沿研究方向\n3.1 六大热点方向\n多模态融合（文本/图像/视频）\n\n小样本学习（Few-shot Learning）\n\n模型蒸馏（Knowledge Distillation）\n\n可解释性（XAI）\n\n持续学习（Continual Learning）\n\n能源效率（Green AI）\n\n四、伦理与社会影响\n\"AI既不是善也不是恶，它是工具。与火或核能一样，关键在于我们如何使用它\"\n—— Andrew Ng\n\n争议焦点：\n\n就业替代效应\n\n生成内容版权\n\n算法偏见问题\n\n能源消耗（单次训练≈3000辆汽车年碳排放）\n\n五、未来展望\n2024-2030预测：\n\n✅ 边缘设备部署微型大模型\n\n✅ 多智能体协作系统\n\n⚠️ 可能出现\"模型即服务\"(MaaS)垄断\n\n❓ AGI是否在本十年内实现？\n\n结语\n大模型正在开启人机协作的新纪元。作为开发者，我们既要拥抱技术创新，也要保持对技术伦理的思考。正如Linus定律所说：\"足够多的眼睛，可使所有bug浮现\"，在AI发展道路上，需要全球协作的智慧与责任。', 0, 9, 0, 4, 0, 0, '2025-05-01 21:06:48', '2025-05-03 00:00:01');
INSERT INTO `blogs` VALUES (12, 'c960026c57e84810add38bc46ba6a34e', '多模态融合', '三、前沿研究方向\n3.1 六大热点方向\n多模态融合（文本/图像/视频）\n\n小样本学习（Few-shot Learning）\n\n模型蒸馏（Knowledge Distillation）\n\n可解释性（XAI）\n\n持续学习（Continual Learning）\n\n能源效率（Green AI）\n\n四、伦理与社会影响\n\"AI既不是善也不是恶，它是工具。与火或核能一样，关键在于我们如何使用它\"\n—— Andrew Ng\n\n争议焦点：\n\n就业替代效应\n\n生成内容版权\n\n算法偏见问题\n\n能源消耗（单次训练≈3000辆汽车年碳排放）\n\n五、未来展望\n2024-2030预测：\n\n✅ 边缘设备部署微型大模型\n\n✅ 多智能体协作系统\n\n⚠️ 可能出现\"模型即服务\"(MaaS)垄断\n\n❓ AGI是否在本十年内实现？\n\n结语\n大模型正在开启人机协作的新纪元。作为开发者，我们既要拥抱技术创新，也要保持对技术伦理的思考。正如Linus定律所说：\"足够多的眼睛，可使所有bug浮现\"，在AI发展道路上，需要全球协作的智慧与责任。\n\n', 112, 9, 0, 4, 1, 0, '2025-05-01 21:07:15', '2025-05-03 00:00:06');
INSERT INTO `blogs` VALUES (13, 'a766df38f83d4350aa85d546a3514b58', '多模态融合', '三、前沿研究方向\n3.1 六大热点方向\n多模态融合（文本/图像/视频）\n\n小样本学习（Few-shot Learning）\n\n模型蒸馏（Knowledge Distillation）\n\n可解释性（XAI）\n\n持续学习（Continual Learning）\n\n能源效率（Green AI）\n\n四、伦理与社会影响\n\"AI既不是善也不是恶，它是工具。与火或核能一样，关键在于我们如何使用它\"\n—— Andrew Ng\n\n争议焦点：\n\n就业替代效应\n\n生成内容版权\n\n算法偏见问题\n\n能源消耗（单次训练≈3000辆汽车年碳排放）\n\n五、未来展望\n2024-2030预测：\n\n✅ 边缘设备部署微型大模型\n\n✅ 多智能体协作系统\n\n⚠️ 可能出现\"模型即服务\"(MaaS)垄断\n\n❓ AGI是否在本十年内实现？\n\n结语\n大模型正在开启人机协作的新纪元。作为开发者，我们既要拥抱技术创新，也要保持对技术伦理的思考。正如Linus定律所说：\"足够多的眼睛，可使所有bug浮现\"，在AI发展道路上，需要全球协作的智慧与责任。\n\n', 4, 9, 0, 4, 0, 2, '2025-05-01 21:07:19', '2025-05-03 00:00:12');
INSERT INTO `blogs` VALUES (19, 'b185b3f9635e4af484f27d0d1ed1e783', '2', '2', 46, 1, 0, 1, 1, 1, '2025-05-02 23:45:21', '2025-05-02 23:59:12');
INSERT INTO `blogs` VALUES (21, '6abe0b7042b649ef9809068c2364716c', '版本加1', '版本加1', 34, 6, 0, 1, 1, 7, '2025-04-27 13:39:39', '2025-05-03 00:32:38');
INSERT INTO `blogs` VALUES (61, '6abe0b7042b649ef9809068c2364716c', '版本加1', '版本加1\n\n\n\n![【哲风壁纸】间谍过家家-阿尼亚.png](http://110.42.213.154:9000/code-share/blogImage/20250504_a9f9c8fadfe043719dd02f6f0f399bb6.png \"【哲风壁纸】间谍过家家-阿尼亚.png\")', 20, 6, 0, 2, 1, 4, '2025-04-27 13:39:39', '2025-05-04 02:40:11');
INSERT INTO `blogs` VALUES (63, '60ce924eeff840d5ae43d430cba7c7e8', '深入理解 Server-Sent Events (SSE)：实时数据推送的轻量级方案', '# 深入理解 Server-Sent Events (SSE)：实时数据推送的轻量级方案\n\n## 什么是SSE？\n**Server-Sent Events** 是基于 HTTP 的服务器到客户端**单向实时通信协议**，特点：\n- 使用简单文本格式（event-stream）\n- 支持自动重连机制\n- 天然支持HTTP协议栈（无需额外端口）\n- 浏览器原生支持（EventSource API）\n\n![SSE工作原理](https://example.com/sse-diagram.png)\n（示意图：客户端发起持久连接，服务器持续发送数据流）\n\n## 与WebSocket的核心差异\n| 特性               | SSE                     | WebSocket         |\n|--------------------|-------------------------|-------------------|\n| 通信方向           | 单向（Server→Client）   | 双向              |\n| 协议               | HTTP                    | 独立协议(ws://)   |\n| 数据格式           | 文本/简单结构           | 二进制/自定义     |\n| 浏览器兼容性       | 除IE外主流支持          | 全平台支持        |\n| 断线重连           | 内置支持                | 需手动实现        |\n\n## 快速实现示例\n\n### 服务端（Node.js）\n```javascript\nconst http = require(\'http\');\n\nhttp.createServer((req, res) => {\n  if (req.url === \'/stream\') {\n    res.writeHead(200, {\n      \'Content-Type\': \'text/event-stream\',\n      \'Cache-Control\': \'no-cache\',\n      \'Connection\': \'keep-alive\'\n    });\n\n    // 定时推送消息\n    const timer = setInterval(() => {\n      res.write(`data: ${new Date().toISOString()}\\n\\n`);\n    }, 1000);\n\n    // 清理连接\n    req.on(\'close\', () => clearInterval(timer));\n  }\n}).listen(3000);', 6, 6, 0, 3, 1, 0, '2025-05-04 13:21:31', '2025-05-04 13:21:31');
INSERT INTO `blogs` VALUES (64, 'a766df38f83d4350aa85d546a3514b58', '多模态融合', '三、前沿研究方向\n3.1 六大热点方向\n多模态融合（文本/图像/视频）\n\n小样本学习（Few-shot Learning）\n\n模型蒸馏（Knowledge Distillation）\n\n可解释性（XAI）\n\n持续学习（Continual Learning）\n\n能源效率（Green AI）\n\n四、伦理与社会影响\n\"AI既不是善也不是恶，它是工具。与火或核能一样，关键在于我们如何使用它\"\n—— Andrew Ng\n\n争议焦点：\n\n就业替代效应\n\n生成内容版权\n\n算法偏见问题\n\n能源消耗（单次训练≈3000辆汽车年碳排放）\n\n五、未来展望\n2024-2030预测：\n\n✅ 边缘设备部署微型大模型\n\n✅ 多智能体协作系统\n\n⚠️ 可能出现\"模型即服务\"(MaaS)垄断\n\n❓ AGI是否在本十年内实现？\n\n结语\n大模型正在开启人机协作的新纪元。作为开发者，我们既要拥抱技术创新，也要保持对技术伦理的思考。正如Linus定律所说：\"足够多的眼睛，可使所有bug浮现\"，在AI发展道路上，需要全球协作的智慧与责任。\n\n版本2\n\n', 0, 9, 0, 2, 1, 1, '2025-05-01 21:07:19', '2025-05-04 13:25:06');
INSERT INTO `blogs` VALUES (65, '3dd9ca9c0c39493093f3f9f8c9ca5514', '实时数据推送的轻量级方案', '# 深入理解 Server-Sent Events (SSE)：实时数据推送的轻量级方案\n\n## 什么是SSE？\n**Server-Sent Events** 是基于 HTTP 的服务器到客户端**单向实时通信协议**，特点：\n- 使用简单文本格式（event-stream）\n- 支持自动重连机制\n- 天然支持HTTP协议栈（无需额外端口）\n- 浏览器原生支持（EventSource API）\n\n![SSE工作原理](https://example.com/sse-diagram.png)\n（示意图：客户端发起持久连接，服务器持续发送数据流）\n\n## 与WebSocket的核心差异\n| 特性               | SSE                     | WebSocket         |\n|--------------------|-------------------------|-------------------|\n| 通信方向           | 单向（Server→Client）   | 双向              |\n| 协议               | HTTP                    | 独立协议(ws://)   |\n| 数据格式           | 文本/简单结构           | 二进制/自定义     |\n| 浏览器兼容性       | 除IE外主流支持          | 全平台支持        |\n| 断线重连           | 内置支持                | 需手动实现        |\n\n## 快速实现示例\n\n### 服务端（Node.js）\n```javascript\nconst http = require(\'http\');\n\nhttp.createServer((req, res) => {\n  if (req.url === \'/stream\') {\n    res.writeHead(200, {\n      \'Content-Type\': \'text/event-stream\',\n      \'Cache-Control\': \'no-cache\',\n      \'Connection\': \'keep-alive\'\n    });\n\n    // 定时推送消息\n    const timer = setInterval(() => {\n      res.write(`data: ${new Date().toISOString()}\\n\\n`);\n    }, 1000);\n\n    // 清理连接\n    req.on(\'close\', () => clearInterval(timer));\n  }\n}).listen(3000);', 0, 6, 0, 3, 0, 0, '2025-05-04 14:13:49', '2025-05-04 14:13:49');
INSERT INTO `blogs` VALUES (66, '5126273ebfa94b5f88accf8610ab9032', '实时数据推送的轻量级方案', '# 深入理解 Server-Sent Events (SSE)：实时数据推送的轻量级方案\n\n## 什么是SSE？\n**Server-Sent Events** 是基于 HTTP 的服务器到客户端**单向实时通信协议**，特点：\n- 使用简单文本格式（event-stream）\n- 支持自动重连机制\n- 天然支持HTTP协议栈（无需额外端口）\n- 浏览器原生支持（EventSource API）\n\n\n\n![【哲风壁纸】AI-创意场景.png](http://110.42.213.154:9000/code-share/blogImage/20250504_e8bc8c6626b642fbbd5c2f61354a0442.png \"【哲风壁纸】AI-创意场景.png\")\n![SSE工作原理](https://example.com/sse-diagram.png)\n（示意图：客户端发起持久连接，服务器持续发送数据流）\n\n## 与WebSocket的核心差异\n| 特性               | SSE                     | WebSocket         |\n|--------------------|-------------------------|-------------------|\n| 通信方向           | 单向（Server→Client）   | 双向              |\n| 协议               | HTTP                    | 独立协议(ws://)   |\n| 数据格式           | 文本/简单结构           | 二进制/自定义     |\n| 浏览器兼容性       | 除IE外主流支持          | 全平台支持        |\n| 断线重连           | 内置支持                | 需手动实现        |\n\n## 快速实现示例\n\n### 服务端（Node.js）\n```javascript\nconst http = require(\'http\');\n\nhttp.createServer((req, res) => {\n  if (req.url === \'/stream\') {\n    res.writeHead(200, {\n      \'Content-Type\': \'text/event-stream\',\n      \'Cache-Control\': \'no-cache\',\n      \'Connection\': \'keep-alive\'\n    });\n\n    // 定时推送消息\n    const timer = setInterval(() => {\n      res.write(`data: ${new Date().toISOString()}\\n\\n`);\n    }, 1000);\n\n    // 清理连接\n    req.on(\'close\', () => clearInterval(timer));\n  }\n}).listen(3000);', 0, 6, 0, 2, 0, 0, '2025-05-04 14:14:28', '2025-05-04 14:14:28');
INSERT INTO `blogs` VALUES (67, '5126273ebfa94b5f88accf8610ab9032', '实时数据推送的轻量级方案', '# 深入理解 Server-Sent Events (SSE)：实时数据推送的轻量级方案\n\n## 什么是SSE？\n**Server-Sent Events** 是基于 HTTP 的服务器到客户端**单向实时通信协议**，特点：\n- 使用简单文本格式（event-stream）\n- 支持自动重连机制\n- 天然支持HTTP协议栈（无需额外端口）\n- 浏览器原生支持（EventSource API）\n\n\n\n![【哲风壁纸】AI-创意场景.png](http://110.42.213.154:9000/code-share/blogImage/20250504_e8bc8c6626b642fbbd5c2f61354a0442.png \"【哲风壁纸】AI-创意场景.png\")\n![SSE工作原理](https://example.com/sse-diagram.png)\n（示意图：客户端发起持久连接，服务器持续发送数据流）\n\n## 与WebSocket的核心差异\n| 特性               | SSE                     | WebSocket         |\n|--------------------|-------------------------|-------------------|\n| 通信方向           | 单向（Server→Client）   | 双向              |\n| 协议               | HTTP                    | 独立协议(ws://)   |\n| 数据格式           | 文本/简单结构           | 二进制/自定义     |\n| 浏览器兼容性       | 除IE外主流支持          | 全平台支持        |\n| 断线重连           | 内置支持                | 需手动实现        |\n\n## 快速实现示例\n\n### 服务端（Node.js）\n```javascript\nconst http = require(\'http\');\n\nhttp.createServer((req, res) => {\n  if (req.url === \'/stream\') {\n    res.writeHead(200, {\n      \'Content-Type\': \'text/event-stream\',\n      \'Cache-Control\': \'no-cache\',\n      \'Connection\': \'keep-alive\'\n    });\n\n    // 定时推送消息\n    const timer = setInterval(() => {\n      res.write(`data: ${new Date().toISOString()}\\n\\n`);\n    }, 1000);\n\n    // 清理连接\n    req.on(\'close\', () => clearInterval(timer));\n  }\n}).listen(3000);', 17, 6, 0, 2, 1, 1, '2025-05-04 14:14:28', '2025-05-04 14:14:30');
INSERT INTO `blogs` VALUES (68, '6abe0b7042b649ef9809068c2364716c', '版本加2', '版本加2', 32, 6, 0, 1, 1, 6, '2025-04-27 13:39:39', '2025-05-04 14:26:13');

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `user_id` int NOT NULL COMMENT '评论者ID',
  `parent_id` int NULL DEFAULT NULL COMMENT '父评论ID',
  `target_id` int NOT NULL COMMENT '关联目标ID',
  `likes` int NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (1, '傻叉', 2, 0, 6, 0, '2025-04-27 17:06:11');
INSERT INTO `comments` VALUES (2, '你才是', 3, 1, 6, 0, '2025-04-27 17:06:24');
INSERT INTO `comments` VALUES (3, '1', 6, 0, 13, 0, '2025-05-01 21:15:49');
INSERT INTO `comments` VALUES (4, '2', 6, 3, 13, 0, '2025-05-01 21:16:03');
INSERT INTO `comments` VALUES (5, '理性讨论', 9, 0, 12, 0, '2025-05-01 21:19:36');
INSERT INTO `comments` VALUES (6, '@user3 不要争吵', 9, 5, 12, 0, '2025-05-01 21:19:46');
INSERT INTO `comments` VALUES (7, '@user3 1111', 6, 5, 12, 0, '2025-05-02 21:46:05');
INSERT INTO `comments` VALUES (14, '22', 9, 0, 7, 0, '2025-05-04 04:27:41');
INSERT INTO `comments` VALUES (15, '22', 9, 0, 7, 0, '2025-05-04 04:27:48');
INSERT INTO `comments` VALUES (16, '11', 9, 0, 7, 0, '2025-05-04 04:31:43');
INSERT INTO `comments` VALUES (21, '1', 9, 0, 6, 0, '2025-05-04 04:39:01');
INSERT INTO `comments` VALUES (24, '@user3 111111111', 9, 1, 6, 0, '2025-05-04 04:44:06');
INSERT INTO `comments` VALUES (25, '111', 6, 0, 19, 0, '2025-05-04 04:51:34');
INSERT INTO `comments` VALUES (26, '@user 222', 6, 25, 19, 0, '2025-05-04 04:51:38');
INSERT INTO `comments` VALUES (28, '@user 222', 6, 25, 19, 0, '2025-05-04 04:55:26');
INSERT INTO `comments` VALUES (30, '@user 111', 6, 25, 19, 0, '2025-05-04 04:56:19');
INSERT INTO `comments` VALUES (32, '@user 1111', 6, 26, 19, 0, '2025-05-04 05:00:05');
INSERT INTO `comments` VALUES (34, '@user 111', 6, 32, 19, 0, '2025-05-04 05:04:53');
INSERT INTO `comments` VALUES (35, '@user 222', 6, 34, 19, 0, '2025-05-04 05:04:57');
INSERT INTO `comments` VALUES (36, '11', 9, 0, 6, 0, '2025-05-04 05:05:41');
INSERT INTO `comments` VALUES (38, '222', 9, 0, 7, 0, '2025-05-04 13:19:26');
INSERT INTO `comments` VALUES (39, '1111', 9, 0, 67, 0, '2025-05-04 14:15:01');

-- ----------------------------
-- Table structure for ebook
-- ----------------------------
DROP TABLE IF EXISTS `ebook`;
CREATE TABLE `ebook`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `view_count` int NULL DEFAULT 0,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_published` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ebook
-- ----------------------------
INSERT INTO `ebook` VALUES (1, '简历', '我的简历', 3, 'http://110.42.213.154:9000/code-share/EbookImage/20250429_6442cb65a5644457bd07accc36591ffe.png', 'http://110.42.213.154:9000/code-share/EbookFile/20250429_96f8153deabe4108bd6852b0841d1043.pdf', 0, '2025-04-29 21:32:17', '2025-05-04 12:02:08');
INSERT INTO `ebook` VALUES (2, '他的简历', '11111111111222', 31, 'http://110.42.213.154:9000/code-share/EbookImage/20250429_dc4542aa8c2749afb304a69764427fba.png', 'http://110.42.213.154:9000/code-share/EbookFile/20250429_1fe381180d9946f1b300b79de791af5e.docx', 0, '2025-04-29 21:51:34', '2025-05-04 13:26:50');

-- ----------------------------
-- Table structure for exam_arrangements
-- ----------------------------
DROP TABLE IF EXISTS `exam_arrangements`;
CREATE TABLE `exam_arrangements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_plan_id` bigint NOT NULL COMMENT '关联的考试计划ID',
  `exam_room_id` bigint NOT NULL COMMENT '考场ID',
  `examinee_id` bigint NOT NULL COMMENT '考生ID',
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号',
  `status` enum('assigned','confirmed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'assigned' COMMENT '编排状态',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_exam_seat`(`exam_plan_id` ASC, `exam_room_id` ASC, `seat_number` ASC) USING BTREE,
  UNIQUE INDEX `unique_examinee_plan`(`exam_plan_id` ASC, `examinee_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试编排表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_arrangements
-- ----------------------------

-- ----------------------------
-- Table structure for exam_rooms
-- ----------------------------
DROP TABLE IF EXISTS `exam_rooms`;
CREATE TABLE `exam_rooms`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考场名称',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考场位置',
  `capacity` int NOT NULL COMMENT '考场容量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '考场描述',
  `status` enum('available','occupied','maintenance') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'available' COMMENT '考场状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考场表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_rooms
-- ----------------------------
INSERT INTO `exam_rooms` VALUES (1, '教学楼A101', '教学楼A栋1楼101室', 50, '多媒体教室，配备投影设备', 'available', '2025-05-28 17:25:52', '2025-05-28 17:25:52');
INSERT INTO `exam_rooms` VALUES (2, '教学楼A102', '教学楼A栋1楼102室', 45, '普通教室', 'available', '2025-05-28 17:25:52', '2025-05-28 17:25:52');
INSERT INTO `exam_rooms` VALUES (3, '教学楼A201', '教学楼A栋2楼201室', 60, '大教室，适合大型考试', 'available', '2025-05-28 17:25:52', '2025-05-28 17:25:52');
INSERT INTO `exam_rooms` VALUES (4, '教学楼B101', '教学楼B栋1楼101室', 40, '计算机机房', 'available', '2025-05-28 17:25:52', '2025-05-28 17:25:52');
INSERT INTO `exam_rooms` VALUES (5, '教学楼B102', '教学楼B栋1楼102室', 35, '小教室', 'maintenance', '2025-05-28 17:25:52', '2025-05-28 17:25:52');
INSERT INTO `exam_rooms` VALUES (6, '实验楼301', '实验楼3楼301室', 30, '实验室，适合实操考试', 'available', '2025-05-28 17:25:52', '2025-05-28 17:25:52');

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `follow_id_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follow
-- ----------------------------
INSERT INTO `follow` VALUES (1, 2, '[]');
INSERT INTO `follow` VALUES (2, 9, '[]');
INSERT INTO `follow` VALUES (3, 6, '[8,10]');

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `resource_id` int NULL DEFAULT NULL,
  `type` int NULL DEFAULT NULL COMMENT '0 博客 ；1 问答',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of likes
-- ----------------------------
INSERT INTO `likes` VALUES (1, 6, 1, 0);
INSERT INTO `likes` VALUES (2, 9, 12, 0);
INSERT INTO `likes` VALUES (3, 6, 12, 0);
INSERT INTO `likes` VALUES (5, 9, 7, 0);
INSERT INTO `likes` VALUES (6, 9, 21, 0);
INSERT INTO `likes` VALUES (7, 9, 62, 0);
INSERT INTO `likes` VALUES (8, 9, 6, 0);
INSERT INTO `likes` VALUES (9, 9, 67, 0);

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '提问唯一ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提问标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提问内容',
  `user_id` int NOT NULL COMMENT '提问者ID',
  `is_resolved` int NULL DEFAULT 0 COMMENT '是否已解决：0-未解决，1-已解决',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技术问题提问表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of questions
-- ----------------------------
INSERT INTO `questions` VALUES (2, 'Python中多进程共享变量异常', '# 问题：Python中多进程共享变量异常\n\n## 环境\n- Python 3.8.5\n- 操作系统：Ubuntu 20.04 LTS\n- 使用模块：`multiprocessing`\n\n## 问题描述\n尝试使用`multiprocessing.Value`在子进程间共享整数变量时，发现数值更新不同步。\n\n```python\nfrom multiprocessing import Process, Value\n\ndef worker(n):\n    for _ in range(1000):\n        n.value += 1\n\nif __name__ == \'__main__\':\n    num = Value(\'i\', 0)\n    processes = [Process(target=worker, args=(num,)) for _ in range(4)]\n    \n    for p in processes:\n        p.start()\n    for p in processes:\n        p.join()\n    \n    print(\"最终值:\", num.value)  # 预期4000，实际得到随机值', 6, 1, '2025-05-01 20:51:38', '2025-05-01 20:51:38');
INSERT INTO `questions` VALUES (3, 'Python异步编程中协程任务意外阻塞', '# 问题：Python异步编程中协程任务意外阻塞\n\n## 环境\n- Python 3.9.7\n- 异步框架：FastAPI 0.85.0\n- 数据库：MongoDB 5.0 (异步驱动 motor 3.1.1)\n\n## 问题描述\n在FastAPI路由中调用同步IO操作时，整个事件循环被阻塞。以下是简化后的代码：\n\n```python\nfrom fastapi import FastAPI\nimport time  # 模拟同步IO\n\napp = FastAPI()\n\n@app.get(\"/slow-endpoint\")\nasync def slow_operation():\n    # 异步数据库操作（正常）\n    # await collection.find_one(...)\n    \n    # 意外的同步阻塞调用\n    time.sleep(5)  # 这里导致整个应用停滞\n    return {\"status\": \"done\"}', 6, 1, '2025-05-01 20:53:50', '2025-05-01 20:53:50');
INSERT INTO `questions` VALUES (4, 'Java Stream并行处理出现非线程安全操作', '# 问题：Java Stream并行处理出现非线程安全操作\n\n## 环境\n- Java 17.0.2\n- 框架：Spring Boot 2.7.0\n- 数据结构：`ArrayList` → 并行`Stream`\n\n## 问题描述\n使用并行流处理集合时，遇到`ConcurrentModificationException`和非预期结果：\n\n```java\nList<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));\nList<String> result = Collections.synchronizedList(new ArrayList<>());\n\nnumbers.parallelStream()\n       .forEach(n -> {\n           // 模拟耗时操作\n           try { Thread.sleep(100); } \n           catch (InterruptedException e) { /*...*/ }\n           \n           // 非线程安全操作（实际业务更复杂）\n           result.add(\"Processed_\" + n); \n       });\n\nSystem.out.println(result.size()); // 预期5，实际随机出现3-5', 6, 1, '2025-05-01 20:56:54', '2025-05-01 20:56:54');
INSERT INTO `questions` VALUES (5, 'Go协程中共享Map的并发读写问题', '# 问题：Go协程中共享Map的并发读写问题\n\n## 环境\n- Go 1.21\n- 使用原生sync包\n- 操作系统：Linux 5.15\n\n## 问题描述\n多个goroutine并发读写全局map时出现fatal error甚至程序崩溃：\n\n```go\npackage main\n\nimport (\n	\"fmt\"\n	\"sync\"\n)\n\nvar cache = make(map[string]int) // 全局共享map\n\nfunc setValue(key string, value int, wg *sync.WaitGroup) {\n	defer wg.Done()\n	cache[key] = value // 并发写操作\n}\n\nfunc main() {\n	var wg sync.WaitGroup\n	keys := []string{\"a\", \"b\", \"c\", \"d\", \"e\"}\n\n	for i, key := range keys {\n		wg.Add(1)\n		go setValue(key, i, &wg) // 启动多个goroutine\n	}\n\n	wg.Wait()\n	fmt.Println(cache) \n	// 可能输出：fatal error: concurrent map writes\n}', 6, 1, '2025-05-01 20:58:14', '2025-05-01 20:58:14');
INSERT INTO `questions` VALUES (6, 'SSE如何实现流式返回', '11111111111111111111', 6, 1, '2025-05-04 13:22:07', '2025-05-04 13:22:07');
INSERT INTO `questions` VALUES (7, '1313132', '1+1=？111111', 9, 0, '2025-05-04 14:19:43', '2025-05-04 14:19:43');
INSERT INTO `questions` VALUES (8, '22222222222222', '222222222222222222222', 9, 0, '2025-05-04 14:57:36', '2025-05-04 14:57:36');

-- ----------------------------
-- Table structure for sections
-- ----------------------------
DROP TABLE IF EXISTS `sections`;
CREATE TABLE `sections`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '板块唯一ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '板块名称',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '板块描述',
  `admin_id` int NOT NULL COMMENT '创建板块的管理员ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '板块创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  CONSTRAINT `sections_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技术交流板块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sections
-- ----------------------------
INSERT INTO `sections` VALUES (1, '求职/招聘', 'Briefcase', '求职/招聘', 1, '2025-04-27 08:53:18');
INSERT INTO `sections` VALUES (2, '运维/测试', 'Promotion', '运维/测试', 1, '2025-04-27 08:54:25');
INSERT INTO `sections` VALUES (3, '云计算', 'Cloud', '云计算', 1, '2025-04-27 08:54:41');
INSERT INTO `sections` VALUES (4, '人工智能', 'SetUp', '人工智能', 1, '2025-04-27 08:55:25');
INSERT INTO `sections` VALUES (5, '安全技术', 'Lock', '安全技术', 1, '2025-04-27 08:55:36');
INSERT INTO `sections` VALUES (6, 'Java', 'Cpu', 'Java', 1, '2025-04-27 08:55:49');
INSERT INTO `sections` VALUES (7, 'Python', 'MagicStick', 'Python', 1, '2025-04-27 08:56:03');
INSERT INTO `sections` VALUES (8, '数据库', 'DataAnalysis', '数据库', 1, '2025-04-27 08:56:16');
INSERT INTO `sections` VALUES (9, '后端开发', 'Connection', '后端开发', 1, '2025-04-27 08:56:27');
INSERT INTO `sections` VALUES (10, '前端开发', 'Monitor', '前端开发', 1, '2025-04-27 08:56:42');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户邮箱',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像URL',
  `role` int NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员',
  `is_active` int NULL DEFAULT 0 COMMENT '账号状态：0-启用，1-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '163@qq.com', 'http://110.42.213.154:9000/code-share/avatar/20250430_3c84ec0e99a34c889e4f2c17c55c7c38.png', 1, 0, '2025-04-26 16:48:30', '2025-05-01 17:38:56', '2025-05-25 21:37:05');
INSERT INTO `user` VALUES (6, 'user', 'e10adc3949ba59abbe56e057f20f883e', '163@qq.com', 'http://110.42.213.154:9000/code-share/avatar/20250430_3c84ec0e99a34c889e4f2c17c55c7c38.png', 0, 0, '2025-04-27 01:13:24', '2025-05-25 21:48:40', '2025-05-28 15:24:29');
INSERT INTO `user` VALUES (8, 'user2', 'e10adc3949ba59abbe56e057f20f883e', '2521@qq.com', 'http://110.42.213.154:9000/code-share/avatar/20250430_3c84ec0e99a34c889e4f2c17c55c7c38.png', 0, 0, '2025-04-30 16:49:38', '2025-04-30 16:49:38', '2025-05-04 14:12:19');
INSERT INTO `user` VALUES (9, 'user3', 'e10adc3949ba59abbe56e057f20f883e', '123456@qq.com', 'http://110.42.213.154:9000/code-share/avatar/20250501_2170251b26bd4ce5a02921a15eaf8dad.png', 0, 0, '2025-05-01 21:04:05', '2025-05-01 21:04:33', '2025-05-04 14:12:34');

-- ----------------------------
-- Table structure for user_votes
-- ----------------------------
DROP TABLE IF EXISTS `user_votes`;
CREATE TABLE `user_votes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录唯一ID',
  `user_id` int NOT NULL COMMENT '投票用户ID',
  `vote_id` int NOT NULL COMMENT '关联的投票ID',
  `option_id` int NOT NULL COMMENT '选择的选项ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户投票记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_votes
-- ----------------------------
INSERT INTO `user_votes` VALUES (1, 6, 1, 1, '2025-05-02 18:45:13');
INSERT INTO `user_votes` VALUES (2, 6, 2, 3, '2025-05-02 18:55:18');
INSERT INTO `user_votes` VALUES (3, 9, 2, 4, '2025-05-02 19:09:02');
INSERT INTO `user_votes` VALUES (4, 8, 2, 3, '2025-05-02 19:09:34');
INSERT INTO `user_votes` VALUES (5, 10, 2, 3, '2025-05-02 19:10:32');
INSERT INTO `user_votes` VALUES (6, 10, 1, 1, '2025-05-02 20:10:59');
INSERT INTO `user_votes` VALUES (7, 10, 1, 2, '2025-05-02 20:10:59');
INSERT INTO `user_votes` VALUES (8, 9, 3, 5, '2025-05-04 13:24:32');
INSERT INTO `user_votes` VALUES (9, 6, 4, 12, '2025-05-04 14:23:43');
INSERT INTO `user_votes` VALUES (10, 9, 4, 9, '2025-05-04 14:23:51');

-- ----------------------------
-- Table structure for videos
-- ----------------------------
DROP TABLE IF EXISTS `videos`;
CREATE TABLE `videos`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '视频唯一ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视频描述',
  `video_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频存储路径',
  `cover_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图路径',
  `user_id` int NOT NULL COMMENT '上传用户ID',
  `is_published` int NULL DEFAULT 1 COMMENT '上架状态：0-下架，1-上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `download_count` int NULL DEFAULT NULL COMMENT '下载次数',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of videos
-- ----------------------------
INSERT INTO `videos` VALUES (2, '视频2', '不懂', 'http://110.42.213.154:9000/code-share/videoImage/20250430_8c851509e06a4835b08c7b69cca91100.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_db1e0297fda64385a075200d40351477.png', 1, 0, '2025-04-30 01:41:51', '2025-04-30 01:55:42', NULL, 1);
INSERT INTO `videos` VALUES (3, 'SppringAI', 'SppringAI', 'http://110.42.213.154:9000/code-share/videoImage/20250430_9a0d78ecbff348efa0a7bca5372852cf.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_5be67031c84b4701a2fabaf3408e1c9c.png', 1, 0, '2025-04-30 02:30:07', '2025-04-30 02:30:07', NULL, 0);
INSERT INTO `videos` VALUES (4, '鸿蒙5.0', '鸿蒙5.0教学', 'http://110.42.213.154:9000/code-share/videoImage/20250430_720bd096cc544527b9b0b064619b3044.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_376bdcf408ca4729b479e8cf0296b228.png', 1, 0, '2025-04-30 02:30:45', '2025-04-30 02:30:45', NULL, 0);
INSERT INTO `videos` VALUES (5, 'deep seek', 'deep seek', 'http://110.42.213.154:9000/code-share/videoImage/20250430_9cf3306bdbfe4dcd869a145bd058513d.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_b6a1c3e3f4854c6090b6af4de5a8c871.png', 1, 0, '2025-04-30 02:31:07', '2025-04-30 02:31:07', NULL, 1);
INSERT INTO `videos` VALUES (6, '本地部署大模型', '本地部署大模型', 'http://110.42.213.154:9000/code-share/videoImage/20250430_9ab45b6ac48443c793a736e8b147deca.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_c83a5194e3154383806c4316543bbf8f.png', 1, 0, '2025-04-30 02:31:43', '2025-04-30 02:31:43', NULL, 3);
INSERT INTO `videos` VALUES (7, '计算机二级', '计算机二级', 'http://110.42.213.154:9000/code-share/videoImage/20250430_ef2e5c56dfde4cb980ddb1ca4a867c61.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_8a6a6391b61f4b75a788aaf84569c356.png', 1, 0, '2025-04-30 02:32:29', '2025-04-30 02:32:29', NULL, 0);
INSERT INTO `videos` VALUES (8, 'Java函数编程', 'Java函数编程', 'http://110.42.213.154:9000/code-share/videoImage/20250430_dc3fa735517949938bda7e19efe28933.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250430_38b003b0e784481c81bba0bbe4ca729f.png', 1, 0, '2025-04-30 02:32:54', '2025-04-30 02:32:54', NULL, 1);
INSERT INTO `videos` VALUES (9, '11111111111122', '22222222222222', 'http://110.42.213.154:9000/code-share/videoFile/20250503_f494e3dfb3204e59a6a7a2077ebab695.mp4', 'http://110.42.213.154:9000/code-share/videoImage/20250503_3876b37abade497eb9ac389752b06a00.png', 6, 0, '2025-05-02 14:16:36', '2025-05-03 08:27:44', NULL, 24);
INSERT INTO `videos` VALUES (11, '111111111111', '111111111111111111111', 'http://110.42.213.154:9000/code-share/videoImage/20250504_0f8a6d1825ab4400afa68d889037ced8.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250504_ae14f152684948338d2f797c472d48ec.png', 6, 0, '2025-05-04 13:23:29', '2025-05-04 13:23:29', NULL, 1);
INSERT INTO `videos` VALUES (12, '11111111', '1111111111111111111', 'http://110.42.213.154:9000/code-share/videoImage/20250504_9c06c4533f3b427d82c23fd9c34775b6.mp4', 'http://110.42.213.154:9000/code-share/videoFile/20250504_844a9542fb424b4085e4c505dc33a1da.png', 9, 0, '2025-05-04 14:22:26', '2025-05-04 14:22:26', NULL, 0);

-- ----------------------------
-- Table structure for vote_options
-- ----------------------------
DROP TABLE IF EXISTS `vote_options`;
CREATE TABLE `vote_options`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '选项唯一ID',
  `vote_id` int NOT NULL COMMENT '关联的投票ID',
  `option_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投票选项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vote_options
-- ----------------------------
INSERT INTO `vote_options` VALUES (1, 1, '正确', '2025-05-02 17:45:17');
INSERT INTO `vote_options` VALUES (2, 1, '错误', '2025-05-02 17:45:17');
INSERT INTO `vote_options` VALUES (3, 2, '不吃', '2025-05-02 18:01:53');
INSERT INTO `vote_options` VALUES (4, 2, '吃', '2025-05-02 18:01:53');
INSERT INTO `vote_options` VALUES (5, 3, '1', '2025-05-04 13:24:19');
INSERT INTO `vote_options` VALUES (6, 3, '2', '2025-05-04 13:24:19');
INSERT INTO `vote_options` VALUES (7, 3, '3', '2025-05-04 13:24:19');
INSERT INTO `vote_options` VALUES (8, 3, '4', '2025-05-04 13:24:19');
INSERT INTO `vote_options` VALUES (9, 4, '1', '2025-05-04 14:23:30');
INSERT INTO `vote_options` VALUES (10, 4, '2', '2025-05-04 14:23:30');
INSERT INTO `vote_options` VALUES (11, 4, '3', '2025-05-04 14:23:30');
INSERT INTO `vote_options` VALUES (12, 4, '4', '2025-05-04 14:23:30');

-- ----------------------------
-- Table structure for votes
-- ----------------------------
DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '投票唯一ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '投票标题',
  `user_id` int NOT NULL COMMENT '发起用户ID',
  `vote_type` int NULL DEFAULT 0 COMMENT '投票类型：0-单选，1-多选',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `end_time` datetime NOT NULL COMMENT '截止时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投票信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of votes
-- ----------------------------
INSERT INTO `votes` VALUES (1, '投票', 6, 1, '2025-05-02 17:45:17', '2025-10-01 08:00:00');
INSERT INTO `votes` VALUES (2, '香菜吃不吃', 6, 0, '2025-05-02 18:01:54', '2025-05-09 08:00:00');
INSERT INTO `votes` VALUES (3, '测试投票111', 6, 0, '2025-05-04 13:24:20', '2025-05-14 08:00:00');
INSERT INTO `votes` VALUES (4, '1111111111111', 9, 0, '2025-05-04 14:23:30', '2025-05-23 08:00:00');

SET FOREIGN_KEY_CHECKS = 1;
