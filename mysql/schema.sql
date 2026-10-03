SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- ---------------------------------------------------------------------
-- 古玩寄卖行 · 寄卖品台账 / 估价记录 / 状态流转 / 买家与结算
-- 库名 antique_resale；application.yml 里 ddl-auto: none，表结构全部由本文件在 MySQL 镜像构建期导入。
-- 注意：mysql 容器是 UTC，时间戳一律写 UTC_TIMESTAMP() / NOW()，日期用 DATE_SUB(CURDATE(), INTERVAL N DAY)。

CREATE DATABASE IF NOT EXISTS antique_resale
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE antique_resale;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS status_history;
DROP TABLE IF EXISTS deal;
DROP TABLE IF EXISTS buyer;
DROP TABLE IF EXISTS appraisal;
DROP TABLE IF EXISTS consignment_item;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------- 寄卖品台账（主实体） ----------------
CREATE TABLE consignment_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(32) NOT NULL UNIQUE COMMENT '寄卖编号',
    category     VARCHAR(16) COMMENT '瓷器 / 字画 / 玉器 / 杂项',
    name         VARCHAR(128) NOT NULL COMMENT '名称',
    era          VARCHAR(64) COMMENT '年代',
    consignor    VARCHAR(128) COMMENT '委托方',
    estimate     DOUBLE NOT NULL COMMENT '估价（必填 > 0）',
    status       VARCHAR(16) COMMENT '在售 / 已预订 / 已售出 / 已撤回',
    created_at   DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 估价记录（依赖寄卖品） ----------------
CREATE TABLE appraisal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id         BIGINT NOT NULL COMMENT '寄卖品 id',
    appraiser       VARCHAR(64) COMMENT '评估师',
    amount          DOUBLE COMMENT '估价金额（必须 > 0）',
    appraisal_date  DATE COMMENT '估价日期',
    status          VARCHAR(16) COMMENT '待评 / 已评',
    created_at      DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 状态流转留痕（依赖寄卖品） ----------------
CREATE TABLE status_history (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id      BIGINT NOT NULL COMMENT '寄卖品 id',
    from_status  VARCHAR(16) COMMENT '流转前状态',
    to_status    VARCHAR(16) COMMENT '流转后状态',
    operator     VARCHAR(64) COMMENT '操作人',
    remark       VARCHAR(255) COMMENT '备注',
    created_at   DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 买家与结算 ----------------
CREATE TABLE buyer (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(64) NOT NULL COMMENT '买家姓名',
    phone      VARCHAR(24) COMMENT '联系电话',
    level      VARCHAR(16) COMMENT '新客 / 会员 / VIP',
    created_at DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE deal (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id           BIGINT NOT NULL COMMENT '买家 id',
    item_id            BIGINT NOT NULL COMMENT '寄卖品 id',
    deal_price         DOUBLE COMMENT '成交价',
    commission_rate    DOUBLE COMMENT '佣金比例',
    commission_amount  DOUBLE COMMENT '佣金金额 = 成交价 × 比例',
    settlement_status  VARCHAR(16) COMMENT '未结算 / 已结算',
    deal_date          DATE COMMENT '成交日期',
    created_at         DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================== 中文种子数据 =====================

-- 寄卖品台账：含在售/已预订/已售出/已撤回 各态
INSERT INTO consignment_item (id, code, category, name, era, consignor, estimate, status, created_at) VALUES
(1, 'GM-1001', '瓷器', '青花缠枝莲纹瓶', '清乾隆', '王氏藏家', 120000, '在售', NOW()),
(2, 'GM-1002', '玉器', '白玉龙纹佩',     '明代',   '李记古玩', 38000,  '已预订', NOW()),
(3, 'GM-1003', '字画', '山水立轴',       '民国',   '张居士',   22000,  '已售出', NOW()),
(4, 'GM-1004', '杂项', '铜香炉',         '清代',   '陈掌柜',   15000,  '已撤回', NOW()),
(5, 'GM-1005', '瓷器', '粉彩花鸟罐',     '清光绪', '王氏藏家', 45000,  '在售', NOW()),
(6, 'GM-1006', '杂项', '汉砖砚',         '汉代',   '赵文房',   8000,   '在售', NOW());

-- 估价记录：同一物品多次估价留痕；含一条金额为 0 的异常态（触发「估价金额必须 > 0」）
INSERT INTO appraisal (id, item_id, appraiser, amount, appraisal_date, status, created_at) VALUES
(1, 1, '周明', 120000, DATE_SUB(CURDATE(), INTERVAL 20 DAY), '待评', NOW()),
(2, 1, '周明', 118000, DATE_SUB(CURDATE(), INTERVAL 18 DAY), '已评', NOW()),
(3, 3, '林芳', 22000,  DATE_SUB(CURDATE(), INTERVAL 15 DAY), '已评', NOW()),
(4, 2, '林芳', 40000,  DATE_SUB(CURDATE(), INTERVAL 12 DAY), '已评', NOW()),
(5, 5, '吴磊', 0,       DATE_SUB(CURDATE(), INTERVAL 5 DAY),  '待评', NOW());

-- 状态流转留痕：已售出 / 已撤回 均为终态
INSERT INTO status_history (id, item_id, from_status, to_status, operator, remark, created_at) VALUES
(1, 3, '在售', '已售出', '系统', '售出留痕', NOW()),
(2, 4, '在售', '已撤回', '系统', '委托方撤回', NOW());

-- 买家
INSERT INTO buyer (id, name, phone, level, created_at) VALUES
(1, '收藏家刘先生', '13800001111', 'VIP',  NOW()),
(2, '画廊采购部',   '13900002222', '会员', NOW()),
(3, '散客孙女士',   '13700003333', '新客', NOW());

-- 成交与结算：
-- deal 1：正常成交（成交价 = 估价，未结算）
-- deal 2：异常态——成交价 18000 低于估价红线 22000（触发「成交价不得低于估价红线」）
-- deal 3：异常态——成交品 GM-1002 为「已预订」非「已售出」（触发「只有已售出才能结算」）
INSERT INTO deal (id, buyer_id, item_id, deal_price, commission_rate, commission_amount, settlement_status, deal_date, created_at) VALUES
(1, 1, 3, 22000, 0.10, 2200.00, '未结算', DATE_SUB(CURDATE(), INTERVAL 30 DAY), NOW()),
(2, 2, 3, 18000, 0.10, 1800.00, '未结算', DATE_SUB(CURDATE(), INTERVAL 28 DAY), NOW()),
(3, 3, 2, 40000, 0.10, 4000.00, '未结算', DATE_SUB(CURDATE(), INTERVAL 25 DAY), NOW());
