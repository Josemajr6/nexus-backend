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

    @ManyToOne
    @JoinColumn(name = "oferta_id")
    private Oferta oferta;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Actor autor;

    public Comentario() {
        super();
    }
    
    public Comentario(String texto, Oferta oferta, Actor autor) {
        super();
        this.texto = texto;
        this.oferta = oferta;
        this.autor = autor;
    }

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public Boolean getEsReportado() {
		return esReportado;
	}

	public void setEsReportado(Boolean esReportado) {
		this.esReportado = esReportado;
	}

	public Oferta getOferta() {
		return oferta;
	}

	public void setOferta(Oferta oferta) {
		this.oferta = oferta;
	}

	public Actor getAutor() {
		return autor;
	}

	public void setAutor(Actor autor) {
		this.autor = autor;
	}
    
    
}