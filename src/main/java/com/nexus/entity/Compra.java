package com.nexus.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad de compra actualizada con soporte para:
 *  - Sistema escrow (fondos retenidos hasta confirmación)
 *  - Venta en persona o por envío
 *  - Referencia al Stripe PaymentIntent para operaciones de reembolso
 */
@Entity
public class Compra extends DomainEntity {

    @NotNull
    private LocalDateTime fechaCompra;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoCompra estado;

    @Enumerated(EnumType.STRING)
    private MetodoEntrega metodoEntrega;

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // PaymentIntent de Stripe: necesario para capturar o reembolsar
    private String stripePaymentIntentId;

    // Precio final pactado (puede diferir del precio original si hubo propuesta de precio)
    private Double precioFinal;

    // Fechas de transición de estado
    private LocalDateTime fechaPago;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaCompletada;

    public Compra() {
        super();
        this.fechaCompra = LocalDateTime.now();
        this.estado = EstadoCompra.PENDIENTE;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }

    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }

    public MetodoEntrega getMetodoEntrega() { return metodoEntrega; }
    public void setMetodoEntrega(MetodoEntrega metodoEntrega) { this.metodoEntrega = metodoEntrega; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String id) { this.stripePaymentIntentId = id; }

    public Double getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(Double precioFinal) { this.precioFinal = precioFinal; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public LocalDateTime getFechaCompletada() { return fechaCompletada; }
    public void setFechaCompletada(LocalDateTime fechaCompletada) { this.fechaCompletada = fechaCompletada; }
}