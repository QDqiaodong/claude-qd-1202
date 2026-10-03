package com.antique.resale.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findBySettlementStatus(String status);

    List<Deal> findByBuyerId(Long buyerId);

    List<Deal> findByItemId(Long itemId);
}
