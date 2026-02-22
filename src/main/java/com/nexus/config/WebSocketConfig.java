package com.nexus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración del broker WebSocket con STOMP.
 *
 * Angular se conecta así:
 *   import { Client } from '@stomp/stompjs';
 *   import SockJS from 'sockjs-client';
 *
 *   const client = new Client({
 *     webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
 *     connectHeaders: { Authorization: 'Bearer ' + token },
 *     onConnect: () => {
 *       // Suscribirse al chat de un producto
 *       client.subscribe(`/topic/chat/${productoId}`, (msg) => {
 *         const mensaje = JSON.parse(msg.body);
 *       });
 *     }
 *   });
 *   client.activate();
 *
 *   // Enviar mensaje:
 *   client.publish({
 *     destination: '/app/chat.enviar',
 *     body: JSON.stringify({ productoId, remitenteId, texto, tipo: 'TEXTO' })
 *   });
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo de los topics a los que se suscriben los clientes
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para los mensajes que van al @MessageMapping del servidor
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes privados usuario-a-usuario
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En prod: "http://localhost:4200", "https://nexus-app.es"
                .withSockJS(); // SockJS como fallback para navegadores sin WebSocket nativo
    }
}