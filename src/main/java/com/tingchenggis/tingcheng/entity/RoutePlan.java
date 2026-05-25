package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 路线方案 — 保存 TSP 规划结果，便于复用、分享、动画回放
 */
@Entity
@Table(name = "route_plans")
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false, length = 200)
    private String planName;

    @Column(name = "transport_mode")
    private String transportMode;

    @Column(name = "objective")
    private String objective;

    /** 访问顺序的亭子ID列表，逗号分隔 */
    @Column(name = "visit_order_ids", columnDefinition = "TEXT")
    private String visitOrderIds;

    @Column(name = "visit_order_names", columnDefinition = "TEXT")
    private String visitOrderNames;

    /** 完整 TSP 计划 JSON，包含 segments/allCoordinates 等 */
    @Column(name = "plan_json", columnDefinition = "TEXT")
    private String planJson;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_duration")
    private Double totalDuration;

    @Column(name = "total_fare")
    private Double totalFare;

    @Column(name = "total_ticket")
    private Double totalTicket;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "pavilion_count")
    private Integer pavilionCount;

    @Column(name = "notes", length = 500)
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
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getVisitOrderIds() { return visitOrderIds; }
    public void setVisitOrderIds(String visitOrderIds) { this.visitOrderIds = visitOrderIds; }
    public String getVisitOrderNames() { return visitOrderNames; }
    public void setVisitOrderNames(String visitOrderNames) { this.visitOrderNames = visitOrderNames; }
    public String getPlanJson() { return planJson; }
    public void setPlanJson(String planJson) { this.planJson = planJson; }
    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }
    public Double getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Double totalDuration) { this.totalDuration = totalDuration; }
    public Double getTotalFare() { return totalFare; }
    public void setTotalFare(Double totalFare) { this.totalFare = totalFare; }
    public Double getTotalTicket() { return totalTicket; }
    public void setTotalTicket(Double totalTicket) { this.totalTicket = totalTicket; }
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    public Integer getPavilionCount() { return pavilionCount; }
    public void setPavilionCount(Integer pavilionCount) { this.pavilionCount = pavilionCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
