package com.nexus.entity;

import jakarta.persistence.Entity;

@Entity
public class Admin extends Actor {
	private Integer nivelAcceso;

	public Admin() {
		super();
	}

	public Admin(Integer nivelAcceso) {
		super();
		this.nivelAcceso = nivelAcceso;
	}

	public Integer getNivelAcceso() {
		return nivelAcceso;
	}

	public void setNivelAcceso(Integer nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}
}
