package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(String role);
    
    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE department = :department", nativeQuery = true)
    List<User> findUsersByDepartmentNative(@Param("department") String department);

    Optional<User> findByDepartment(String department);

    @Query(value = "SELECT * FROM users WHERE user_role = :role", nativeQuery = true)
    List<User> findUsersForRoleAudit(@Param("role") String role);

    Optional<User> findByDepartmentAndActive(String department, Boolean active);

    @Query(value = "SELECT * FROM users WHERE department = :department AND active = true", nativeQuery = true)
    List<User> findActiveDepartmentMembers(@Param("department") String department);
}
