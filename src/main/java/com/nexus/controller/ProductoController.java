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
import com.nexus.service.StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/producto")
@Tag(name = "Productos", description = "Gestión del mercado de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private StorageService storageService;

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

    // ✅ CORREGIDO: Publicar con imagen principal + galería
    @PostMapping(value = "/publicar/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar un nuevo producto con imagen principal y galería opcional")
    public ResponseEntity<Object> publicar(
            @RequestPart("producto") Producto producto,
            @RequestPart("imagenPrincipal") MultipartFile imagenPrincipal, // ✅ Obligatorio
            @RequestPart(value = "galeria", required = false) List<MultipartFile> galeria, // ✅ Opcional
            @PathVariable Integer usuarioId) {
        
        try {
            // 1. Subir imagen principal (OBLIGATORIA)
            String urlPrincipal = storageService.subirImagen(imagenPrincipal);
            if (urlPrincipal == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al subir imagen principal");
            }
            producto.setImagenPrincipal(urlPrincipal);
            
            // 2. Subir galería si existe (máximo 5 imágenes)
            if (galeria != null && !galeria.isEmpty()) {
                for (int i = 0; i < Math.min(galeria.size(), 5); i++) {
                    String urlGaleria = storageService.subirImagen(galeria.get(i));
                    if (urlGaleria != null) {
                        producto.addImagenGaleria(urlGaleria);
                    }
                }
            }
            
            // 3. Guardar producto en BD
            Producto nuevoProducto = productoService.publicar(producto, usuarioId);
            
            if (nuevoProducto != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Usuario no encontrado para vincular el producto");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // ✅ NUEVO: Actualizar producto con nuevas imágenes
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar producto (puede incluir nuevas imágenes)")
    public ResponseEntity<Object> update(
            @PathVariable Integer id,
            @RequestPart("producto") Producto productoDetalles,
            @RequestPart(value = "imagenPrincipal", required = false) MultipartFile imagenPrincipal,
            @RequestPart(value = "galeria", required = false) List<MultipartFile> galeria) {
        
        try {
            Optional<Producto> oProducto = productoService.findById(id);
            if (oProducto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
            }
            
            Producto producto = oProducto.get();
            
            // Actualizar campos básicos
            producto.setTitulo(productoDetalles.getTitulo());
            producto.setDescripcion(productoDetalles.getDescripcion());
            producto.setPrecio(productoDetalles.getPrecio());
            producto.setTipoOferta(productoDetalles.getTipoOferta());
            
            // Si hay nueva imagen principal, reemplazar
            if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {
                String urlNueva = storageService.subirImagen(imagenPrincipal);
                if (urlNueva != null) {
                    // Eliminar imagen anterior de Cloudinary
                    storageService.eliminarImagen(producto.getImagenPrincipal());
                    producto.setImagenPrincipal(urlNueva);
                }
            }
            
            // Si hay nueva galería, añadir (no reemplazar completamente)
            if (galeria != null && !galeria.isEmpty()) {
                for (MultipartFile file : galeria) {
                    if (producto.getGaleriaImagenes().size() < 5) {
                        String urlGaleria = storageService.subirImagen(file);
                        if (urlGaleria != null) {
                            producto.addImagenGaleria(urlGaleria);
                        }
                    }
                }
            }
            
            Producto actualizado = productoService.update(id, producto);
            return ResponseEntity.ok(actualizado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<Producto> oProducto = productoService.findById(id);
        
        if (oProducto.isPresent()) {
            Producto producto = oProducto.get();
            
            // Eliminar imágenes de Cloudinary
            storageService.eliminarImagen(producto.getImagenPrincipal());
            for (String url : producto.getGaleriaImagenes()) {
                storageService.eliminarImagen(url);
            }
            
            productoService.delete(id);
            return ResponseEntity.ok("Producto eliminado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }
}