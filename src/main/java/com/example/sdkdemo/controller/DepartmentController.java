package com.example.sdkdemo.controller;

import com.example.sdkdemo.dto.ApiResponse;
import com.example.sdkdemo.dto.DepartmentBudgetRequest;
import com.example.sdkdemo.model.Department;
import com.example.sdkdemo.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> listDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    @PostMapping("/budget")
    public ResponseEntity<ApiResponse<Department>> increaseBudget(@RequestBody DepartmentBudgetRequest request) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.allocateAdditionalBudget(request), "Budget updated"));
    }

    @GetMapping("/{id}/utilization")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUtilization(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentUtilization(id)));
    }
}
