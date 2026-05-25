package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scenic_area_collectors")
public class ScenicAreaCollector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenic_area_id", nullable = false)
    private Long scenicAreaId;

    @Column(name = "collector_user_id")
    private String collectorUserId;

    @Column(name = "collector_name")
    private String collectorName;

    @Column(name = "collection_time")
    private java.time.LocalDateTime collectionTime;

    @Column(name = "collection_tool")
    private String collectionTool;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "data_source")
    private String dataSource;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScenicAreaId() { return scenicAreaId; }
    public void setScenicAreaId(Long scenicAreaId) { this.scenicAreaId = scenicAreaId; }

    public String getCollectorUserId() { return collectorUserId; }
    public void setCollectorUserId(String collectorUserId) { this.collectorUserId = collectorUserId; }

    public String getCollectorName() { return collectorName; }
    public void setCollectorName(String collectorName) { this.collectorName = collectorName; }

    public java.time.LocalDateTime getCollectionTime() { return collectionTime; }
    public void setCollectionTime(java.time.LocalDateTime collectionTime) { this.collectionTime = collectionTime; }

    public String getCollectionTool() { return collectionTool; }
    public void setCollectionTool(String collectionTool) { this.collectionTool = collectionTool; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
