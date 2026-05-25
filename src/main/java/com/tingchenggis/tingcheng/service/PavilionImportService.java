package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.dto.PavilionImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface PavilionImportService {
    PavilionImportResult importFromExcel(MultipartFile file);
    PavilionImportResult importFromGeoJson(MultipartFile file);
    PavilionImportResult importFromCsv(MultipartFile file);
    PavilionImportResult importAuto(MultipartFile file);
}
