package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

/**
 * 交通线实体类
 *
 * 用于表示亭子之间的实际交通连接路径
 * 支持带中间节点的真实道路路线
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Entity
@Table(name = "transport_routes")
public class TransportRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "route_type", nullable = false)
    private String routeType;

    @Column(name = "from_pavilion_id", nullable = false)
    private Long fromPavilionId;

    @Column(name = "to_pavilion_id", nullable = false)
    private Long toPavilionId;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "travel_time_minutes")
    private Integer travelTimeMinutes;

    @Column(name = "route_description")
    private String routeDescription;

    @Column(name = "is_accessible")
    private Boolean isAccessible;

    @Column(name = "is_scenic_route")
    private Boolean isScenicRoute;

    @Column(name = "road_type")
    private String roadType;

    @Column(name = "waypoints", columnDefinition = "TEXT")
    private String waypoints;

    @Column(name = "elevation_gain")
    private Double elevationGain;

    @Column(name = "transport_mode", nullable = false)
    private String transportMode;

    @Column(name = "road_level")
    private String roadLevel;

    @Column(name = "traffic_condition")
    private String trafficCondition;

    @Column(name = "estimated_fare")
    private Double estimatedFare;

    @Column(name = "geom_wkt", columnDefinition = "TEXT")
    private String geomWkt;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public Long getFromPavilionId() {
        return fromPavilionId;
    }

    public void setFromPavilionId(Long fromPavilionId) {
        this.fromPavilionId = fromPavilionId;
    }

    public Long getToPavilionId() {
        return toPavilionId;
    }

    public void setToPavilionId(Long toPavilionId) {
        this.toPavilionId = toPavilionId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(Integer travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean isAccessible) {
        this.isAccessible = isAccessible;
    }

    public Boolean getIsScenicRoute() {
        return isScenicRoute;
    }

    public void setIsScenicRoute(Boolean isScenicRoute) {
        this.isScenicRoute = isScenicRoute;
    }

    public String getRoadType() {
        return roadType;
    }

    public void setRoadType(String roadType) {
        this.roadType = roadType;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public Double getElevationGain() {
        return elevationGain;
    }

    public void setElevationGain(Double elevationGain) {
        this.elevationGain = elevationGain;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getRoadLevel() {
        return roadLevel;
    }

    public void setRoadLevel(String roadLevel) {
        this.roadLevel = roadLevel;
    }

    public String getTrafficCondition() {
        return trafficCondition;
    }

    public void setTrafficCondition(String trafficCondition) {
        this.trafficCondition = trafficCondition;
    }

    public Double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(Double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public String getGeomWkt() {
        return geomWkt;
    }

    public void setGeomWkt(String geomWkt) {
        this.geomWkt = geomWkt;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
