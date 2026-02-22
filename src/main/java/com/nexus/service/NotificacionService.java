package com.nexus.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nexus.entity.*;
import com.nexus.repository.NotificacionRepository;

@Service
public class NotificacionService {

    @Autowired private NotificacionRepository notificacionRepository;

    public List<Notificacion> obtenerPorUsuario(Integer id)  { return notificacionRepository.findByUsuarioId(id); }
    public List<Notificacion> obtenerNoLeidas(Integer id)    { return notificacionRepository.findNoLeidasByUsuarioId(id); }
    public long contarNoLeidas(Integer id)                   { return notificacionRepository.countNoLeidasByUsuarioId(id); }

    public void marcarComoLeida(Integer id) {
        notificacionRepository.findById(id).ifPresent(n -> {
            n.setLeida(true); notificacionRepository.save(n);
        });
    }

    public void marcarTodasComoLeidas(Integer usuarioId) {
        List<Notificacion> nl = obtenerNoLeidas(usuarioId);
        nl.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(nl);
    }

    // BUG FIX: Guard instanceof antes del cast — evita ClassCastException si el actor es Empresa
    public void notificarHitoSpark(Oferta oferta) {
        Actor actor = oferta.getActor();
        if (!(actor instanceof Usuario usuario)) return;

        Notificacion notif = new Notificacion();
        notif.setUsuario(usuario);
        notif.setTitulo("🎉 ¡Hito alcanzado!");
        notif.setMensaje(String.format("Tu oferta '%s' ha alcanzado %d Spark Score!", oferta.getTitulo(), oferta.getSparkScore()));
        notif.setTipo(TipoNotificacion.SPARK_MILESTONE);
        notif.setUrlDestino("/oferta/" + oferta.getId());
        notificacionRepository.save(notif);
    }
}