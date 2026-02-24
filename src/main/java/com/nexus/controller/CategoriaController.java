package com.nexus.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexus.entity.Categoria;
import com.nexus.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API de categorias.
 *
 * Angular - cargar menu de categorias:
 *   GET /categorias/raiz         -> navbar principal
 *   GET /categorias              -> select/dropdown completo
 *   GET /categorias/{id}/hijas   -> sub-categorias al hacer clic
 */
@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias")
public class CategoriaController {

    @Autowired private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Todas las categorias activas (para selects)")
    public ResponseEntity<List<Categoria>> todas() {
        return ResponseEntity.ok(categoriaService.getTodas());
    }

    @GetMapping("/raiz")
    @Operation(summary = "Categorias raiz para la navbar")
    public ResponseEntity<List<Categoria>> raiz() {
        return ResponseEntity.ok(categoriaService.getRaizActivas());
    }

    @GetMapping("/{id}/hijas")
    @Operation(summary = "Sub-categorias de una categoria padre")
    public ResponseEntity<List<Categoria>> hijas(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.getHijas(id));
    }

    @PostMapping
    @Operation(summary = "[ADMIN] Crear categoria")
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.crear(categoria));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "[ADMIN] Actualizar categoria")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Categoria datos) {
        try {
            return ResponseEntity.ok(categoriaService.actualizar(id, datos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[ADMIN] Eliminar categoria")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Categoria eliminada"));
    }
}