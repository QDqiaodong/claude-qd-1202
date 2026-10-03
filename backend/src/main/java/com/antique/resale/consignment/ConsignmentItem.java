package com.antique.resale.consignment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 寄卖品台账：登记委托方寄卖的古玩（瓷器/字画/玉器/杂项）。
 * 实体字段一律不写 Java 初值，默认值在 Service 的 create 里给。
 */
@Entity
@Table(name = "consignment_item")
public class ConsignmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 寄卖编号（唯一） */
    public String code;

    /** 品类：瓷器 / 字画 / 玉器 / 杂项 */
    public String category;

    /** 名称 */
    public String name;

    /** 年代 */
    public String era;

    /** 委托方 */
    public String consignor;

    /** 估价（必填且 > 0） */
    public Double estimate;

    /** 状态：在售 / 已预订 / 已售出 / 已撤回 */
    public String status;

    public LocalDateTime createdAt = LocalDateTime.now();
}
