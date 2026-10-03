package com.antique.resale.common;

import java.util.List;

/**
 * 古玩寄卖行业务词典与规则参数表：寄卖品类 / 寄卖状态、估价状态、
 * 结算状态、买家级别、寄卖状态机。规则集中在此，便于出题时被要求调整或扩展。
 */
public final class Dict {

    private Dict() {
    }

    // ---------------- 寄卖品 ----------------
    /** 瓷器 / 字画 / 玉器 / 杂项 */
    public static final List<String> ITEM_CATEGORIES = List.of("瓷器", "字画", "玉器", "杂项");

    /** 在售 / 已预订 / 已售出 / 已撤回 */
    public static final List<String> ITEM_STATUS = List.of("在售", "已预订", "已售出", "已撤回");

    // ---------------- 估价记录 ----------------
    /** 待评 / 已评 */
    public static final List<String> APPRAISAL_STATUS = List.of("待评", "已评");

    // ---------------- 买家与结算 ----------------
    /** 新客 / 会员 / VIP */
    public static final List<String> BUYER_LEVELS = List.of("新客", "会员", "VIP");

    /** 未结算 / 已结算 */
    public static final List<String> SETTLEMENT_STATUS = List.of("未结算", "已结算");

    // ---------------- 取值方法 ----------------

    public static boolean isValidItemCategory(String category) {
        return category != null && ITEM_CATEGORIES.contains(category);
    }

    public static boolean isValidItemStatus(String status) {
        return status != null && ITEM_STATUS.contains(status);
    }

    public static boolean isValidAppraisalStatus(String status) {
        return status != null && APPRAISAL_STATUS.contains(status);
    }

    public static boolean isValidBuyerLevel(String level) {
        return level != null && BUYER_LEVELS.contains(level);
    }

    public static boolean isValidSettlementStatus(String status) {
        return status != null && SETTLEMENT_STATUS.contains(status);
    }

    /**
     * 寄卖状态机：
     * 在售 / 已预订 可互相流转，也可走向 已售出 / 已撤回；
     * 已售出（锁价终态）、已撤回（不可再上架终态）为不可离开的终态。
     * 同态、非法跳转一律拒绝。
     */
    public static boolean canTransferStatus(String from, String to) {
        if (from == null || to == null) {
            return false;
        }
        if (!ITEM_STATUS.contains(from) || !ITEM_STATUS.contains(to)) {
            return false;
        }
        if (from.equals(to)) {
            return false;
        }
        // 终态：已售出 / 已撤回 不可再流转
        if ("已售出".equals(from) || "已撤回".equals(from)) {
            return false;
        }
        // 起点为 在售 / 已预订，目标可为任意其它合法状态
        return true;
    }
}
