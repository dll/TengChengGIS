package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.AdminDivisionCollector;

import java.util.List;
import java.util.Map;

public interface AdminDivisionCollectorService {

    AdminDivisionCollector createCollector(AdminDivisionCollector collector);

    List<AdminDivisionCollector> getCollectorsByAdminDivisionId(Long adminDivisionId);

    void deleteCollectorsByAdminDivisionId(Long adminDivisionId);

    Map<Long, Long> getCollectorCountByAdminDivisionIds();
}
