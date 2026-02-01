package com.nexus.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.Producto;
import com.nexus.service.ProductoService;
import com.nexus.service.StorageService; // <--- Importante

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/producto")
@Tag(name = "Productos", description = "Gestión del mercado de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private StorageService storageService; // <--- NUEVO SERVICIO

    @GetMapping
    @Operation(summary = "Obtener todos los productos")
    public ResponseEntity<List<Producto>> findAll() {
        List<Producto> productos = productoService.findAll();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Obtener solo productos disponibles para compra")
    public ResponseEntity<List<Producto>> findDisponibles() {
        List<Producto> productos = productoService.findDisponibles();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por ID")
    public ResponseEntity<Producto> findById(@PathVariable Integer id) {
        Optional<Producto> oProducto = productoService.findById(id);
        
        if (oProducto.isPresent()) {
            return ResponseEntity.ok(oProducto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // --- MODIFICADO: PUBLICAR CON FOTO ---
    @PostMapping(value = "/publicar/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar un nuevo producto con foto (Multipart)")
    public ResponseEntity<Object> publicar(
            @RequestPart("producto") Producto producto, // Cambiado @RequestBody por @RequestPart para mezclar JSON y File
            @RequestPart(value = "file", required = false) MultipartFile file, // Archivo opcional
            @PathVariable Integer usuarioId) {
        
        // 1. Si hay foto, la subimos primero
        if (file != null && !file.isEmpty()) {
            String urlImagen = storageService.subirImagen(file);
            producto.setImagenUrl(urlImagen); // Asegúrate que Producto tiene setImagenUrl
        }

        // 2. Llamamos al servicio original que ya tenías
        Producto nuevoProducto = productoService.publicar(producto, usuarioId);
        
        if (nuevoProducto != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado para vincular el producto");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody Producto producto) {
        Producto productoActualizado = productoService.update(id, producto);
        
        if (productoActualizado != null) {
            return ResponseEntity.ok(productoActualizado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<Producto> oProducto = productoService.findById(id);
        
        if (oProducto.isPresent()) {
            productoService.delete(id);
            return ResponseEntity.ok("Producto eliminado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }
}