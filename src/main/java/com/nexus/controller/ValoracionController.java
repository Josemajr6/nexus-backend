package com.nexus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Valoracion;
import com.nexus.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/valoracion")
@Tag(name = "Valoraciones", description = "Sistema de reseñas de 5 estrellas")
public class ValoracionController {

    @Autowired private ValoracionService valoracionService;

    /** Perfil público del vendedor: media y lista de reseñas */
    @GetMapping("/vendedor/{id}")
    @Operation(summary = "Reseñas de un vendedor (público)")
    public ResponseEntity<List<Valoracion>> porVendedor(@PathVariable Integer id) {
        return ResponseEntity.ok(valoracionService.getValoracionesVendedor(id));
    }

    @GetMapping("/vendedor/{id}/resumen")
    @Operation(summary = "Media de estrellas y número de reseñas")
    public ResponseEntity<Map<String, Object>> resumen(@PathVariable Integer id) {
        return ResponseEntity.ok(valoracionService.getResumenVendedor(id));
    }

    @GetMapping("/mis-valoraciones/{compradorId}")
    public ResponseEntity<List<Valoracion>> misValoraciones(@PathVariable Integer compradorId) {
        return ResponseEntity.ok(valoracionService.getMisValoraciones(compradorId));
    }

    /**
     * Dejar una reseña.
     * Body: { "compraId": 42, "compradorId": 5, "estrellas": 5, "comentario": "..." }
     */
    @PostMapping
    @Operation(summary = "Valorar al vendedor tras una compra completada")
    public ResponseEntity<?> valorar(@RequestBody Map<String, Object> body) {
        try {
            Valoracion v = valoracionService.valorar(
                (Integer) body.get("compraId"),
                (Integer) body.get("compradorId"),
                (Integer) body.get("estrellas"),
                (String) body.getOrDefault("comentario", "")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(v);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Responder a una reseña (vendedor).
     * Body: { "vendedorId": 7, "respuesta": "Gracias por tu compra..." }
     */
    @PatchMapping("/{id}/responder")
    @Operation(summary = "El vendedor responde públicamente a una reseña")
    public ResponseEntity<?> responder(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(valoracionService.responder(
                id, (Integer) body.get("vendedorId"), (String) body.get("respuesta")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}