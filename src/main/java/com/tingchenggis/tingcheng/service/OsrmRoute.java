package com.tingchenggis.tingcheng.service;

import java.util.List;

public class OsrmRoute {
    private double distance;
    private double duration;
    private List<double[]> coordinates;
    private String geometryWkt;
    private List<NavigationStep> steps;

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }
    public List<double[]> getCoordinates() { return coordinates; }
    public void setCoordinates(List<double[]> coordinates) { this.coordinates = coordinates; }
    public String getGeometryWkt() { return geometryWkt; }
    public void setGeometryWkt(String geometryWkt) { this.geometryWkt = geometryWkt; }
    public List<NavigationStep> getSteps() { return steps; }
    public void setSteps(List<NavigationStep> steps) { this.steps = steps; }
}
