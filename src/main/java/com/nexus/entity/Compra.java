package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Compra extends DomainEntity {

    @NotNull
    private LocalDateTime fechaCompra;

    // --- EL CAMPO QUE FALTABA ---
    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoCompra estado; 

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    public Compra() {
        super();
        this.fechaCompra = LocalDateTime.now();
        this.estado = EstadoCompra.PENDIENTE; // Valor por defecto
    }

    // Getters y Setters
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }

    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; } // <--- ¡AQUÍ ESTÁ LA SOLUCIÓN!

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}