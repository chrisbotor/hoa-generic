package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "houses", schema = "hoa")
public class House extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "lot_number")
    public String lotNumber;

    @Column(name = "street_address")
    public String streetAddress;

    @Column(name = "owner_id")
    public UUID ownerId;
}