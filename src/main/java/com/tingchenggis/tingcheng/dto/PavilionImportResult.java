package com.tingchenggis.tingcheng.dto;

import java.util.ArrayList;
import java.util.List;

public class PavilionImportResult {

    private int totalRows;
    private int successCount;
    private int skipCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();
    private String format;

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public void addError(String msg) {
        errors.add(msg);
        errorCount++;
    }
}
