package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Denuncia de un contenido o usuario.
 * El admin revisa todas las denuncias pendientes.
 */
@Entity
@Table(name = "reporte")
public class Reporte extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportador_id", nullable = false)
    private Actor reportador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoReporte motivo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // ── El objeto denunciado (solo uno puede tener valor) ────────────────
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_denunciado_id")
    private Actor  usuarioDenunciado;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "producto_denunciado_id")
    private Producto productoDenunciado;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "oferta_denunciada_id")
    private Oferta ofertaDenunciada;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "mensaje_denunciado_id")
    private ChatMensaje mensajeDenunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    private String  notaAdmin;   // Resolución del admin
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;

    public Reporte() { super(); this.fechaReporte = LocalDateTime.now(); }

    public Actor    getReportador()              { return reportador; }
    public void     setReportador(Actor r)       { this.reportador = r; }
    public TipoReporte getTipo()                 { return tipo; }
    public void     setTipo(TipoReporte t)       { this.tipo = t; }
    public MotivoReporte getMotivo()             { return motivo; }
    public void     setMotivo(MotivoReporte m)   { this.motivo = m; }
    public String   getDescripcion()             { return descripcion; }
    public void     setDescripcion(String d)     { this.descripcion = d; }
    public Actor    getUsuarioDenunciado()        { return usuarioDenunciado; }
    public void     setUsuarioDenunciado(Actor a) { this.usuarioDenunciado = a; }
    public Producto getProductoDenunciado()      { return productoDenunciado; }
    public void     setProductoDenunciado(Producto p) { this.productoDenunciado = p; }
    public Oferta   getOfertaDenunciada()         { return ofertaDenunciada; }
    public void     setOfertaDenunciada(Oferta o)  { this.ofertaDenunciada = o; }
    public ChatMensaje getMensajeDenunciado()     { return mensajeDenunciado; }
    public void     setMensajeDenunciado(ChatMensaje m) { this.mensajeDenunciado = m; }
    public EstadoReporte getEstado()             { return estado; }
    public void     setEstado(EstadoReporte e)   { this.estado = e; }
    public String   getNotaAdmin()               { return notaAdmin; }
    public void     setNotaAdmin(String n)       { this.notaAdmin = n; }
    public LocalDateTime getFechaReporte()       { return fechaReporte; }
    public LocalDateTime getFechaResolucion()    { return fechaResolucion; }
    public void     setFechaResolucion(LocalDateTime f) { this.fechaResolucion = f; }
}