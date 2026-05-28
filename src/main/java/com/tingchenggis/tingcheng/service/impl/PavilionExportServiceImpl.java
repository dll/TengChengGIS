package com.tingchenggis.tingcheng.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.exception.BusinessException;
import com.tingchenggis.tingcheng.service.PavilionExportService;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class PavilionExportServiceImpl implements PavilionExportService {

    private static final Logger logger = LoggerFactory.getLogger(PavilionExportServiceImpl.class);
    private static final String[] HEADERS = {"序号","名称","结构","x","y","位置","所在街道/小区","大致面积(m²)","风格(平立面)","备注"};

    private final PavilionService pavilionService;
    private final ObjectMapper objectMapper;

    public PavilionExportServiceImpl(PavilionService pavilionService, ObjectMapper objectMapper) {
        this.pavilionService = pavilionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] exportGeoJson() {
        try {
            List<Pavilion> pavs = pavilionService.getAllPavilions();
            ObjectNode fc = objectMapper.createObjectNode();
            fc.put("type", "FeatureCollection");
            ArrayNode features = fc.putArray("features");
            int seq = 0;
            for (Pavilion p : pavs) {
                ObjectNode feat = objectMapper.createObjectNode();
                feat.put("type", "Feature");
                feat.put("id", p.getId());
                ObjectNode geom = feat.putObject("geometry");
                geom.put("type", "Point");
                ArrayNode coords = geom.putArray("coordinates");
                coords.add(p.getLongitude() != null ? p.getLongitude() : 0);
                coords.add(p.getLatitude() != null ? p.getLatitude() : 0);
                ObjectNode props = feat.putObject("properties");
                seq++;
                props.put("seq", seq);
                put(props, "name", p.getName());
                put(props, "chineseName", p.getChineseName());
                put(props, "description", p.getDescription());
                put(props, "type", p.getPavilionType());
                put(props, "structure", p.getStructure());
                put(props, "topStyle", p.getTopStyle());
                put(props, "street", p.getStreet());
                put(props, "locationDesc", p.getLocationDesc());
                put(props, "notes", p.getNotes());
                if (p.getAreaSize() != null) props.put("areaSize", p.getAreaSize());
                if (p.getBuiltYear() != null) props.put("builtYear", p.getBuiltYear());
                if (p.getVisitorRating() != null) props.put("visitorRating", p.getVisitorRating());
                if (p.getIsOpenToPublic() != null) props.put("isOpenToPublic", p.getIsOpenToPublic());
                if (p.getTicketPrice() != null) props.put("ticketPrice", p.getTicketPrice());
                features.add(feat);
            }
            logger.info("GeoJSON export: {} features", features.size());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(fc);
        } catch (Exception e) {
            throw new BusinessException("GeoJSON export failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportExcel() {
        try {
            List<Pavilion> pavs = pavilionService.getAllPavilions();
            try (var wb = new XSSFWorkbook(); var bos = new ByteArrayOutputStream()) {
                Sheet sheet = wb.createSheet("亭子数据");
                Row hdr = sheet.createRow(0);
                for (int i = 0; i < HEADERS.length; i++) hdr.createCell(i).setCellValue(HEADERS[i]);
                int rowIdx = 1;
                for (Pavilion p : pavs) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rowIdx - 1);
                    row.createCell(1).setCellValue(p.getName() != null ? p.getName() : "");
                    row.createCell(2).setCellValue(p.getStructure() != null ? p.getStructure() : "");
                    if (p.getLongitude() != null) row.createCell(3).setCellValue(p.getLongitude());
                    if (p.getLatitude() != null) row.createCell(4).setCellValue(p.getLatitude());
                    row.createCell(5).setCellValue(p.getLocationDesc() != null ? p.getLocationDesc() : "");
                    row.createCell(6).setCellValue(p.getStreet() != null ? p.getStreet() : "");
                    if (p.getAreaSize() != null) row.createCell(7).setCellValue(p.getAreaSize());
                    row.createCell(8).setCellValue(p.getTopStyle() != null ? p.getTopStyle() : "");
                    row.createCell(9).setCellValue(p.getNotes() != null ? p.getNotes() : "");
                }
                wb.write(bos);
                return bos.toByteArray();
            }
        } catch (Exception e) {
throw new BusinessException("Excel export failed: " + e.getMessage(), e);

        }
    }

    @Override
    public byte[] exportCsv() {
        try {
            List<Pavilion> pavs = pavilionService.getAllPavilions();
            var bos = new ByteArrayOutputStream();
            var w = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
            w.write('﻿'); // UTF-8 BOM
            w.write(String.join(",", HEADERS) + "\n");
            int seq = 1;
            for (Pavilion p : pavs) {
                w.write(seq++ + "," + csvSafe(p.getName()) + "," + csvSafe(p.getStructure()) + "," +
                    p.getLongitude() + "," + p.getLatitude() + "," + csvSafe(p.getLocationDesc()) + "," +
                    csvSafe(p.getStreet()) + "," + p.getAreaSize() + "," + csvSafe(p.getTopStyle()) + "," +
                    csvSafe(p.getNotes()) + "\n");
            }
            w.flush();
            return bos.toByteArray();
        } catch (Exception e) {
throw new BusinessException("CSV export failed: " + e.getMessage(), e);

        }
    }

    @Override
    public byte[] exportExcelTemplate() {
        try (var wb = new XSSFWorkbook(); var bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("亭子导入模板");
            Row hdr = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) hdr.createCell(i).setCellValue(HEADERS[i]);
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
throw new BusinessException("Template export failed: " + e.getMessage(), e);

        }
    }

    @Override
    public byte[] exportCsvTemplate() {
        try {
            var bos = new ByteArrayOutputStream();
            var w = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
            w.write('﻿');
            w.write(String.join(",", HEADERS) + "\n");
            w.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("CSV template export failed: " + e.getMessage(), e);
        }
    }

    private void put(ObjectNode node, String key, String value) {
        if (value != null && !value.isEmpty()) node.put(key, value);
    }

    private String csvSafe(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
