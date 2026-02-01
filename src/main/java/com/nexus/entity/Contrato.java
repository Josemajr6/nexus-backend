package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Contrato extends DomainEntity {
    
    @Enumerated(EnumType.STRING)
    private TipoContrato tipoContrato;
    
    private LocalDateTime fecha; // <-- Añadido según UML
    
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    public Contrato() {
        super();
        this.fecha = LocalDateTime.now();
    }

    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
}