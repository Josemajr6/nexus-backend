package com.nexus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.nexus.entity.ChatMensaje;
import com.nexus.service.ChatService;

import java.util.Map;

/**
 * Controlador WebSocket STOMP.
 *
 * ═══════════════════════════════════════════════════════════════
 *  GUÍA DE INTEGRACIÓN ANGULAR (copiar en chat.service.ts)
 * ═══════════════════════════════════════════════════════════════
 *
 *  // 1. Instalar:  npm install @stomp/stompjs sockjs-client
 *  //               npm install -D @types/sockjs-client
 *
 *  // 2. Conectar:
 *  import { Client } from '@stomp/stompjs';
 *  import SockJS from 'sockjs-client';
 *
 *  this.stompClient = new Client({
 *    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
 *    connectHeaders: { Authorization: 'Bearer ' + this.authService.getToken() },
 *    reconnectDelay: 3000,
 *    onConnect: () => {
 *
 *      // Suscripción a la sala del producto (todos los participantes)
 *      this.stompClient.subscribe(`/topic/chat/${productoId}`, (frame) => {
 *        const msg: ChatMensaje = JSON.parse(frame.body);
 *        this.mensajes.push(msg);        // Añadir al array del componente
 *        this.marcarLeido(productoId);   // Marcar como leído automáticamente
 *      });
 *
 *      // Cola privada para notificaciones (nuevo mensaje en otra pestaña)
 *      this.stompClient.subscribe(`/user/queue/notificaciones`, (frame) => {
 *        const data = JSON.parse(frame.body);
 *        this.badgeCount++;
 *      });
 *    }
 *  });
 *  this.stompClient.activate();
 *
 *  // 3. Enviar mensaje de texto:
 *  this.stompClient.publish({
 *    destination: '/app/chat.enviar',
 *    body: JSON.stringify({
 *      productoId: 42,
 *      remitenteId: 5,
 *      receptorId: 7,
 *      texto: 'Hola, ¿sigue disponible?',
 *      tipo: 'TEXTO'
 *    })
 *  });
 *
 *  // 4. Para imágenes/vídeos: usar ChatController REST → POST /chat/media
 *  //    El servidor publica el resultado en el topic automáticamente.
 *
 *  // 5. Propuesta de precio:
 *  this.stompClient.publish({
 *    destination: '/app/chat.enviar',
 *    body: JSON.stringify({
 *      productoId: 42, remitenteId: 5, receptorId: 7,
 *      tipo: 'OFERTA_PRECIO', precioPropuesto: 350.0
 *    })
 *  });
 * ═══════════════════════════════════════════════════════════════
 */
@Controller
public class ChatWebSocketController {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ChatService chatService;

    /**
     * Recibe un mensaje desde Angular y lo distribuye a todos los suscriptores
     * del topic de ese producto.
     *
     * Payload esperado:
     * {
     *   "productoId": 42,
     *   "remitenteId": 5,
     *   "receptorId": 7,
     *   "texto": "Hola, ¿sigue disponible?",
     *   "tipo": "TEXTO",               // TEXTO | OFERTA_PRECIO
     *   "precioPropuesto": null         // Solo para tipo OFERTA_PRECIO
     * }
     */
    @MessageMapping("/chat.enviar")
    public void enviarMensaje(@Payload Map<String, Object> payload) {
        Integer productoId    = (Integer) payload.get("productoId");
        Integer remitenteId   = (Integer) payload.get("remitenteId");
        Integer receptorId    = (Integer) payload.get("receptorId");
        String  texto         = (String)  payload.get("texto");
        String  tipoStr       = (String)  payload.getOrDefault("tipo", "TEXTO");
        Double  precioProp    = payload.get("precioPropuesto") != null
                                ? Double.valueOf(payload.get("precioPropuesto").toString())
                                : null;

        ChatMensaje guardado;

        if ("OFERTA_PRECIO".equals(tipoStr) && precioProp != null) {
            guardado = chatService.guardarPropuestaPrecio(
                productoId, remitenteId, receptorId, precioProp);
        } else {
            guardado = chatService.guardarMensajeTexto(
                productoId, remitenteId, receptorId, texto);
        }

        // Publicar al topic del producto → Angular recibe en tiempo real
        messagingTemplate.convertAndSend("/topic/chat/" + productoId, guardado);

        // Notificar al receptor en su cola privada (para el badge de mensajes nuevos)
        if (receptorId != null) {
            messagingTemplate.convertAndSendToUser(
                receptorId.toString(),
                "/queue/notificaciones",
                Map.of("tipo", "NUEVO_MENSAJE", "productoId", productoId, "remitenteId", remitenteId)
            );
        }
    }

    /**
     * Marcar mensajes como leídos (los checks se vuelven azules en Angular).
     *
     * Angular publica aquí cuando el usuario abre la conversación:
     *   client.publish({ destination: '/app/chat.leer',
     *                    body: JSON.stringify({ productoId: 42, receptorId: 7 }) });
     */
    @MessageMapping("/chat.leer")
    public void marcarLeidos(@Payload Map<String, Integer> payload) {
        Integer productoId  = payload.get("productoId");
        Integer receptorId  = payload.get("receptorId");

        chatService.marcarLeidos(productoId, receptorId);

        // Notificar al remitente que sus mensajes fueron leídos (checks azules)
        messagingTemplate.convertAndSend(
            "/topic/chat/" + productoId + "/leidos",
            Map.of("receptorId", receptorId, "leido", true)
        );
    }

    /**
     * Indicador "está escribiendo..." (typing indicator).
     * Angular publica aquí onKeyup; el receptor ve "Usuario está escribiendo..."
     */
    @MessageMapping("/chat.escribiendo")
    public void escribiendo(@Payload Map<String, Object> payload) {
        Integer productoId  = (Integer) payload.get("productoId");
        Integer remitenteId = (Integer) payload.get("remitenteId");
        Boolean escribiendo = (Boolean) payload.getOrDefault("escribiendo", true);

        messagingTemplate.convertAndSend(
            "/topic/chat/" + productoId + "/escribiendo",
            Map.of("remitenteId", remitenteId, "escribiendo", escribiendo)
        );
    }

    // ── Método público para que otros servicios publiquen mensajes de sistema ──

    /**
     * Publica un mensaje de sistema en el chat de un producto.
     * Lo usan EnvioService y CompraService para notificar eventos:
     * "✅ Pago confirmado", "📦 Pedido enviado", "🎉 Entrega confirmada"
     */
    public void publicarMensajeSistema(Integer productoId, Integer remitenteId,
                                        Integer receptorId, String texto) {
        ChatMensaje msg = chatService.mensajeSistema(productoId, remitenteId, receptorId, texto);
        messagingTemplate.convertAndSend("/topic/chat/" + productoId, msg);
    }
}