package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "hoa")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String role;

    // Helper method to find a user by email for login/auth
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}