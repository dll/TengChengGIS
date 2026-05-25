package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_divisions")
public class AdminDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "chinese_name")
    private String chineseName;

    @Column(name = "admin_level", nullable = false)
    private String adminLevel;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "geom_wkt", columnDefinition = "TEXT")
    private String geomWkt;

    @Column(name = "area_size")
    private Double areaSize;

    @Column(name = "population")
    private Long population;

    @Column(name = "admin_code")
    private String adminCode;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChineseName() { return chineseName; }
    public void setChineseName(String chineseName) { this.chineseName = chineseName; }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getGeomWkt() { return geomWkt; }
    public void setGeomWkt(String geomWkt) { this.geomWkt = geomWkt; }

    public Double getAreaSize() { return areaSize; }
    public void setAreaSize(Double areaSize) { this.areaSize = areaSize; }

    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }

    public String getAdminCode() { return adminCode; }
    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
