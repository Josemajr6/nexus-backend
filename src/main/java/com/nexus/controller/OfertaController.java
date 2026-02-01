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
@Tag(name = "Ofertas", description = "Gestión de chollos y promociones (Usuarios y Empresas)")
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
    
    // --- CREAR OFERTA ---
    @PostMapping(value = "/{actorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publicar una oferta")
    public ResponseEntity<?> crearOferta(
            @PathVariable Integer actorId,
            @RequestPart("oferta") Oferta oferta,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        Optional<Actor> actor = actorRepository.findById(actorId);
        if (actor.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Actor no encontrado");
        
        oferta.setActor(actor.get());
        
        if (file != null && !file.isEmpty()) {
            String url = storageService.subirImagen(file);
            oferta.setUrlOferta(url);
        }
        
        return new ResponseEntity<>(ofertaRepository.save(oferta), HttpStatus.CREATED);
    }
    
    // --- ACTUALIZAR OFERTA ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar oferta")
    public ResponseEntity<?> actualizarOferta(
            @PathVariable Integer id,
            @RequestPart("oferta") Oferta nuevosDatos,
            @RequestPart(value = "file", required = false) MultipartFile file) {
            
        return ofertaRepository.findById(id).map(oferta -> {
            // Actualizamos campos nuevos y viejos
            if (nuevosDatos.getTitulo() != null) oferta.setTitulo(nuevosDatos.getTitulo());
            if (nuevosDatos.getDescripcion() != null) oferta.setDescripcion(nuevosDatos.getDescripcion());
            if (nuevosDatos.getTienda() != null) oferta.setTienda(nuevosDatos.getTienda());
            if (nuevosDatos.getPrecioOriginal() > 0) oferta.setPrecioOriginal(nuevosDatos.getPrecioOriginal());
            if (nuevosDatos.getPrecioOferta() > 0) oferta.setPrecioOferta(nuevosDatos.getPrecioOferta());
            if (nuevosDatos.getFechaExpiracion() != null) oferta.setFechaExpiracion(nuevosDatos.getFechaExpiracion());
            
            if (file != null && !file.isEmpty()) {
                String url = storageService.subirImagen(file);
                oferta.setUrlOferta(url);
            }
            
            return ResponseEntity.ok(ofertaRepository.save(oferta));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar oferta")
    public ResponseEntity<?> borrar(@PathVariable Integer id) {
        if (ofertaRepository.existsById(id)) {
            ofertaRepository.deleteById(id);
            return ResponseEntity.ok("Oferta eliminada correctamente");
        }
        return ResponseEntity.notFound().build();
    }
}