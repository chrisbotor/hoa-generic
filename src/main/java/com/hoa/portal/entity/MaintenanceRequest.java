package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests", schema = "hoa")
public class MaintenanceRequest extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;
    
    public String description;

    public String status;

    @Column(name = "house_id")
    public Long houseId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}