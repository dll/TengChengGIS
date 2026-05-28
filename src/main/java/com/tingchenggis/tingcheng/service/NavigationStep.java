package com.tingchenggis.tingcheng.service;

public class NavigationStep {
    private int stepNumber;
    private String instruction;
    private String maneuverType;
    private String maneuverModifier;
    private String streetName;
    private double distanceKm;
    private double durationSeconds;
    private double latitude;
    private double longitude;

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public String getManeuverType() { return maneuverType; }
    public void setManeuverType(String maneuverType) { this.maneuverType = maneuverType; }
    public String getManeuverModifier() { return maneuverModifier; }
    public void setManeuverModifier(String maneuverModifier) { this.maneuverModifier = maneuverModifier; }
    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(double durationSeconds) { this.durationSeconds = durationSeconds; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
