package com.tingchenggis.tingcheng.dto;

import java.time.LocalDateTime;

/**
 * 亭子数据传输对象
 * 
 * 用于前后端数据传输
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
public class PavilionDto {

    private Long id;
    private String name;
    private String chineseName;
    private String description;
    private String historicalSignificance;
    private String constructionPeriod;
    private String architecturalStyle;
    private String geometry;
    private String pavilionType;
    private Double areaSize;
    private Double visitorRating;
    private Boolean isOpenToPublic;
    private Double ticketPrice;
    private Integer builtYear;
    private Integer lastRenovationYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public PavilionDto() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHistoricalSignificance() {
        return historicalSignificance;
    }

    public void setHistoricalSignificance(String historicalSignificance) {
        this.historicalSignificance = historicalSignificance;
    }

    public String getConstructionPeriod() {
        return constructionPeriod;
    }

    public void setConstructionPeriod(String constructionPeriod) {
        this.constructionPeriod = constructionPeriod;
    }

    public String getArchitecturalStyle() {
        return architecturalStyle;
    }

    public void setArchitecturalStyle(String architecturalStyle) {
        this.architecturalStyle = architecturalStyle;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getPavilionType() {
        return pavilionType;
    }

    public void setPavilionType(String pavilionType) {
        this.pavilionType = pavilionType;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Double areaSize) {
        this.areaSize = areaSize;
    }

    public Double getVisitorRating() {
        return visitorRating;
    }

    public void setVisitorRating(Double visitorRating) {
        this.visitorRating = visitorRating;
    }

    public Boolean getIsOpenToPublic() {
        return isOpenToPublic;
    }

    public void setIsOpenToPublic(Boolean isOpenToPublic) {
        this.isOpenToPublic = isOpenToPublic;
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Integer getBuiltYear() {
        return builtYear;
    }

    public void setBuiltYear(Integer builtYear) {
        this.builtYear = builtYear;
    }

    public Integer getLastRenovationYear() {
        return lastRenovationYear;
    }

    public void setLastRenovationYear(Integer lastRenovationYear) {
        this.lastRenovationYear = lastRenovationYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}