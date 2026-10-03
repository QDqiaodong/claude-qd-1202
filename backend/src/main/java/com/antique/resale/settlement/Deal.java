package com.antique.resale.settlement;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成交记录：买家买走某寄卖品，含成交价、佣金比例、佣金金额、结算状态。
 * 实体字段一律不写 Java 初值，默认值在 Service 的 create 里给。
 */
@Entity
@Table(name = "deal")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 买家 id */
    public Long buyerId;

    /** 寄卖品 id */
    public Long itemId;

    /** 成交价 */
    public Double dealPrice;

    /** 佣金比例（如 0.1 表示 10%） */
    public Double commissionRate;

    /** 佣金金额 = 成交价 × 比例 */
    public Double commissionAmount;

    /** 结算状态：未结算 / 已结算 */
    public String settlementStatus;

    /** 成交日期 */
    public LocalDate dealDate;

    public LocalDateTime createdAt = LocalDateTime.now();
}
