package com.nexus.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexus.entity.*;
import com.nexus.repository.*;
/**
 * Usa NotificacionInApp (no "Notificacion").
 * getNotificaciones(Integer, int, int) requerido por NotificacionController line 44.
 */
@Service
public class NotificacionService {
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private ActorRepository        actorRepository;
    @Autowired private SimpMessagingTemplate  messagingTemplate;

    public List<NotificacionInApp> getNoLeidas(Integer actorId) { return notificacionRepository.findByActorIdAndLeidaFalseOrderByFechaDesc(actorId); }
    public List<NotificacionInApp> getTodas(Integer actorId)    { return notificacionRepository.findByActorIdOrderByFechaDesc(actorId); }
    public Page<NotificacionInApp> getNotificaciones(Integer actorId, int page, int size) { return notificacionRepository.findByActorIdOrderByFechaDesc(actorId,PageRequest.of(page,size)); }
    public long countNoLeidas(Integer actorId) { return notificacionRepository.countByActorIdAndLeidaFalse(actorId); }

    @Transactional public void marcarLeida(Integer id) { notificacionRepository.findById(id).ifPresent(n->{n.setLeida(true);notificacionRepository.save(n);}); }
    @Transactional public void marcarTodasLeidas(Integer actorId) { getNoLeidas(actorId).forEach(n->{n.setLeida(true);notificacionRepository.save(n);}); }
    @Transactional public void eliminar(Integer id) { notificacionRepository.deleteById(id); }

    @Transactional
    public NotificacionInApp crear(Integer actorId, TipoNotificacion tipo, String titulo, String mensaje, String url) {
        Actor actor=actorRepository.findById(actorId).orElse(null); if(actor==null)return null;
        NotificacionInApp n=new NotificacionInApp();
        n.setActor(actor); n.setTipo(tipo); n.setTitulo(titulo); n.setMensaje(mensaje); n.setUrl(url); n.setLeida(false); n.setFecha(LocalDateTime.now());
        NotificacionInApp g=notificacionRepository.save(n);
        try{messagingTemplate.convertAndSendToUser(actorId.toString(),"/queue/notificaciones",g);}catch(Exception e){System.err.println("WS: "+e.getMessage());}
        return g;
    }
    public void notificarNuevoMensaje(Integer id, String remitente)     { crear(id,TipoNotificacion.NUEVO_MENSAJE,"Nuevo mensaje","Tienes un nuevo mensaje de "+remitente,"/chat"); }
    public void notificarNuevaCompra(Integer id, String titulo)          { crear(id,TipoNotificacion.NUEVA_COMPRA,"Nueva venta","Han comprado tu producto: "+titulo,"/ventas"); }
    public void notificarCompraConfirmada(Integer id, String titulo)     { crear(id,TipoNotificacion.COMPRA_CONFIRMADA,"Compra confirmada","Tu compra de "+titulo+" ha sido confirmada","/compras"); }
    public void notificarEnvio(Integer id, String titulo)                { crear(id,TipoNotificacion.ENVIO_ACTUALIZADO,"Pedido enviado","Tu pedido de "+titulo+" ha sido enviado","/compras"); }
    public void notificarNuevaValoracion(Integer id, int puntuacion)     { crear(id,TipoNotificacion.NUEVA_VALORACION,"Nueva valoracion","Has recibido "+puntuacion+" estrellas","/perfil"); }
    public void notificarSparkEnOferta(Integer id, String titulo)        { crear(id,TipoNotificacion.SPARK_EN_OFERTA,"Tu oferta tiene nuevos votos","\""+titulo+"\" ha recibido Sparks","/ofertas"); }
    public void notificarNuevoComentario(Integer id, String titulo)      { crear(id,TipoNotificacion.NUEVO_COMENTARIO,"Nuevo comentario","Han comentado en: "+titulo,"/publicaciones"); }
    public void notificarDevolucion(Integer id, String titulo)           { crear(id,TipoNotificacion.DEVOLUCION,"Solicitud de devolucion","Han solicitado devolucion de: "+titulo,"/ventas"); }
    public void notificarSistema(Integer id, String mensaje)             { crear(id,TipoNotificacion.SISTEMA,"Notificacion del sistema",mensaje,null); }
}
