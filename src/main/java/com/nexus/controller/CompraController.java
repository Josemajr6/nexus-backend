package com.nexus.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Compra;
import com.nexus.service.CompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compra")
@Tag(name = "Compras", description = "Gestión de transacciones y pedidos")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @GetMapping
    @Operation(summary = "Listar todas las compras (Admin)")
    public ResponseEntity<List<Compra>> findAll() {
        return ResponseEntity.ok(compraService.findAll());
    }

    @GetMapping("/historial/{usuarioId}")
    @Operation(summary = "Ver historial de compras de un usuario")
    public ResponseEntity<List<Compra>> historialUsuario(@PathVariable Integer usuarioId) {
        List<Compra> historial = compraService.findHistorialUsuario(usuarioId);
        
        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(historial);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una compra")
    public ResponseEntity<Compra> findById(@PathVariable Integer id) {
        Optional<Compra> oCompra = compraService.findById(id);

        if (oCompra.isPresent()) {
            return ResponseEntity.ok(oCompra.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/realizar")
    @Operation(summary = "Ejecutar una compra (Transacción)")
    public ResponseEntity<Object> realizarCompra(@RequestParam Integer productoId, @RequestParam Integer compradorId) {
        try {
            // Intentamos procesar la compra llamando al servicio transaccional
            Compra compra = compraService.procesarCompra(productoId, compradorId);

            if (compra != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(compra);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto o Usuario no encontrados");
            }

        } catch (IllegalStateException e) {
            // Captura error: El producto no está disponible
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // Captura error: Usuario intentando comprarse a sí mismo
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        } catch (Exception e) {
            // Captura cualquier otro error no previsto
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la compra");
        }
    }
}