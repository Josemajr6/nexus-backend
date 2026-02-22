package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Solicitud de devolución de un producto recibido.
 *
 * Flujo:
 *  1. Comprador solicita devolución → estado SOLICITADA
 *  2. Vendedor acepta/rechaza en 48h → ACEPTADA o RECHAZADA
 *  3. Comprador envía el producto de vuelta → DEVOLUCION_ENVIADA
 *  4. Vendedor confirma recepción → COMPLETADA → reembolso automático
 *
 * Motivos válidos: PRODUCTO_NO_CORRESPONDE, DEFECTUOSO, DAÑADO_EN_ENVIO, ARREPENTIMIENTO
 */
@Entity
@Table(name = "devolucion")
public class Devolucion extends DomainEntity {

    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false, unique = true)
    private Compra compra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDevolucion estado = EstadoDevolucion.SOLICITADA;

    @Enumerated(EnumType.STRING)
    private MotivoDevolucion motivo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Fotos del problema (URLs de Cloudinary)
    @ElementCollection
    @CollectionTable(name = "devolucion_fotos", joinColumns = @JoinColumn(name = "devolucion_id"))
    @Column(name = "foto_url")
    private java.util.List<String> fotos = new java.util.ArrayList<>();

    // Datos del envío de vuelta
    private String transportistaDevolucion;
    private String trackingDevolucion;

    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;

    private String notaVendedor;     // Respuesta del vendedor al aceptar/rechazar
    private Boolean reembolsoTotal = true; // false = reembolso parcial

    public Devolucion() { super(); this.fechaSolicitud = LocalDateTime.now(); }

    public Compra           getCompra()                      { return compra; }
    public void             setCompra(Compra c)              { this.compra = c; }
    public EstadoDevolucion getEstado()                      { return estado; }
    public void             setEstado(EstadoDevolucion e)    { this.estado = e; }
    public MotivoDevolucion getMotivo()                      { return motivo; }
    public void             setMotivo(MotivoDevolucion m)    { this.motivo = m; }
    public String           getDescripcion()                 { return descripcion; }
    public void             setDescripcion(String d)         { this.descripcion = d; }
    public java.util.List<String> getFotos()                 { return fotos; }
    public void             setFotos(java.util.List<String> f) { this.fotos = f; }
    public String           getTransportistaDevolucion()     { return transportistaDevolucion; }
    public void             setTransportistaDevolucion(String t) { this.transportistaDevolucion = t; }
    public String           getTrackingDevolucion()          { return trackingDevolucion; }
    public void             setTrackingDevolucion(String t)  { this.trackingDevolucion = t; }
    public LocalDateTime    getFechaSolicitud()               { return fechaSolicitud; }
    public LocalDateTime    getFechaResolucion()              { return fechaResolucion; }
    public void             setFechaResolucion(LocalDateTime f) { this.fechaResolucion = f; }
    public String           getNotaVendedor()                { return notaVendedor; }
    public void             setNotaVendedor(String n)        { this.notaVendedor = n; }
    public Boolean          getReembolsoTotal()              { return reembolsoTotal; }
    public void             setReembolsoTotal(Boolean r)     { this.reembolsoTotal = r; }
}