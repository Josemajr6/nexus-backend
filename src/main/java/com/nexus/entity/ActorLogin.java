package com.nexus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@MappedSuperclass
public abstract class ActorLogin extends Actor {

    @NotBlank
    @Column(unique = true)
    protected String user;

    @Email
    @NotBlank
    @Column(unique = true)
    protected String email;

    @NotBlank
    protected String password;

    public ActorLogin() {
        super();
    }

    // EL CONSTRUCTOR QUE FALTABA:
    public ActorLogin(String user, String email, String password) {
        super();
        this.user = user;
        this.email = email;
        this.password = password;
    }

    // Getters y Setters
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}