package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class Comentario extends DomainEntity {

    @NotBlank
    private String texto;

    private LocalDateTime fecha;

    private Boolean esReportado;

    // Relación: Comentario pertenece a 1 Oferta (tiene)
    @ManyToOne
    @JoinColumn(name = "oferta_id")
    private Oferta oferta;

    // Relación: Comentario lo hace 1 Actor (comenta)
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    public Comentario() {
        super();
        this.fecha = LocalDateTime.now();
        this.esReportado = false;
    }
    
    public Comentario(String texto, Oferta oferta, Actor actor) {
        super();
        this.texto = texto;
        this.oferta = oferta;
        this.actor = actor;
        this.fecha = LocalDateTime.now();
        this.esReportado = false;
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Boolean getEsReportado() { return esReportado; }
    public void setEsReportado(Boolean esReportado) { this.esReportado = esReportado; }

    public Oferta getOferta() { return oferta; }
    public void setOferta(Oferta oferta) { this.oferta = oferta; }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }
}