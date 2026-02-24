package com.nexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.NotificacionInApp;
import com.nexus.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Panel de notificaciones (campana 🔔).
 *
 * Angular — badge de notificaciones:
 *   // Suscribirse al WebSocket para actualizaciones en tiempo real:
 *   client.subscribe(`/user/${userId}/queue/notificaciones`, msg => {
 *     const { noLeidas } = JSON.parse(msg.body);
 *     this.badgeCount = noLeidas;
 *   });
 *
 *   // Al abrir el panel de notificaciones:
 *   GET /notificaciones?receptorId=5&page=0&size=20
 *
 *   // Al marcar todo como leído:
 *   PATCH /notificaciones/leer-todas?receptorId=5
 */
@RestController
@RequestMapping("/notificaciones")
@Tag(name = "Notificaciones", description = "Campana 🔔 — sin Firebase, solo WebSocket + BD")
public class NotificacionController {

    @Autowired private NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Listar notificaciones paginadas")
    public ResponseEntity<Page<NotificacionInApp>> listar(
            @RequestParam Integer receptorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificacionService.getNotificaciones(receptorId, page, size));
    }

    @GetMapping("/no-leidas/{receptorId}")
    @Operation(summary = "Número de notificaciones no leídas (para el badge)")
    public ResponseEntity<Map<String, Long>> noLeidas(@PathVariable Integer receptorId) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.getNoLeidas(receptorId)));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<?> marcarLeida(@PathVariable Integer id) {
        notificacionService.marcarLeida(id);
        return ResponseEntity.ok(Map.of("mensaje", "Notificación marcada como leída"));
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<?> marcarTodasLeidas(@RequestParam Integer receptorId) {
        notificacionService.marcarTodasLeidas(receptorId);
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones marcadas como leídas"));
    }
}