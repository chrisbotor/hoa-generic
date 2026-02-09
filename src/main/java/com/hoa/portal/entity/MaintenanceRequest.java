package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests", schema = "hoa")
public class MaintenanceRequest extends PanacheEntity {
    
    public String title;
    public String description;
    public String status;

    @Column(name = "requester_id")
    public UUID requesterId;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}