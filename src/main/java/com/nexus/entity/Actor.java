package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // ✅ CORREGIDO: Evita problemas de FK
public abstract class Actor extends DomainEntity {

    @NotBlank
    @Column(unique = true)
    protected String user;

    @Email
    @NotBlank
    @Column(unique = true)
    protected String email;

    @NotBlank
    protected String password;

    protected LocalDateTime fechaRegistro;
    
    // Campo interno para lógica de registro (no sale en el UML pero es necesario)
    @JsonIgnore
    protected String codigoVerificacion;

    // RELACIÓN: 1 Actor publica 0..* Ofertas
    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL)
    @JsonIgnore
    protected List<Oferta> ofertasPublicadas = new ArrayList<>();

    // RELACIÓN: 1 Actor comenta 0..* Comentarios
    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL)
    @JsonIgnore
    protected List<Comentario> comentariosRealizados = new ArrayList<>();

    public Actor() {
        super();
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getCodigoVerificacion() { return codigoVerificacion; }
    public void setCodigoVerificacion(String codigoVerificacion) { this.codigoVerificacion = codigoVerificacion; }

    public List<Oferta> getOfertasPublicadas() { return ofertasPublicadas; }
    public void setOfertasPublicadas(List<Oferta> ofertasPublicadas) { this.ofertasPublicadas = ofertasPublicadas; }

    public List<Comentario> getComentariosRealizados() { return comentariosRealizados; }
    public void setComentariosRealizados(List<Comentario> comentariosRealizados) { this.comentariosRealizados = comentariosRealizados; }
}