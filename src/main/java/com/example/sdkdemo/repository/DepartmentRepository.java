package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);
    Optional<Department> findByCostCenterCode(String costCenterCode);
    List<Department> findByActiveTrue();
}
