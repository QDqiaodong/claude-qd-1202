package com.antique.resale.appraisal;

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
@RequestMapping("/api/appraisals")
public class AppraisalController {

    private final AppraisalService service;

    public AppraisalController(AppraisalService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Appraisal>> list(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(itemId, status));
    }

    @PostMapping
    public ApiResponse<Appraisal> create(@RequestBody Appraisal input) {
        return ApiResponse.success(service.create(input));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Appraisal> complete(@PathVariable Long id) {
        return ApiResponse.success(service.complete(id));
    }
}
