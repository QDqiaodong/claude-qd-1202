package com.antique.resale.consignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsignmentItemRepository extends JpaRepository<ConsignmentItem, Long> {

    List<ConsignmentItem> findByCategory(String category);

    List<ConsignmentItem> findByStatus(String status);

    List<ConsignmentItem> findByCategoryAndStatus(String category, String status);
}
