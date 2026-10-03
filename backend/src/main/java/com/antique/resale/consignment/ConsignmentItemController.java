package com.antique.resale.consignment;

import com.antique.resale.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ConsignmentItemController {

    private final ConsignmentItemService service;

    public ConsignmentItemController(ConsignmentItemService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ConsignmentItem>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(category, status));
    }

    @PostMapping
    public ApiResponse<ConsignmentItem> create(@RequestBody ConsignmentItem input) {
        return ApiResponse.success(service.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConsignmentItem> update(@PathVariable Long id, @RequestBody ConsignmentItem input) {
        return ApiResponse.success(service.update(id, input));
    }
}
