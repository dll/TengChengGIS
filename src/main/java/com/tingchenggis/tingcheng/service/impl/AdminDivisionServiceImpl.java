package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.AdminDivision;
import com.tingchenggis.tingcheng.repository.AdminDivisionRepository;
import com.tingchenggis.tingcheng.service.AdminDivisionCollectorService;
import com.tingchenggis.tingcheng.service.AdminDivisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AdminDivisionServiceImpl implements AdminDivisionService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDivisionServiceImpl.class);

    private final AdminDivisionRepository adminDivisionRepository;
    private final AdminDivisionCollectorService collectorService;

    public AdminDivisionServiceImpl(AdminDivisionRepository adminDivisionRepository,
                                     AdminDivisionCollectorService collectorService) {
        this.adminDivisionRepository = adminDivisionRepository;
        this.collectorService = collectorService;
    }

    @Override
    public AdminDivision createAdminDivision(AdminDivision adminDivision) {
        logger.info("Creating admin division: {}", adminDivision.getName());
        adminDivision.setId(null);
        return adminDivisionRepository.save(adminDivision);
    }

    @Override
    public Optional<AdminDivision> getAdminDivisionById(Long id) {
        return adminDivisionRepository.findById(id);
    }

    @Override
    public Page<AdminDivision> getAllAdminDivisions(Pageable pageable) {
        return adminDivisionRepository.findAll(pageable);
    }

    @Override
    public List<AdminDivision> getAllAdminDivisions() {
        return adminDivisionRepository.findAll();
    }

    @Override
    public AdminDivision updateAdminDivision(Long id, AdminDivision adminDivision) {
        AdminDivision existing = adminDivisionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("AdminDivision not found: " + id));
        existing.setName(adminDivision.getName());
        existing.setChineseName(adminDivision.getChineseName());
        existing.setAdminLevel(adminDivision.getAdminLevel());
        existing.setParentId(adminDivision.getParentId());
        existing.setParentName(adminDivision.getParentName());
        existing.setGeomWkt(adminDivision.getGeomWkt());
        existing.setAreaSize(adminDivision.getAreaSize());
        existing.setPopulation(adminDivision.getPopulation());
        existing.setAdminCode(adminDivision.getAdminCode());
        existing.setNotes(adminDivision.getNotes());
        return adminDivisionRepository.save(existing);
    }

    @Override
    public void deleteAdminDivision(Long id) {
        if (!adminDivisionRepository.existsById(id))
            throw new RuntimeException("AdminDivision not found: " + id);
        collectorService.deleteCollectorsByAdminDivisionId(id);
        adminDivisionRepository.deleteById(id);
    }

    @Override
    public List<AdminDivision> findByAdminLevel(String adminLevel) {
        return adminDivisionRepository.findByAdminLevel(adminLevel);
    }

    @Override
    public List<AdminDivision> findByParentId(Long parentId) {
        return adminDivisionRepository.findByParentIdOrderByName(parentId);
    }

    @Override
    public List<AdminDivision> findByNameContaining(String name) {
        return adminDivisionRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Map<String, Object>> getTree() {
        List<AdminDivision> all = adminDivisionRepository.findAll();
        Map<Long, List<Map<String, Object>>> childrenMap = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (AdminDivision ad : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", ad.getId());
            node.put("name", ad.getName());
            node.put("chineseName", ad.getChineseName());
            node.put("adminLevel", ad.getAdminLevel());
            node.put("parentId", ad.getParentId());
            node.put("parentName", ad.getParentName());
            node.put("children", new ArrayList<>());
            childrenMap.computeIfAbsent(ad.getId(), k -> new ArrayList<>());
            childrenMap.put(ad.getId(), (List<Map<String, Object>>) node.get("children"));

            if (ad.getParentId() == null) {
                roots.add(node);
            } else {
                childrenMap.computeIfAbsent(ad.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        for (AdminDivision ad : all) {
            List<Map<String, Object>> children = childrenMap.get(ad.getId());
            if (children != null) {
                for (Map<String, Object> root : roots) {
                    attachChildren(root, ad.getId(), children);
                }
            }
        }

        return roots;
    }

    private void attachChildren(Map<String, Object> node, Long targetId, List<Map<String, Object>> children) {
        if (targetId.equals(node.get("id"))) {
            node.put("children", children);
            return;
        }
        List<Map<String, Object>> nodeChildren = (List<Map<String, Object>>) node.get("children");
        if (nodeChildren != null) {
            for (Map<String, Object> child : nodeChildren) {
                attachChildren(child, targetId, children);
            }
        }
    }

    @Override
    public Map<String, Object> getStats() {
        List<AdminDivision> all = adminDivisionRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());

        Map<String, Long> levelCounts = new LinkedHashMap<>();
        long withChildren = 0;
        for (AdminDivision ad : all) {
            String lv = ad.getAdminLevel() != null ? ad.getAdminLevel() : "UNKNOWN";
            levelCounts.merge(lv, 1L, Long::sum);
            if (ad.getParentId() != null) withChildren++;
        }
        stats.put("byLevel", levelCounts);
        stats.put("withParent", withChildren);
        stats.put("roots", all.stream().filter(a -> a.getParentId() == null).count());

        return stats;
    }
}
