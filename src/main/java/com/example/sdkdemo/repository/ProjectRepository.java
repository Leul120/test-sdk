package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCode(String code);
    List<Project> findByDepartmentId(Long departmentId);
    List<Project> findByStatus(String status);
}
