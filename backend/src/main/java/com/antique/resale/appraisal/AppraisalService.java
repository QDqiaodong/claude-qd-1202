package com.antique.resale.appraisal;

import com.antique.resale.common.BizException;
import com.antique.resale.common.Dict;
import com.antique.resale.consignment.ConsignmentItem;
import com.antique.resale.consignment.ConsignmentItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppraisalService {

    private final AppraisalRepository repository;
    private final ConsignmentItemRepository itemRepository;

    public AppraisalService(AppraisalRepository repository, ConsignmentItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    public List<Appraisal> list(Long itemId, String status) {
        if (itemId != null) {
            if (status != null && !status.isBlank()) {
                return repository.findByItemIdAndStatus(itemId, status);
            }
            return repository.findByItemId(itemId);
        }
        if (status != null && !status.isBlank()) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    /** 创建估价：同一物品可多次估价留痕；金额必须 > 0；物品必须存在 */
    public Appraisal create(Appraisal input) {
        if (input.itemId == null) {
            throw new BizException("估价必须关联一件寄卖品");
        }
        ConsignmentItem item = itemRepository.findById(input.itemId)
                .orElseThrow(() -> new BizException("关联的寄卖品不存在"));
        Appraisal saved = new Appraisal();
        saved.itemId = item.id;
        saved.appraiser = (input.appraiser == null || input.appraiser.isBlank()) ? "未署名评估师" : input.appraiser;
        if (input.amount == null || input.amount <= 0) {
            throw new BizException("估价金额必须大于 0");
        }
        saved.amount = input.amount;
        saved.appraisalDate = (input.appraisalDate == null) ? LocalDate.now() : input.appraisalDate;
        // 默认状态：待评
        saved.status = (input.status == null || input.status.isBlank())
                ? "待评"
                : (Dict.isValidAppraisalStatus(input.status) ? input.status : "待评");
        return repository.save(saved);
    }

    /** 完成估价：仅 待评 可置为 已评 */
    public Appraisal complete(Long id) {
        Appraisal existing = repository.findById(id)
                .orElseThrow(() -> new BizException("估价记录不存在"));
        if (!"待评".equals(existing.status)) {
            throw new BizException("仅待评的估价可标记为已评");
        }
        existing.status = "已评";
        return repository.save(existing);
    }
}
