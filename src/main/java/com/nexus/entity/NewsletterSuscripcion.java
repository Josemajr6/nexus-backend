package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Suscripcion al newsletter de Nexus.
 *
 * Cumple con RGPD / LSSI:
 *  - Double opt-in: el suscriptor confirma via email antes de activarse
 *  - Token de baja unico e irremplazable
 *  - Se guarda la fecha y la IP del consentimiento (art. 7 RGPD)
 *  - Soft-delete: nunca se borra el registro, solo se marca como dado de baja
 *    para poder demostrar que se proceso la solicitud de baja
 */
@Entity
@Table(name = "newsletter_suscripcion", indexes = {
    @Index(name = "idx_newsletter_email",     columnList = "email", unique = true),
    @Index(name = "idx_newsletter_token_conf",columnList = "tokenConfirmacion"),
    @Index(name = "idx_newsletter_token_baja", columnList = "tokenBaja")
})
public class NewsletterSuscripcion extends DomainEntity {

    @Column(nullable = false, unique = true)
    private String email;

    /** Nombre opcional para personalizar los emails */
    private String nombre;

    // ---- Estado ---------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSuscripcion estado = EstadoSuscripcion.PENDIENTE;

    // ---- Double opt-in --------------------------------------------------
    /** Token enviado al email para confirmar la suscripcion */
    @Column(unique = true)
    private String tokenConfirmacion;

    private LocalDateTime fechaEnvioConfirmacion;
    private LocalDateTime fechaConfirmacion;

    // ---- Baja -----------------------------------------------------------
    /** Token unico para darse de baja (incluido en cada email enviado) */
    @Column(nullable = false, unique = true)
    private String tokenBaja;

    private LocalDateTime fechaBaja;

    /** Motivo de baja seleccionado por el usuario */
    private String motivoBaja;

    // ---- Preferencias de contenido --------------------------------------
    @Column(nullable = false)
    private boolean recibirOfertas    = true;

    @Column(nullable = false)
    private boolean recibirNoticias   = true;

    @Column(nullable = false)
    private boolean recibirTrending   = true;

    /** Frecuencia: DIARIO, SEMANAL, QUINCENAL, MENSUAL */
    @Column(nullable = false)
    private String frecuencia = "SEMANAL";

    // ---- Consentimiento (RGPD art. 7) -----------------------------------
    /** Fecha en que se dio el consentimiento inicial */
    private LocalDateTime fechaConsentimiento;

    /** IP desde la que se suscribio (para demostrar consentimiento) */
    private String ipConsentimiento;

    /** Version de la politica de privacidad aceptada */
    private String versionPolitica;

    // ---- Auditoria ------------------------------------------------------
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    // ---- Getters / Setters ----------------------------------------------
    public String   getEmail()                                { return email; }
    public void     setEmail(String e)                        { this.email = e; }
    public String   getNombre()                               { return nombre; }
    public void     setNombre(String n)                       { this.nombre = n; }
    public EstadoSuscripcion getEstado()                      { return estado; }
    public void     setEstado(EstadoSuscripcion e)            { this.estado = e; }
    public String   getTokenConfirmacion()                    { return tokenConfirmacion; }
    public void     setTokenConfirmacion(String t)            { this.tokenConfirmacion = t; }
    public LocalDateTime getFechaEnvioConfirmacion()          { return fechaEnvioConfirmacion; }
    public void     setFechaEnvioConfirmacion(LocalDateTime f){ this.fechaEnvioConfirmacion = f; }
    public LocalDateTime getFechaConfirmacion()               { return fechaConfirmacion; }
    public void     setFechaConfirmacion(LocalDateTime f)     { this.fechaConfirmacion = f; }
    public String   getTokenBaja()                            { return tokenBaja; }
    public void     setTokenBaja(String t)                    { this.tokenBaja = t; }
    public LocalDateTime getFechaBaja()                       { return fechaBaja; }
    public void     setFechaBaja(LocalDateTime f)             { this.fechaBaja = f; }
    public String   getMotivoBaja()                           { return motivoBaja; }
    public void     setMotivoBaja(String m)                   { this.motivoBaja = m; }
    public boolean  isRecibirOfertas()                        { return recibirOfertas; }
    public void     setRecibirOfertas(boolean b)              { this.recibirOfertas = b; }
    public boolean  isRecibirNoticias()                       { return recibirNoticias; }
    public void     setRecibirNoticias(boolean b)             { this.recibirNoticias = b; }
    public boolean  isRecibirTrending()                       { return recibirTrending; }
    public void     setRecibirTrending(boolean b)             { this.recibirTrending = b; }
    public String   getFrecuencia()                           { return frecuencia; }
    public void     setFrecuencia(String f)                   { this.frecuencia = f; }
    public LocalDateTime getFechaConsentimiento()             { return fechaConsentimiento; }
    public void     setFechaConsentimiento(LocalDateTime f)   { this.fechaConsentimiento = f; }
    public String   getIpConsentimiento()                     { return ipConsentimiento; }
    public void     setIpConsentimiento(String ip)            { this.ipConsentimiento = ip; }
    public String   getVersionPolitica()                      { return versionPolitica; }
    public void     setVersionPolitica(String v)              { this.versionPolitica = v; }
    public LocalDateTime getFechaRegistro()                   { return fechaRegistro; }
}