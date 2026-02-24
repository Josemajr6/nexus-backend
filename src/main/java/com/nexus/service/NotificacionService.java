package com.nexus.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.*;
import com.nexus.repository.*;

/**
 * Notificaciones en la app sin Firebase.
 *
 * Flujo:
 *  1. Se guarda NotificacionInApp en BD
 *  2. Se publica por WebSocket → Angular actualiza el badge en tiempo real
 *  3. Si el usuario tiene la preferencia activa, se manda un email
 *
 * Angular — suscribirse al badge (campana del navbar):
 *   client.subscribe(`/user/${userId}/queue/notificaciones`, msg => {
 *     const { noLeidas } = JSON.parse(msg.body);
 *     this.badgeCount = noLeidas;
 *   });
 */
@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private ActorRepository        actorRepository;
    @Autowired private SimpMessagingTemplate  messagingTemplate;
    @Autowired private EmailService           emailService;

    // ── Método central ────────────────────────────────────────────────────

    @Async
    @Transactional
    public void notificar(Integer receptorId, TipoNotificacion tipo,
                           String titulo, String cuerpo,
                           String enlace, Integer referenciaId) {
        Actor receptor = actorRepository.findById(receptorId).orElse(null);
        if (receptor == null || receptor.isCuentaEliminada()) return;

        NotificacionInApp n = new NotificacionInApp();
        n.setReceptor(receptor);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setCuerpo(cuerpo);
        n.setEnlace(enlace);
        n.setReferenciaId(referenciaId);
        notificacionRepository.save(n);

        long noLeidas = notificacionRepository.countNoLeidasByReceptorId(receptorId);
        messagingTemplate.convertAndSendToUser(
            receptorId.toString(),
            "/queue/notificaciones",
            Map.of(
                "id",       n.getId(),
                "tipo",     tipo.name(),
                "titulo",   titulo,
                "cuerpo",   cuerpo,
                "enlace",   enlace != null ? enlace : "",
                "noLeidas", noLeidas
            )
        );

        ActorNotificacionConfig cfg = receptor.getNotificacionConfig();
        if (cfg != null && debeEnviarEmail(tipo, cfg)) {
            emailService.enviarEmail(receptor.getEmail(), titulo, cuerpo);
        }
    }

    /** Shortcut para notificaciones simples de sistema */
    @Async
    public void notificarActorPorId(Integer receptorId, String titulo) {
        notificar(receptorId, TipoNotificacion.SISTEMA, titulo, titulo, null, null);
    }

    // ── Especializados ────────────────────────────────────────────────────

    @Async
    public void notificarNuevaMensaje(Integer receptorId, String remitente, Integer productoId) {
        notificar(receptorId, TipoNotificacion.NUEVO_MENSAJE,
            "Nuevo mensaje de " + remitente,
            remitente + " te ha enviado un mensaje",
            "/chat/" + productoId, productoId);
    }

    @Async
    public void notificarNuevaCompra(Integer vendedorId, String tituloProducto, Integer compraId) {
        notificar(vendedorId, TipoNotificacion.NUEVA_COMPRA,
            "Nueva venta",
            "Han comprado: " + tituloProducto,
            "/mis-ventas/" + compraId, compraId);
    }

    @Async
    public void notificarEstadoEnvio(Integer compradorId, String estado, Integer envioId) {
        notificar(compradorId, TipoNotificacion.ESTADO_ENVIO,
            "Actualizacion de tu pedido",
            estado,
            "/mis-compras/" + envioId, envioId);
    }

    @Async
    public void notificarNuevaValoracion(Integer vendedorId, Integer estrellas, String tituloProducto) {
        notificar(vendedorId, TipoNotificacion.NUEVA_VALORACION,
            "Nueva valoracion: " + estrellas + " estrellas",
            "Han valorado tu venta de \"" + tituloProducto + "\"",
            "/mis-valoraciones", null);
    }

    @Async
    public void notificarNuevoVoto(Integer publicadorId, String tituloOferta, int score) {
        notificar(publicadorId, TipoNotificacion.NUEVO_VOTO,
            "Tu oferta esta en tendencia",
            "\"" + tituloOferta + "\" tiene " + score + " SparkVotos",
            "/ofertas", null);
    }

    // ── Lectura ────────────────────────────────────────────────────────────

    public Page<NotificacionInApp> getNotificaciones(Integer receptorId, int page, int size) {
        return notificacionRepository.findByReceptorIdOrderByFechaCreacionDesc(
            receptorId, PageRequest.of(page, size));
    }

    public long getNoLeidas(Integer receptorId) {
        return notificacionRepository.countNoLeidasByReceptorId(receptorId);
    }

    @Transactional
    public void marcarTodasLeidas(Integer receptorId) {
        notificacionRepository.marcarTodasLeidasByReceptorId(receptorId);
    }

    @Transactional
    public void marcarLeida(Integer notificacionId) {
        notificacionRepository.marcarLeidaById(notificacionId);
    }

    // ── Privado ────────────────────────────────────────────────────────────

    private boolean debeEnviarEmail(TipoNotificacion tipo, ActorNotificacionConfig cfg) {
        return switch (tipo) {
            case NUEVO_MENSAJE                              -> Boolean.TRUE.equals(cfg.getEmailNuevoMensaje());
            case NUEVA_COMPRA                               -> Boolean.TRUE.equals(cfg.getEmailNuevaCompra());
            case ESTADO_ENVIO, PEDIDO_ENVIADO,
                 ENTREGA_CONFIRMADA                         -> Boolean.TRUE.equals(cfg.getEmailEstadoEnvio());
            default                                        -> false;
        };
    }
}