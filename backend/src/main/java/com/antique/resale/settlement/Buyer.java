package com.antique.resale.settlement;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 买家信息。
 * 实体字段一律不写 Java 初值，默认值在 Service 的 create 里给。
 */
@Entity
@Table(name = "buyer")
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 买家姓名 */
    public String name;

    /** 联系电话 */
    public String phone;

    /** 级别：新客 / 会员 / VIP */
    public String level;

    public LocalDateTime createdAt = LocalDateTime.now();
}
