package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "hoa") // Directs to your 'hoa' schema
public class User extends PanacheEntityBase {

    @Id
    public UUID id; // Matches the gen_random_uuid() from your SQL

    @Column(name = "full_name")
    public String fullName;

    public String email;

    /**
     * This @Column annotation is the "bridge." 
     * It tells Java that the variable 'passwordHash' 
     * maps to the 'password_hash' column in your DB.
     */
    @Column(name = "password_hash")
    public String passwordHash;

    public String role;

    @Column(name = "house_id")
    public Long houseId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;
}