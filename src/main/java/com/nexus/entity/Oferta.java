package com.nexus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class Oferta extends DomainEntity {

    @NotBlank
    private String titulo;  

    @NotBlank
    private String descripcion;

    private String tienda;
    private double precioOriginal;
    private double precioOferta;
    private String urlOferta; // Esto guardará la URL de la FOTO
    
    private LocalDateTime fechaExpiracion;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    public Oferta() {
        super();
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }

    public double getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(double precioOriginal) { this.precioOriginal = precioOriginal; }

    public double getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(double precioOferta) { this.precioOferta = precioOferta; }

    public String getUrlOferta() { return urlOferta; }
    public void setUrlOferta(String urlOferta) { this.urlOferta = urlOferta; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }
}