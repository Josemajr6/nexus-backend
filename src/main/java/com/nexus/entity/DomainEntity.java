package com.nexus.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass; 
import jakarta.persistence.Version;


@MappedSuperclass 
public class DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Version
    private int version;

    public DomainEntity() {
        super();
    }
    
    public DomainEntity(int id, int version) {
        super();
        this.id = id;
        this.version = version;
    }

    // Getters y Setters 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}