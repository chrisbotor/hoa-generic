package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "hoa")
public class User extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "full_name")
    public String fullName;

    public String email;

    @Column(name = "password_hash")
    public String passwordHash; // This matches your DB column

    public String role;

    @Column(name = "house_id")
    public Long houseId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}