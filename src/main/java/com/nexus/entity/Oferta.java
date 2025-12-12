package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Oferta extends DomainEntity {

    @NotBlank
    private String urlOferta;

    @NotBlank
    private String tienda;

    @Min(0)
    private Double precioOferta;

    @Min(0)
    private Double precioOriginal; 

    private LocalDateTime fechaExpiracion; 

    @ManyToOne
    @JoinColumn(name = "actor_id") 
    private Actor actor;

    
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios;

    public Oferta() {
        super();
    }
    
    public Oferta(String urlOferta, String tienda, Double precioOferta, Double precioOriginal, LocalDateTime fechaExpiracion, Actor actor) {
        super();
        this.urlOferta = urlOferta;
        this.tienda = tienda;
        this.precioOferta = precioOferta;
        this.precioOriginal = precioOriginal;
        this.fechaExpiracion = fechaExpiracion;
        this.actor = actor;
    }

    public Actor getActor() { return actor; }
    public void setActor(Actor actor) { this.actor = actor; }

	public String getUrlOferta() {
		return urlOferta;
	}

	public void setUrlOferta(String urlOferta) {
		this.urlOferta = urlOferta;
	}

	public String getTienda() {
		return tienda;
	}

	public void setTienda(String tienda) {
		this.tienda = tienda;
	}

	public Double getPrecioOferta() {
		return precioOferta;
	}

	public void setPrecioOferta(Double precioOferta) {
		this.precioOferta = precioOferta;
	}

	public Double getPrecioOriginal() {
		return precioOriginal;
	}

	public void setPrecioOriginal(Double precioOriginal) {
		this.precioOriginal = precioOriginal;
	}

	public LocalDateTime getFechaExpiracion() {
		return fechaExpiracion;
	}

	public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
		this.fechaExpiracion = fechaExpiracion;
	}

	public List<Comentario> getComentarios() {
		return comentarios;
	}

	public void setComentarios(List<Comentario> comentarios) {
		this.comentarios = comentarios;
	}
    
    
}
