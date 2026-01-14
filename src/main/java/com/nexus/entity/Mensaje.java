package com.nexus.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Mensaje extends DomainEntity{

	@NotNull
	private LocalDateTime fechaCreacion;
	
	@NotNull
	private Boolean estaActivo;
	
	@ManyToOne
	private Usuario usuario;

	@ManyToOne
	private Producto producto;
	
	public Mensaje() {
		super();
		this.estaActivo = true;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public boolean isEstaActivo() {
		return estaActivo;
	}

	public void setEstaActivo(boolean estaActivo) {
		this.estaActivo = estaActivo;
	}
	
	
	
}
