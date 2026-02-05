package com.nexus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexus.entity.Notificacion;
import com.nexus.entity.Oferta;
import com.nexus.entity.TipoNotificacion;
import com.nexus.entity.Usuario;
import com.nexus.repository.NotificacionRepository;

@Service
public class NotificacionService {
    
    @Autowired
    private NotificacionRepository notificacionRepository;
    
    public List<Notificacion> obtenerPorUsuario(Integer usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId);
    }
    
    public List<Notificacion> obtenerNoLeidas(Integer usuarioId) {
        return notificacionRepository.findNoLeidasByUsuarioId(usuarioId);
    }
    
    public long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countNoLeidasByUsuarioId(usuarioId);
    }
    
    public void marcarComoLeida(Integer notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(notif -> {
            notif.setLeida(true);
            notificacionRepository.save(notif);
        });
    }
    
    public void marcarTodasComoLeidas(Integer usuarioId) {
        List<Notificacion> noLeidas = obtenerNoLeidas(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
    }
    
    // Notificar hito de Spark
    public void notificarHitoSpark(Oferta oferta) {
        Notificacion notif = new Notificacion();
        notif.setUsuario((Usuario) oferta.getActor());
        notif.setTitulo("🎉 ¡Hito alcanzado!");
        notif.setMensaje(String.format("Tu oferta '%s' ha alcanzado %d Spark Score!", 
                oferta.getTitulo(), oferta.getSparkScore()));
        notif.setTipo(TipoNotificacion.SPARK_MILESTONE);
        notif.setUrlDestino("/oferta/" + oferta.getId());
        
        notificacionRepository.save(notif);
    }
}