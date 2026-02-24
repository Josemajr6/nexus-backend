package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notificación en la app (campana de notificaciones, como Instagram/Twitter).
 * Se almacena en BD y se publica en tiempo real via WebSocket.
 * NO usa Firebase → funciona sin cuenta externa.
 */
@Entity
@Table(name = "notificacion_in_app", indexes = {
    @Index(name = "idx_notif_receptor", columnList = "receptor_id"),
    @Index(name = "idx_notif_leida",    columnList = "leida")
})
public class NotificacionInApp extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    private Actor receptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    /** URL a la que navega Angular al clicar la notificación */
    private String enlace;

    /** ID del objeto relacionado (oferta, producto, compra...) */
    private Integer referenciaId;

    @Column(nullable = false)
    private Boolean leida = false;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }

    public NotificacionInApp() {}

    public Actor    getReceptor()                  { return receptor; }
    public void     setReceptor(Actor r)           { this.receptor = r; }
    public TipoNotificacion getTipo()              { return tipo; }
    public void     setTipo(TipoNotificacion t)    { this.tipo = t; }
    public String   getTitulo()                    { return titulo; }
    public void     setTitulo(String t)            { this.titulo = t; }
    public String   getCuerpo()                    { return cuerpo; }
    public void     setCuerpo(String c)            { this.cuerpo = c; }
    public String   getEnlace()                    { return enlace; }
    public void     setEnlace(String e)            { this.enlace = e; }
    public Integer  getReferenciaId()              { return referenciaId; }
    public void     setReferenciaId(Integer id)    { this.referenciaId = id; }
    public Boolean  getLeida()                     { return leida; }
    public void     setLeida(Boolean l)            { this.leida = l; }
    public LocalDateTime getFechaCreacion()        { return fechaCreacion; }
}