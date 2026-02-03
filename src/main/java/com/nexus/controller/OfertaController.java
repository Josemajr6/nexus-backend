package com.nexus.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.Actor;
import com.nexus.entity.Oferta;
import com.nexus.repository.ActorRepository;
import com.nexus.repository.OfertaRepository;
import com.nexus.service.StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/oferta")
@Tag(name = "Ofertas", description = "Gestión de chollos y promociones")
public class OfertaController {

    @Autowired private OfertaRepository ofertaRepository;
    @Autowired private ActorRepository actorRepository;
    @Autowired private StorageService storageService;

    @GetMapping
    @Operation(summary = "Listar todas las ofertas")
    public List<Oferta> listar() { 
        return ofertaRepository.findAll(); 
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Ver detalle de una oferta")
    public ResponseEntity<Oferta> verOferta(@PathVariable Integer id) {
        return ofertaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ✅ CORREGIDO: Crear oferta con imagen principal + galería
    @PostMapping(value = "/{actorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar una oferta con imagen principal y galería opcional")
    public ResponseEntity<?> crearOferta(
            @PathVariable Integer actorId,
            @RequestPart("oferta") Oferta oferta,
            @RequestPart("imagenPrincipal") MultipartFile imagenPrincipal, // ✅ Obligatorio
            @RequestPart(value = "galeria", required = false) List<MultipartFile> galeria) {
        
        try {
            Optional<Actor> actor = actorRepository.findById(actorId);
            if (actor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Actor no encontrado");
            }
            
            oferta.setActor(actor.get());
            
            // 1. Subir imagen principal (OBLIGATORIA)
            String urlPrincipal = storageService.subirImagen(imagenPrincipal);
            if (urlPrincipal == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al subir imagen principal");
            }
            oferta.setImagenPrincipal(urlPrincipal);
            
            // 2. Subir galería si existe (máximo 4 imágenes)
            if (galeria != null && !galeria.isEmpty()) {
                for (int i = 0; i < Math.min(galeria.size(), 4); i++) {
                    String urlGaleria = storageService.subirImagen(galeria.get(i));
                    if (urlGaleria != null) {
                        oferta.addImagenGaleria(urlGaleria);
                    }
                }
            }
            
            return new ResponseEntity<>(ofertaRepository.save(oferta), HttpStatus.CREATED);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // ✅ CORREGIDO: Actualizar oferta con nuevas imágenes
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar oferta (puede incluir nuevas imágenes)")
    public ResponseEntity<?> actualizarOferta(
            @PathVariable Integer id,
            @RequestPart("oferta") Oferta nuevosDatos,
            @RequestPart(value = "imagenPrincipal", required = false) MultipartFile imagenPrincipal,
            @RequestPart(value = "galeria", required = false) List<MultipartFile> galeria) {
            
        try {
            return ofertaRepository.findById(id).map(oferta -> {
                // Actualizar campos básicos
                if (nuevosDatos.getTitulo() != null) oferta.setTitulo(nuevosDatos.getTitulo());
                if (nuevosDatos.getDescripcion() != null) oferta.setDescripcion(nuevosDatos.getDescripcion());
                if (nuevosDatos.getTienda() != null) oferta.setTienda(nuevosDatos.getTienda());
                if (nuevosDatos.getPrecioOriginal() > 0) oferta.setPrecioOriginal(nuevosDatos.getPrecioOriginal());
                if (nuevosDatos.getPrecioOferta() > 0) oferta.setPrecioOferta(nuevosDatos.getPrecioOferta());
                if (nuevosDatos.getFechaExpiracion() != null) oferta.setFechaExpiracion(nuevosDatos.getFechaExpiracion());
                
                // Si hay nueva imagen principal, reemplazar
                if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {
                    String urlNueva = storageService.subirImagen(imagenPrincipal);
                    if (urlNueva != null) {
                        storageService.eliminarImagen(oferta.getImagenPrincipal());
                        oferta.setImagenPrincipal(urlNueva);
                    }
                }
                
                // Si hay nueva galería, añadir
                if (galeria != null && !galeria.isEmpty()) {
                    for (MultipartFile file : galeria) {
                        if (oferta.getGaleriaImagenes().size() < 4) {
                            String urlGaleria = storageService.subirImagen(file);
                            if (urlGaleria != null) {
                                oferta.addImagenGaleria(urlGaleria);
                            }
                        }
                    }
                }
                
                return ResponseEntity.ok(ofertaRepository.save(oferta));
            }).orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar oferta")
    public ResponseEntity<?> borrar(@PathVariable Integer id) {
        if (ofertaRepository.existsById(id)) {
            Oferta oferta = ofertaRepository.findById(id).get();
            
            // Eliminar imágenes de Cloudinary
            storageService.eliminarImagen(oferta.getImagenPrincipal());
            for (String url : oferta.getGaleriaImagenes()) {
                storageService.eliminarImagen(url);
            }
            
            ofertaRepository.deleteById(id);
            return ResponseEntity.ok("Oferta eliminada correctamente");
        }
        return ResponseEntity.notFound().build();
    }
}