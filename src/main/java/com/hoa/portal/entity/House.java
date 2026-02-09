package com.hoa.portal.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "houses", schema = "hoa")
public class House extends PanacheEntityBase {
    @Id
    public Integer id;
    public String address; // Assuming you have an address column
}