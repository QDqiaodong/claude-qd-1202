package com.antique.resale.appraisal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {

    List<Appraisal> findByItemId(Long itemId);

    List<Appraisal> findByStatus(String status);

    List<Appraisal> findByItemIdAndStatus(Long itemId, String status);
}
