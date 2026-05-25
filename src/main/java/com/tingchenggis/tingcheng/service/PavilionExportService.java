package com.tingchenggis.tingcheng.service;

public interface PavilionExportService {
    byte[] exportGeoJson();
    byte[] exportExcel();
    byte[] exportCsv();
    byte[] exportExcelTemplate();
    byte[] exportCsvTemplate();
}
