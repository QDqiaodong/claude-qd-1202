package com.antique.resale.flow;

import com.antique.resale.common.BizException;
import com.antique.resale.common.Dict;
import com.antique.resale.consignment.ConsignmentItem;
import com.antique.resale.consignment.ConsignmentItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusFlowService {

    private final ConsignmentItemRepository itemRepository;
    private final StatusHistoryRepository historyRepository;

    public StatusFlowService(ConsignmentItemRepository itemRepository,
                             StatusHistoryRepository historyRepository) {
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
    }

    /** 查询某寄卖品的状态流转留痕 */
    public List<StatusHistory> history(Long itemId) {
        if (itemId == null) {
            return historyRepository.findAll();
        }
        return historyRepository.findByItemId(itemId);
    }

    /**
     * 状态机流转：校验合法跳转，写入留痕，更新寄卖品状态。
     * 非法跳转（如 已售出→在售、已撤回→在售、同态）一律抛出中文业务异常。
     */
    public ConsignmentItem transition(Long itemId, String toStatus, String operator, String remark) {
        if (itemId == null) {
            throw new BizException("流转必须关联一件寄卖品");
        }
        ConsignmentItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BizException("寄卖品不存在"));
        if (!Dict.isValidItemStatus(toStatus)) {
            throw new BizException("目标状态必须是：在售 / 已预订 / 已售出 / 已撤回");
        }
        if (!Dict.canTransferStatus(item.status, toStatus)) {
            throw new BizException(
                    "非法状态跳转：「" + item.status + "」不可流转为「" + toStatus + "」（已售出锁价、已撤回不可再上架）");
        }
        String from = item.status;
        item.status = toStatus;
        ConsignmentItem saved = itemRepository.save(item);

        StatusHistory log = new StatusHistory();
        log.itemId = item.id;
        log.fromStatus = from;
        log.toStatus = toStatus;
        log.operator = (operator == null || operator.isBlank()) ? "系统" : operator;
        log.remark = (remark == null) ? "" : remark;
        historyRepository.save(log);
        return saved;
    }
}
