package com.nexus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.*;
import com.nexus.service.DevolucionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/devolucion")
@Tag(name = "Devoluciones", description = "Sistema completo de devoluciones y reembolsos")
public class DevolucionController {

    @Autowired private DevolucionService devolucionService;

    @GetMapping("/comprador/{id}")
    public ResponseEntity<List<Devolucion>> porComprador(@PathVariable Integer id) {
        return ResponseEntity.ok(devolucionService.getDevolucionesComoComprador(id));
    }

    @GetMapping("/vendedor/{id}")
    public ResponseEntity<List<Devolucion>> porVendedor(@PathVariable Integer id) {
        return ResponseEntity.ok(devolucionService.getDevolucionesComoVendedor(id));
    }

    /**
     * El comprador solicita una devolución con fotos del problema.
     *
     * Angular (FormData):
     *   formData.append('compraId', '42');
     *   formData.append('motivo', 'DEFECTUOSO');
     *   formData.append('descripcion', 'La pantalla tiene una línea rota...');
     *   fotos.forEach(f => formData.append('fotos', f));
     *   http.post('/devolucion/solicitar', formData)
     */
    @PostMapping(value = "/solicitar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Solicitar devolución (con fotos del defecto)")
    public ResponseEntity<?> solicitar(
            @RequestParam Integer compraId,
            @RequestParam MotivoDevolucion motivo,
            @RequestParam(required = false, defaultValue = "") String descripcion,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            Devolucion d = devolucionService.solicitar(compraId, motivo, descripcion, fotos);
            return ResponseEntity.status(HttpStatus.CREATED).body(d);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * El vendedor acepta o rechaza.
     * Body: { "aceptada": true, "nota": "De acuerdo, envíalo a Calle Mayor 1..." }
     */
    @PostMapping("/{id}/responder")
    @Operation(summary = "Vendedor acepta o rechaza la devolución")
    public ResponseEntity<?> responder(@PathVariable Integer id,
                                        @RequestBody Map<String, Object> body) {
        try {
            boolean aceptada = Boolean.parseBoolean(body.get("aceptada").toString());
            String  nota     = (String) body.getOrDefault("nota", "");
            return ResponseEntity.ok(devolucionService.responder(id, aceptada, nota));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Comprador confirma que envió el producto de vuelta.
     * Body: { "transportista": "Correos", "tracking": "1Z999..." }
     */
    @PostMapping("/{id}/enviada")
    @Operation(summary = "Comprador confirma el envío del producto de vuelta")
    public ResponseEntity<?> marcarEnviada(@PathVariable Integer id,
                                            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(devolucionService.marcarDevolucionEnviada(
                id, body.get("transportista"), body.get("tracking")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vendedor confirma que recibió el producto → reembolso automático.
     */
    @PostMapping("/{id}/completar")
    @Operation(summary = "Vendedor confirma recepción → reembolso automático al comprador")
    public ResponseEntity<?> completar(@PathVariable Integer id) {
        try {
            Devolucion d = devolucionService.confirmarRecepcionDevolucion(id);
            return ResponseEntity.ok(Map.of(
                "mensaje", "💸 Reembolso procesado. El comprador recibirá el dinero en 3-5 días.",
                "devolucion", d
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}