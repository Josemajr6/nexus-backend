package com.nexus.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId()          { return id; }
    public void    setId(Integer i) { this.id = i; }
}