package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase; // Change to Base
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests", schema = "hoa")
public class MaintenanceRequest extends PanacheEntityBase { // Use Base for custom IDs
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This tells Hibernate to use SERIAL/BIGSERIAL
    public Long id;

    public String title;
    public String description;
    public String status;

    @Column(name = "requester_id")
    public UUID requesterId;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}