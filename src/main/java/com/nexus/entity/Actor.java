package com.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Clase base para Usuario, Empresa y Admin.
 * Usa herencia JOINED: tabla actor + tabla especifica.
 */
@Entity
@Table(name = "actor")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Actor extends DomainEntity {

    @Column(nullable = false, unique = true)
    private String user;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // ---- 2FA -----------------------------------------------------------
    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    private String twoFactorMethod;  // "TOTP" o "EMAIL"
    private String twoFactorSecret;  // Secret TOTP (encriptado)

    // ---- Sesiones -------------------------------------------------------
    /** Incrementar para invalidar todos los JWT activos */
    @Column(nullable = false)
    private int jwtVersion = 0;

    // ---- Estado de la cuenta --------------------------------------------
    @Column(nullable = false)
    private boolean cuentaEliminada = false;

    @Column(nullable = false)
    private boolean cuentaVerificada = false;

    private LocalDateTime fechaRegistro;

    // ---- Notificaciones -------------------------------------------------
    @Embedded
    private ActorNotificacionConfig notificacionConfig = new ActorNotificacionConfig();

    @PrePersist
    protected void onActorCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (notificacionConfig == null) notificacionConfig = new ActorNotificacionConfig();
    }

    // ---- Getters / Setters -----------------------------------------------

    public String  getUser()                                  { return user; }
    public void    setUser(String u)                          { this.user = u; }
    public String  getEmail()                                 { return email; }
    public void    setEmail(String e)                         { this.email = e; }
    public String  getPassword()                              { return password; }
    public void    setPassword(String p)                      { this.password = p; }
    public boolean isTwoFactorEnabled()                       { return twoFactorEnabled; }
    public void    setTwoFactorEnabled(boolean b)             { this.twoFactorEnabled = b; }
    public String  getTwoFactorMethod()                       { return twoFactorMethod; }
    public void    setTwoFactorMethod(String m)               { this.twoFactorMethod = m; }
    public String  getTwoFactorSecret()                       { return twoFactorSecret; }
    public void    setTwoFactorSecret(String s)               { this.twoFactorSecret = s; }
    public int     getJwtVersion()                            { return jwtVersion; }
    public void    setJwtVersion(int v)                       { this.jwtVersion = v; }
    public boolean isCuentaEliminada()                        { return cuentaEliminada; }
    public void    setCuentaEliminada(boolean b)              { this.cuentaEliminada = b; }
    public boolean isCuentaVerificada()                       { return cuentaVerificada; }
    public void    setCuentaVerificada(boolean b)             { this.cuentaVerificada = b; }
    public LocalDateTime getFechaRegistro()                   { return fechaRegistro; }
    public void    setFechaRegistro(LocalDateTime f)          { this.fechaRegistro = f; }
    public ActorNotificacionConfig getNotificacionConfig()    { return notificacionConfig; }
    public void    setNotificacionConfig(ActorNotificacionConfig c) { this.notificacionConfig = c; }
}