package com.antique.resale.consignment;

import com.antique.resale.common.BizException;
import com.antique.resale.common.Dict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsignmentItemService {

    private final ConsignmentItemRepository repository;

    public ConsignmentItemService(ConsignmentItemRepository repository) {
        this.repository = repository;
    }

    public List<ConsignmentItem> list(String category, String status) {
        if (category != null && !category.isBlank() && status != null && !status.isBlank()) {
            return repository.findByCategoryAndStatus(category, status);
        }
        if (category != null && !category.isBlank()) {
            return repository.findByCategory(category);
        }
        if (status != null && !status.isBlank()) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public ConsignmentItem create(ConsignmentItem input) {
        ConsignmentItem saved = new ConsignmentItem();
        if (input.code == null || input.code.isBlank()) {
            throw new BizException("寄卖编号不能为空");
        }
        saved.code = input.code;
        if (input.name == null || input.name.isBlank()) {
            throw new BizException("寄卖品名称不能为空");
        }
        saved.name = input.name;
        if (!Dict.isValidItemCategory(input.category)) {
            throw new BizException("品类必须是：瓷器 / 字画 / 玉器 / 杂项");
        }
        saved.category = input.category;
        saved.era = (input.era == null || input.era.isBlank()) ? "不详" : input.era;
        saved.consignor = (input.consignor == null || input.consignor.isBlank()) ? "匿名委托方" : input.consignor;
        if (input.estimate == null || input.estimate <= 0) {
            throw new BizException("估价必填且必须大于 0");
        }
        saved.estimate = input.estimate;
        // 默认状态：在售
        saved.status = (input.status == null || input.status.isBlank())
                ? "在售"
                : (Dict.isValidItemStatus(input.status) ? input.status : "在售");
        return repository.save(saved);
    }

    public ConsignmentItem update(Long id, ConsignmentItem input) {
        ConsignmentItem existing = repository.findById(id)
                .orElseThrow(() -> new BizException("寄卖品不存在"));
        // 已售出：锁价，不可改估价
        if ("已售出".equals(existing.status) && input.estimate != null) {
            throw new BizException("已售出的寄卖品不可改价");
        }
        // 已撤回：不可再售（这里拦截把状态改回在售/已预订/已售出）
        if ("已撤回".equals(existing.status)
                && input.status != null && !input.status.isBlank()
                && Dict.ITEM_STATUS.contains(input.status)
                && !"已撤回".equals(input.status)) {
            throw new BizException("已撤回的寄卖品不可再上架");
        }
        if (input.name != null && !input.name.isBlank()) {
            existing.name = input.name;
        }
        if (input.category != null && !input.category.isBlank()) {
            if (!Dict.isValidItemCategory(input.category)) {
                throw new BizException("品类必须是：瓷器 / 字画 / 玉器 / 杂项");
            }
            existing.category = input.category;
        }
        if (input.era != null && !input.era.isBlank()) {
            existing.era = input.era;
        }
        if (input.consignor != null && !input.consignor.isBlank()) {
            existing.consignor = input.consignor;
        }
        if (input.estimate != null) {
            if (input.estimate <= 0) {
                throw new BizException("估价必须大于 0");
            }
            existing.estimate = input.estimate;
        }
        return repository.save(existing);
    }

    public ConsignmentItem getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BizException("寄卖品不存在"));
    }
}
