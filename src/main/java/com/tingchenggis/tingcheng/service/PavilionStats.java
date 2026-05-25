package com.tingchenggis.tingcheng.service;

/**
 * 亭子统计信息类
 * 
 * 用于存储亭子统计数据
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
public class PavilionStats {
    private Long totalPavilions;
    private Long historicalPavilions;
    private Long modernPavilions;
    private Long culturalPavilions;
    private Double averageRating;
    private String mostPopularPavilion;

    public PavilionStats() {}

    // 构造函数、Getter和Setter方法
    public Long getTotalPavilions() { return totalPavilions; }
    public void setTotalPavilions(Long totalPavilions) { this.totalPavilions = totalPavilions; }
    
    public Long getHistoricalPavilions() { return historicalPavilions; }
    public void setHistoricalPavilions(Long historicalPavilions) { this.historicalPavilions = historicalPavilions; }
    
    public Long getModernPavilions() { return modernPavilions; }
    public void setModernPavilions(Long modernPavilions) { this.modernPavilions = modernPavilions; }
    
    public Long getCulturalPavilions() { return culturalPavilions; }
    public void setCulturalPavilions(Long culturalPavilions) { this.culturalPavilions = culturalPavilions; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public String getMostPopularPavilion() { return mostPopularPavilion; }
    public void setMostPopularPavilion(String mostPopularPavilion) { this.mostPopularPavilion = mostPopularPavilion; }
}