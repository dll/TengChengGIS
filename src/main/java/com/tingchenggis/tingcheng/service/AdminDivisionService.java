package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.AdminDivision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AdminDivisionService {

    AdminDivision createAdminDivision(AdminDivision adminDivision);

    Optional<AdminDivision> getAdminDivisionById(Long id);

    Page<AdminDivision> getAllAdminDivisions(Pageable pageable);

    List<AdminDivision> getAllAdminDivisions();

    AdminDivision updateAdminDivision(Long id, AdminDivision adminDivision);

    void deleteAdminDivision(Long id);

    List<AdminDivision> findByAdminLevel(String adminLevel);

    List<AdminDivision> findByParentId(Long parentId);

    List<AdminDivision> findByNameContaining(String name);

    List<Map<String, Object>> getTree();

    Map<String, Object> getStats();
}
