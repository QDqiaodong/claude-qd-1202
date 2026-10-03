package com.antique.resale.flow;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 状态流转留痕：每次合法的寄卖状态跳转记一条。
 * 实体字段一律不写 Java 初值，默认值在 Service 的 create 里给。
 */
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 关联寄卖品 id */
    public Long itemId;

    /** 流转前状态 */
    public String fromStatus;

    /** 流转后状态 */
    public String toStatus;

    /** 操作人 */
    public String operator;

    /** 备注 */
    public String remark;

    public LocalDateTime createdAt = LocalDateTime.now();
}
