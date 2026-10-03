package com.antique.resale.settlement;

import com.antique.resale.common.BizException;
import com.antique.resale.common.Dict;
import com.antique.resale.consignment.ConsignmentItem;
import com.antique.resale.consignment.ConsignmentItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SettlementService {

    private final BuyerRepository buyerRepository;
    private final DealRepository dealRepository;
    private final ConsignmentItemRepository itemRepository;

    public SettlementService(BuyerRepository buyerRepository,
                             DealRepository dealRepository,
                             ConsignmentItemRepository itemRepository) {
        this.buyerRepository = buyerRepository;
        this.dealRepository = dealRepository;
        this.itemRepository = itemRepository;
    }

    // ---------------- 买家 ----------------
    public List<Buyer> listBuyers() {
        return buyerRepository.findAll();
    }

    public Buyer createBuyer(Buyer input) {
        Buyer saved = new Buyer();
        if (input.name == null || input.name.isBlank()) {
            throw new BizException("买家姓名不能为空");
        }
        saved.name = input.name;
        saved.phone = (input.phone == null) ? "" : input.phone;
        saved.level = (input.level == null || input.level.isBlank())
                ? "新客"
                : (Dict.isValidBuyerLevel(input.level) ? input.level : "新客");
        return buyerRepository.save(saved);
    }

    // ---------------- 成交与结算 ----------------
    public List<Deal> listDeals(String settlementStatus) {
        if (settlementStatus != null && !settlementStatus.isBlank()) {
            return dealRepository.findBySettlementStatus(settlementStatus);
        }
        return dealRepository.findAll();
    }

    /**
     * 创建成交记录：
     * 1) 买家、寄卖品必须存在；
     * 2) 寄卖品必须是「已售出」方可结算（锁价终态）；
     * 3) 成交价不得低于估价红线（= 寄卖品估价）；
     * 4) 佣金金额 = 成交价 × 佣金比例（自动计算）。
     */
    public Deal createDeal(Deal input) {
        if (input.buyerId == null) {
            throw new BizException("成交必须关联一位买家");
        }
        Buyer buyer = buyerRepository.findById(input.buyerId)
                .orElseThrow(() -> new BizException("买家不存在"));
        if (input.itemId == null) {
            throw new BizException("成交必须关联一件寄卖品");
        }
        ConsignmentItem item = itemRepository.findById(input.itemId)
                .orElseThrow(() -> new BizException("寄卖品不存在"));
        if (!"已售出".equals(item.status)) {
            throw new BizException("只有「已售出」的寄卖品才能结算成交");
        }
        if (input.dealPrice == null || input.dealPrice <= 0) {
            throw new BizException("成交价必须大于 0");
        }
        // 估价红线拦截
        if (item.estimate != null && input.dealPrice < item.estimate) {
            throw new BizException("成交价不得低于估价红线（" + item.estimate + "）");
        }
        if (input.commissionRate == null || input.commissionRate < 0) {
            throw new BizException("佣金比例不能为负");
        }

        Deal saved = new Deal();
        saved.buyerId = buyer.id;
        saved.itemId = item.id;
        saved.dealPrice = input.dealPrice;
        saved.commissionRate = input.commissionRate;
        saved.commissionAmount = input.dealPrice * input.commissionRate;
        saved.dealDate = (input.dealDate == null) ? LocalDate.now() : input.dealDate;
        // 默认结算状态：未结算
        saved.settlementStatus = (input.settlementStatus == null || input.settlementStatus.isBlank())
                ? "未结算"
                : (Dict.isValidSettlementStatus(input.settlementStatus) ? input.settlementStatus : "未结算");
        return dealRepository.save(saved);
    }

    /** 结算：仅 未结算 可置为 已结算 */
    public Deal settle(Long id) {
        Deal existing = dealRepository.findById(id)
                .orElseThrow(() -> new BizException("成交记录不存在"));
        if (!"未结算".equals(existing.settlementStatus)) {
            throw new BizException("仅未结算的成交可结算");
        }
        existing.settlementStatus = "已结算";
        return dealRepository.save(existing);
    }
}
