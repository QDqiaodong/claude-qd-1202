package com.antique.resale.settlement;

import com.antique.resale.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService service;

    public SettlementController(SettlementService service) {
        this.service = service;
    }

    @GetMapping("/buyers")
    public ApiResponse<List<Buyer>> listBuyers() {
        return ApiResponse.success(service.listBuyers());
    }

    @PostMapping("/buyers")
    public ApiResponse<Buyer> createBuyer(@RequestBody Buyer input) {
        return ApiResponse.success(service.createBuyer(input));
    }

    @GetMapping("/deals")
    public ApiResponse<List<Deal>> listDeals(@RequestParam(required = false) String settlementStatus) {
        return ApiResponse.success(service.listDeals(settlementStatus));
    }

    @PostMapping("/deals")
    public ApiResponse<Deal> createDeal(@RequestBody Deal input) {
        return ApiResponse.success(service.createDeal(input));
    }

    @PostMapping("/deals/{id}/settle")
    public ApiResponse<Deal> settle(@PathVariable Long id) {
        return ApiResponse.success(service.settle(id));
    }
}
