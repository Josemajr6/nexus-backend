package com.nexus.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Actor;
import com.nexus.entity.Comentario;
import com.nexus.entity.Oferta;
import com.nexus.repository.ActorRepository;
import com.nexus.repository.ComentarioRepository;
import com.nexus.repository.OfertaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/comentario")
@Tag(name = "Comentarios", description = "Opiniones en ofertas")
public class ComentarioController {

    @Autowired private ComentarioRepository comentarioRepository;
    @Autowired private OfertaRepository ofertaRepository;
    @Autowired private ActorRepository actorRepository;

    // --- VER COMENTARIOS DE UNA OFERTA ---
    @GetMapping("/oferta/{ofertaId}")
    @Operation(summary = "Ver comentarios de una oferta")
    public List<Comentario> porOferta(@PathVariable Integer ofertaId) {
        return comentarioRepository.findAll().stream()
                // BUG FIX: Integer.equals() en lugar de == para evitar fallo con IDs > 127
                .filter(c -> c.getOferta().getId() == ofertaId)
                .toList();
    }

    // --- PUBLICAR COMENTARIO ---
    @PostMapping
    @Operation(summary = "Publicar un comentario")
    public ResponseEntity<?> comentar(
            @RequestParam Integer ofertaId,
            @RequestParam Integer actorId,
            @RequestBody Map<String, String> body) {

        Optional<Oferta> oferta = ofertaRepository.findById(ofertaId);
        Optional<Actor> actor = actorRepository.findById(actorId);

        if (oferta.isEmpty() || actor.isEmpty()) {
            return ResponseEntity.badRequest().body("Oferta o Actor no encontrados");
        }

        String texto = body.get("texto");
        if (texto == null || texto.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El comentario no puede estar vacío");
        }

        Comentario comentario = new Comentario(texto, oferta.get(), actor.get());
        Comentario guardado = comentarioRepository.save(comentario);

        return ResponseEntity.ok(guardado);
    }

    // --- ACTUALIZAR COMENTARIO ---
    @PutMapping("/{id}")
    @Operation(summary = "Editar texto de un comentario")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return comentarioRepository.findById(id).map(comentario -> {
            String nuevoTexto = body.get("texto");
            if (nuevoTexto != null && !nuevoTexto.trim().isEmpty()) {
                comentario.setTexto(nuevoTexto);
                comentarioRepository.save(comentario);
                return ResponseEntity.ok(comentario);
            }
            return ResponseEntity.badRequest().body("El texto no puede estar vacío");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- BORRAR COMENTARIO ---
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comentario")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (comentarioRepository.existsById(id)) {
            comentarioRepository.deleteById(id);
            return ResponseEntity.ok("Comentario eliminado");
        }
        return ResponseEntity.notFound().build();
    }
}