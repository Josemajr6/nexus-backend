package com.nexus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Notificacion;
import com.nexus.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/notificacion")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del usuario")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Ver todas las notificaciones de un usuario")
    public ResponseEntity<List<Notificacion>> listar(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioId));
    }
    
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @Operation(summary = "Ver notificaciones no leídas")
    public ResponseEntity<List<Notificacion>> noLeidas(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerNoLeidas(usuarioId));
    }
    
    @GetMapping("/usuario/{usuarioId}/contador")
    @Operation(summary = "Contar notificaciones no leídas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(@PathVariable Integer usuarioId) {
        long count = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("noLeidas", count));
    }
    
    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<?> marcarLeida(@PathVariable Integer id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(Map.of("mensaje", "Notificación marcada como leída"));
    }
    
    @PutMapping("/usuario/{usuarioId}/leer-todas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ResponseEntity<?> marcarTodasLeidas(@PathVariable Integer usuarioId) {
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones marcadas como leídas"));
    }
}