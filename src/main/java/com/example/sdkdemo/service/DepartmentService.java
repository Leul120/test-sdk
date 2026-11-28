package com.example.sdkdemo.service;

import com.example.sdkdemo.dto.DepartmentBudgetRequest;
import com.example.sdkdemo.exception.ResourceNotFoundException;
import com.example.sdkdemo.model.Department;
import com.example.sdkdemo.model.Project;
import com.example.sdkdemo.repository.DepartmentRepository;
import com.example.sdkdemo.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department allocateAdditionalBudget(DepartmentBudgetRequest request) {
        Department dept = departmentRepository.findByNameIgnoreCase(request.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentName()));
        dept.setAnnualBudget(dept.getAnnualBudget().add(request.getAdditionalBudget()));
        Department saved = departmentRepository.save(dept);
        auditService.record("DEPARTMENT", saved.getCostCenterCode(), "BUDGET_INCREASED",
                request.getRequestedByEmail(), "Added=" + request.getAdditionalBudget());
        return saved;
    }

    public Map<String, Object> getDepartmentUtilization(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
        List<Project> projects = projectRepository.findByDepartmentId(departmentId);

        BigDecimal utilizationPct = dept.getSpentBudget()
                .multiply(BigDecimal.valueOf(100))
                .divide(dept.getAnnualBudget(), 2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("department", dept.getName());
        result.put("costCenter", dept.getCostCenterCode());
        result.put("annualBudget", dept.getAnnualBudget());
        result.put("spentBudget", dept.getSpentBudget());
        result.put("utilizationPercent", utilizationPct);
        result.put("activeProjects", projects.size());
        return result;
    }
}
