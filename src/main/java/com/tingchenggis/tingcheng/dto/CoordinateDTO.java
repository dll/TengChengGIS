package com.tingchenggis.tingcheng.dto;

/**
 * 坐标传输对象
 * 
 * 用于表示亭子的经纬度坐标
 * 
 * @author TingChengGIS
 */
public class CoordinateDTO {
    private Double longitude;
    private Double latitude;

    public CoordinateDTO() {}

    public CoordinateDTO(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters and setters
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

    @Override
    public String toString() {
        return "CoordinateDTO{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}