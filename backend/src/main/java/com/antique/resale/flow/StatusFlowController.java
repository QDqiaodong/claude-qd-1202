package com.antique.resale.flow;

import com.antique.resale.common.ApiResponse;
import com.antique.resale.consignment.ConsignmentItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flow")
public class StatusFlowController {

    private final StatusFlowService service;

    public StatusFlowController(StatusFlowService service) {
        this.service = service;
    }

    @GetMapping("/history")
    public ApiResponse<List<StatusHistory>> history(@RequestParam(required = false) Long itemId) {
        return ApiResponse.success(service.history(itemId));
    }

    @PostMapping("/items/{id}/transition")
    public ApiResponse<ConsignmentItem> transition(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String toStatus = body.get("to");
        String operator = body.get("operator");
        String remark = body.get("remark");
        return ApiResponse.success(service.transition(id, toStatus, operator, remark));
    }
}
