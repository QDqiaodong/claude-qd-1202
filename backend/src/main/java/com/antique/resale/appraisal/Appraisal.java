package com.antique.resale.appraisal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 估价记录：每件寄卖品可有多条估价留痕。
 * 实体字段一律不写 Java 初值，默认值在 Service 的 create 里给。
 */
@Entity
@Table(name = "appraisal")
public class Appraisal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 关联寄卖品 id */
    public Long itemId;

    /** 评估师 */
    public String appraiser;

    /** 估价金额（必须 > 0） */
    public Double amount;

    /** 估价日期 */
    public LocalDate appraisalDate;

    /** 状态：待评 / 已评 */
    public String status;

    public LocalDateTime createdAt = LocalDateTime.now();
}
