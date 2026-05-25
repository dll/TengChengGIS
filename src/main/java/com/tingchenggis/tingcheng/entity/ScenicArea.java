package com.tingchenggis.tingcheng.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scenic_areas")
public class ScenicArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "chinese_name")
    private String chineseName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "area_type")
    private String areaType;

    @Column(name = "area_size")
    private Double areaSize;

    @Column(name = "geom_wkt", columnDefinition = "TEXT")
    private String geomWkt;

    @Column(name = "boundary_wkt", columnDefinition = "TEXT")
    private String boundaryWkt;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "address")
    private String address;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "ticket_price")
    private Double ticketPrice;

    @Column(name = "visitor_rating")
    private Double visitorRating;

    @Column(name = "is_open_to_public")
    private Boolean isOpenToPublic;

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public Double getAreaSize() { return areaSize; }
    public void setAreaSize(Double areaSize) { this.areaSize = areaSize; }

    public String getGeomWkt() { return geomWkt; }
    public void setGeomWkt(String geomWkt) { this.geomWkt = geomWkt; }

    public String getBoundaryWkt() { return boundaryWkt; }
    public void setBoundaryWkt(String boundaryWkt) { this.boundaryWkt = boundaryWkt; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public Double getVisitorRating() { return visitorRating; }
    public void setVisitorRating(Double visitorRating) { this.visitorRating = visitorRating; }

    public Boolean getIsOpenToPublic() { return isOpenToPublic; }
    public void setIsOpenToPublic(Boolean isOpenToPublic) { this.isOpenToPublic = isOpenToPublic; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
