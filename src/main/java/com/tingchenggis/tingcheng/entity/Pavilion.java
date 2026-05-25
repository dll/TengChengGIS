package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 亭子实体类
 * 
 * 代表滁州地区各类亭子的空间和属性信息
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@Entity
@Table(name = "pavilions")
public class Pavilion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "chinese_name")
    private String chineseName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "historical_significance", length = 2000)
    private String historicalSignificance;

    @Column(name = "construction_period")
    private String constructionPeriod;

    @Column(name = "architectural_style")
    private String architecturalStyle;

    // 使用文本存储几何数据，便于在不同数据库之间迁移
    @Column(name = "geom_wkt", columnDefinition = "TEXT")  // WKT格式存储
    private String geomWkt;
    
    // 经纬度字段用于基本定位功能
    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    /** GCJ-02 偏移坐标，供高德/腾讯地图直接使用 */
    @Column(name = "longitude_gcj")
    private Double longitudeGcj;

    @Column(name = "latitude_gcj")
    private Double latitudeGcj;

    @Column(name = "pavilion_type")
    private String pavilionType; // HISTORICAL, MODERN, CULTURAL

    @Column(name = "area_size")
    private Double areaSize; // 平方米

    // 凉亭调查数据字段
    @Column(name = "structure")
    private String structure;

    @Column(name = "top_style")
    private String topStyle;

    @Column(name = "street")
    private String street;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "location_desc")
    private String locationDesc;

    @Column(name = "visitor_rating")
    private Double visitorRating; // 评分 1-5

    @Column(name = "is_open_to_public")
    private Boolean isOpenToPublic;

    @Column(name = "ticket_price")
    private Double ticketPrice;

    @Column(name = "built_year")
    private Integer builtYear;

    @Column(name = "last_renovation_year")
    private Integer lastRenovationYear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public Pavilion() {}

    public Pavilion(String name, String chineseName, String description, String geomWkt, Double longitude, Double latitude, String pavilionType) {
        this.name = name;
        this.chineseName = chineseName;
        this.description = description;
        this.geomWkt = geomWkt;
        this.longitude = longitude;
        this.latitude = latitude;
        this.pavilionType = pavilionType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getGeomWkt() {
        return geomWkt;
    }

    public void setGeomWkt(String geomWkt) {
        this.geomWkt = geomWkt;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitudeGcj() { return longitudeGcj; }
    public void setLongitudeGcj(Double longitudeGcj) { this.longitudeGcj = longitudeGcj; }
    public Double getLatitudeGcj() { return latitudeGcj; }
    public void setLatitudeGcj(Double latitudeGcj) { this.latitudeGcj = latitudeGcj; }

    public String getPavilionType() {
        return pavilionType;
    }

    public void setPavilionType(String pavilionType) {
        this.pavilionType = pavilionType;
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

    public Double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Double areaSize) {
        this.areaSize = areaSize;
    }

    public String getStructure() { return structure; }
    public void setStructure(String structure) { this.structure = structure; }
    public String getTopStyle() { return topStyle; }
    public void setTopStyle(String topStyle) { this.topStyle = topStyle; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }

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
        this.updatedAt = LocalDateTime.now();
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}