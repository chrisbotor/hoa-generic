package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "hoa")
public class User extends PanacheEntityBase {
    @Id
    public UUID id;

    public String email;
    
    @Column(name = "password_hash")
    public String passwordHash;
    
    public String role;

    @Column(name = "house_id")
    public Integer houseId;
}