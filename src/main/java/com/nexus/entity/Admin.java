package com.nexus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "actor_id")
public class Admin extends Actor {

    /** 1 = moderador, 2 = admin completo, 3 = superadmin */
    @Column(name = "nivel_acceso", nullable = false)
    private Integer nivelAcceso = 1;

    public Admin() {}

    public Integer getNivelAcceso()          { return nivelAcceso; }
    public void    setNivelAcceso(Integer n) { this.nivelAcceso = n; }
}