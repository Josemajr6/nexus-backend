package com.nexus.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexus.service.SparkVotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Upvotes/downvotes en tiempo real.
 *
 * Angular suscripción para actualizar el contador sin recargar:
 *   client.subscribe('/topic/votos/oferta/42', msg => {
 *     const { score } = JSON.parse(msg.body);
 *     this.score = score;
 *   });
 *
 * El icono de flecha debe estar "activo" si tuVoto === 1 (upvote) o -1 (downvote).
 */
@RestController
@RequestMapping("/votos")
@Tag(name = "SparkVotos", description = "Upvotes/downvotes en tiempo real (SparkVoto)")
public class SparkVotoController {

    @Autowired private SparkVotoService votoService;

    @PostMapping("/oferta")
    @Operation(summary = "Upvote (+1) o downvote (-1) en una oferta")
    public ResponseEntity<Map<String, Object>> votarOferta(
            @RequestParam Integer actorId,
            @RequestParam Integer ofertaId,
            @RequestParam Integer valor) {
        try {
            int score = votoService.votarOferta(actorId, ofertaId, valor);
            return ResponseEntity.ok(Map.of(
                "ofertaId", ofertaId, "score", score,
                "tuVoto", votoService.getVotoActualOferta(actorId, ofertaId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/oferta/{ofertaId}")
    public ResponseEntity<Map<String, Object>> scoreOferta(
            @PathVariable Integer ofertaId,
            @RequestParam(required = false) Integer actorId) {
        return ResponseEntity.ok(Map.of(
            "ofertaId", ofertaId,
            "score",    votoService.getScoreOferta(ofertaId),
            "tuVoto",   actorId != null ? votoService.getVotoActualOferta(actorId, ofertaId) : 0));
    }

    @PostMapping("/producto")
    public ResponseEntity<Map<String, Object>> votarProducto(
            @RequestParam Integer actorId,
            @RequestParam Integer productoId,
            @RequestParam Integer valor) {
        try {
            int score = votoService.votarProducto(actorId, productoId, valor);
            return ResponseEntity.ok(Map.of(
                "productoId", productoId, "score", score,
                "tuVoto", votoService.getVotoActualProducto(actorId, productoId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<Map<String, Object>> scoreProducto(
            @PathVariable Integer productoId,
            @RequestParam(required = false) Integer actorId) {
        return ResponseEntity.ok(Map.of(
            "productoId", productoId,
            "score",      votoService.getScoreProducto(productoId),
            "tuVoto",     actorId != null ? votoService.getVotoActualProducto(actorId, productoId) : 0));
    }
}