package com.nexus.entity;

import jakarta.persistence.Entity;

@Entity
public class Admin extends Actor { // <--- CAMBIO: Extends Actor

    private int nivelAcceso;

    public Admin() {
        super();
    }

    public Admin(String user, String email, String password, int nivelAcceso) {
        super();
        this.username = user;
        this.email = email;
        this.password = password;
        this.nivelAcceso = nivelAcceso;
    }

    public int getNivelAcceso() { return nivelAcceso; }
    public void setNivelAcceso(int nivelAcceso) { this.nivelAcceso = nivelAcceso; }
}