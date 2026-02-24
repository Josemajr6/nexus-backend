package com.nexus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.*;
import com.nexus.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reporte")
@Tag(name = "Reportes", description = "Denuncias de usuarios, productos y ofertas")
public class ReporteController {

    @Autowired private ReporteService reporteService;

    /**
     * Denunciar un contenido.
     * Body: { "reportadorId": 5, "tipo": "PRODUCTO", "motivo": "FRAUDE_ESTAFA",
     *         "descripcion": "...", "objetoId": 42 }
     */
    @PostMapping
    @Operation(summary = "Reportar un usuario, producto, oferta o mensaje")
    public ResponseEntity<?> reportar(@RequestBody Map<String, Object> body) {
        try {
            Reporte r = reporteService.reportar(
                (Integer) body.get("reportadorId"),
                TipoReporte.valueOf((String) body.get("tipo")),
                MotivoReporte.valueOf((String) body.get("motivo")),
                (String) body.getOrDefault("descripcion", ""),
                (Integer) body.get("objetoId")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Reporte enviado. Lo revisaremos en breve.",
                "reporteId", r.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-reportes/{reportadorId}")
    public ResponseEntity<List<Reporte>> misReportes(@PathVariable Integer reportadorId) {
        return ResponseEntity.ok(reporteService.getMisReportes(reportadorId));
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping("/admin/pendientes")
    @Operation(summary = "[ADMIN] Ver reportes pendientes")
    public ResponseEntity<List<Reporte>> pendientes() {
        return ResponseEntity.ok(reporteService.getPendientes());
    }

    /**
     * [ADMIN] Resolver un reporte.
     * Body: { "estado": "RESUELTO", "notaAdmin": "El producto ha sido eliminado." }
     */
    @PatchMapping("/admin/{id}/resolver")
    @Operation(summary = "[ADMIN] Resolver o desestimar un reporte")
    public ResponseEntity<?> resolver(@PathVariable Integer id,
                                       @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(reporteService.resolver(
                id,
                EstadoReporte.valueOf(body.get("estado")),
                body.getOrDefault("notaAdmin", "")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}