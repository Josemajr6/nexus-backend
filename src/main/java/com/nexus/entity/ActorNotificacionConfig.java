package com.nexus.entity;

import jakarta.persistence.Embeddable;

/**
 * Preferencias de notificaciones del actor.
 * @Embeddable → columnas en la tabla actor.
 */
@Embeddable
public class ActorNotificacionConfig {
    private Boolean emailNuevoMensaje  = true;
    private Boolean emailNuevaCompra   = true;
    private Boolean emailEstadoEnvio   = true;
    private Boolean emailMarketing     = false;
    private Boolean pushNuevoMensaje   = true;
    private Boolean pushNuevaCompra    = true;

    public ActorNotificacionConfig() {}

    public Boolean getEmailNuevoMensaje()               { return emailNuevoMensaje; }
    public void    setEmailNuevoMensaje(Boolean v)      { this.emailNuevoMensaje = v; }
    public Boolean getEmailNuevaCompra()                { return emailNuevaCompra; }
    public void    setEmailNuevaCompra(Boolean v)       { this.emailNuevaCompra = v; }
    public Boolean getEmailEstadoEnvio()                { return emailEstadoEnvio; }
    public void    setEmailEstadoEnvio(Boolean v)       { this.emailEstadoEnvio = v; }
    public Boolean getEmailMarketing()                  { return emailMarketing; }
    public void    setEmailMarketing(Boolean v)         { this.emailMarketing = v; }
    public Boolean getPushNuevoMensaje()                { return pushNuevoMensaje; }
    public void    setPushNuevoMensaje(Boolean v)       { this.pushNuevoMensaje = v; }
    public Boolean getPushNuevaCompra()                 { return pushNuevaCompra; }
    public void    setPushNuevaCompra(Boolean v)        { this.pushNuevaCompra = v; }
}