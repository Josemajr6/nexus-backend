package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "spark_voto",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "oferta_id"})
)
public class SparkVoto extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @Column(nullable = false)
    private Boolean esSpark; // true = ⚡ Spark, false = 💧 Drip

    private LocalDateTime fechaVoto;

    public SparkVoto() {
        super();
        this.fechaVoto = LocalDateTime.now();
    }

    public SparkVoto(Usuario usuario, Oferta oferta, Boolean esSpark) {
        this();
        this.usuario = usuario;
        this.oferta = oferta;
        this.esSpark = esSpark;
    }

    // Getters y Setters
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Oferta getOferta() { return oferta; }
    public void setOferta(Oferta oferta) { this.oferta = oferta; }

    public Boolean getEsSpark() { return esSpark; }
    public void setEsSpark(Boolean esSpark) { this.esSpark = esSpark; }

    public LocalDateTime getFechaVoto() { return fechaVoto; }
    public void setFechaVoto(LocalDateTime fechaVoto) { this.fechaVoto = fechaVoto; }
}