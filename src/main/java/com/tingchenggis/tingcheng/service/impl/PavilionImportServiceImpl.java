package com.tingchenggis.tingcheng.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.dto.PavilionImportResult;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.entity.PavilionCollector;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionImportService;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.util.PavilionTypeUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class PavilionImportServiceImpl implements PavilionImportService {

    private static final Logger logger = LoggerFactory.getLogger(PavilionImportServiceImpl.class);
    private final PavilionService pavilionService;
    private final ObjectMapper objectMapper;
    private final PavilionCollectorService collectorService;

    public PavilionImportServiceImpl(PavilionService pavilionService, ObjectMapper objectMapper,
                                      PavilionCollectorService collectorService) {
        this.pavilionService = pavilionService;
        this.objectMapper = objectMapper;
        this.collectorService = collectorService;
    }

    private void addDefaultCollector(Pavilion pavilion) {
        PavilionCollector collector = new PavilionCollector();
        collector.setPavilionId(pavilion.getId());
        collector.setCollectorName("系统导入");
        collector.setCollectionTool("Excel批量导入");
        collector.setDataSource("Excel批量导入");
        collector.setCollectionTime(java.time.LocalDateTime.now());
        collector.setNotes("通过文件批量导入");
        collectorService.createCollector(collector);
    }

    @Override
    public PavilionImportResult importAuto(MultipartFile file) {
        String fn = file.getOriginalFilename();
        if (fn == null) throw new RuntimeException("No filename");
        String low = fn.toLowerCase();
        if (low.endsWith(".xlsx") || low.endsWith(".xls")) return importFromExcel(file);
        if (low.endsWith(".geojson") || low.endsWith(".json")) return importFromGeoJson(file);
        if (low.endsWith(".csv")) return importFromCsv(file);
        throw new RuntimeException("Unsupported format: " + fn);
    }

    @Override
    public PavilionImportResult importFromExcel(MultipartFile file) {
        PavilionImportResult r = new PavilionImportResult();
        r.setFormat("excel");
        try (var is = file.getInputStream(); var wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int total = sheet.getLastRowNum() - 1;
            r.setTotalRows(total);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) { r.setSkipCount(r.getSkipCount() + 1); continue; }
                try {
                    Pavilion p = mapExcelRow(row);
                    Pavilion saved = pavilionService.createPavilion(p);
                    addDefaultCollector(saved);
                    r.setSuccessCount(r.getSuccessCount() + 1);
                } catch (Exception e) {
                    r.addError("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
            logger.info("Excel import: {} success, {} errors", r.getSuccessCount(), r.getErrorCount());
        } catch (Exception e) {
            throw new RuntimeException("Excel import failed: " + e.getMessage(), e);
        }
        return r;
    }

    private Pavilion mapExcelRow(Row row) {
        String name = str(row, 1);
        String structure = str(row, 2);
        Double lng = dbl(row, 3);
        Double lat = dbl(row, 4);
        String locDesc = str(row, 5);
        String street = str(row, 6);
        Double area = dbl(row, 7);
        String topStyle = str(row, 8);
        String notes = str(row, 9);

        Pavilion p = new Pavilion();
        p.setName(name != null ? name : "未命名");
        p.setChineseName(p.getName());
        p.setStructure(structure);
        p.setLongitude(lng);
        p.setLatitude(lat);
        p.setLocationDesc(locDesc);
        p.setStreet(street);
        p.setAreaSize(area);
        p.setTopStyle(topStyle);
        p.setNotes(notes);

        String desc = (locDesc != null ? locDesc : "") +
            (notes != null && !notes.isEmpty() ? " (" + notes + ")" : "");
        p.setDescription(desc.length() > 2000 ? desc.substring(0, 1997) + "..." : desc);

        p.setPavilionType(PavilionTypeUtils.inferType(name, structure, locDesc));

        if (lng != null && lat != null) {
            p.setGeomWkt(String.format("POINT(%s %s)", lng, lat));
        }

        p.setIsOpenToPublic(true);
        p.setTicketPrice(0.0);

        return p;
    }

    @Override
    public PavilionImportResult importFromGeoJson(MultipartFile file) {
        PavilionImportResult r = new PavilionImportResult();
        r.setFormat("geojson");
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());
            JsonNode features = root.get("features");
            if (features == null || !features.isArray())
                throw new RuntimeException("Invalid GeoJSON: missing features array");
            r.setTotalRows(features.size());
            for (JsonNode feat : features) {
                try {
                    Pavilion p = new Pavilion();
                    JsonNode geom = feat.get("geometry");
                    JsonNode props = feat.get("properties");
                    if (geom != null && props != null) {
                        JsonNode coords = geom.get("coordinates");
                        if (coords != null && coords.isArray() && coords.size() >= 2) {
                            p.setLongitude(coords.get(0).asDouble());
                            p.setLatitude(coords.get(1).asDouble());
                            p.setGeomWkt(String.format("POINT(%s %s)",
                                coords.get(0).asDouble(), coords.get(1).asDouble()));
                        }
                        p.setName(props.has("name") ? props.get("name").asText() : "未命名");
                        p.setChineseName(props.has("chineseName") ? props.get("chineseName").asText() : p.getName());
                        p.setDescription(props.has("description") ? props.get("description").asText() : null);
                        p.setPavilionType(props.has("type") ? props.get("type").asText()
                            : PavilionTypeUtils.inferType(p.getName(), null, null));
                        if (props.has("structure")) p.setStructure(props.get("structure").asText());
                        if (props.has("topStyle")) p.setTopStyle(props.get("topStyle").asText());
                        if (props.has("street")) p.setStreet(props.get("street").asText());
                        if (props.has("notes")) p.setNotes(props.get("notes").asText());
                        if (props.has("locationDesc")) p.setLocationDesc(props.get("locationDesc").asText());
                        if (props.has("areaSize")) p.setAreaSize(props.get("areaSize").asDouble());
                        if (props.has("visitorRating")) p.setVisitorRating(props.get("visitorRating").asDouble());
                        if (props.has("builtYear")) p.setBuiltYear(props.get("builtYear").asInt());
                    }
                    p.setIsOpenToPublic(true);
                    p.setTicketPrice(0.0);
                    Pavilion saved = pavilionService.createPavilion(p);
                    addDefaultCollector(saved);
                    r.setSuccessCount(r.getSuccessCount() + 1);
                } catch (Exception e) {
                    r.addError("Feature: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("GeoJSON import failed: " + e.getMessage(), e);
        }
        return r;
    }

    @Override
    public PavilionImportResult importFromCsv(MultipartFile file) {
        PavilionImportResult r = new PavilionImportResult();
        r.setFormat("csv");
        try (var br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            int rowNum = 2;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                try {
                    String[] cols = line.split(",", -1);
                    Pavilion p = new Pavilion();
                    p.setName(cols.length > 1 ? cols[1].trim() : "未命名");
                    p.setChineseName(p.getName());
                    p.setStructure(cols.length > 2 ? cols[2].trim() : null);
                    if (cols.length > 3 && !cols[3].isBlank()) p.setLongitude(Double.parseDouble(cols[3].trim()));
                    if (cols.length > 4 && !cols[4].isBlank()) p.setLatitude(Double.parseDouble(cols[4].trim()));
                    p.setLocationDesc(cols.length > 5 ? cols[5].trim() : null);
                    p.setStreet(cols.length > 6 ? cols[6].trim() : null);
                    if (cols.length > 7 && !cols[7].isBlank()) p.setAreaSize(Double.parseDouble(cols[7].trim()));
                    p.setTopStyle(cols.length > 8 ? cols[8].trim() : null);
                    p.setNotes(cols.length > 9 ? cols[9].trim() : null);
                    p.setPavilionType(PavilionTypeUtils.inferType(p.getName(), p.getStructure(), p.getLocationDesc()));
                    if (p.getLongitude() != null && p.getLatitude() != null)
                        p.setGeomWkt(String.format("POINT(%s %s)", p.getLongitude(), p.getLatitude()));
                    p.setIsOpenToPublic(true);
                    p.setTicketPrice(0.0);
                    p.setDescription((p.getLocationDesc() != null ? p.getLocationDesc() : "") +
                        (p.getNotes() != null ? " (" + p.getNotes() + ")" : ""));
                    Pavilion saved = pavilionService.createPavilion(p);
                    addDefaultCollector(saved);
                    r.setSuccessCount(r.getSuccessCount() + 1);
                } catch (Exception e) {
                    r.addError("Row " + rowNum + ": " + e.getMessage());
                }
            }
            r.setTotalRows(r.getSuccessCount() + r.getErrorCount());
        } catch (Exception e) {
            throw new RuntimeException("CSV import failed: " + e.getMessage(), e);
        }
        return r;
    }

    private String str(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString().trim();
        };
    }

    private Double dbl(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue().trim()); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
