package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests", schema = "hoa")
public class MaintenanceRequest extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "requester_id")
    public UUID requesterId;

    @Column(name = "house_id")
    public Integer houseId;

    public String title;
    public String description;
    public String status; // pending, in_progress, completed

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    // Helper to find requests for a specific resident
    public static List<MaintenanceRequest> findByResident(UUID residentId) {
        return list("requesterId", residentId);
    }
}