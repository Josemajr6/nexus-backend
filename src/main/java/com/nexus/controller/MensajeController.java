package com.nexus.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Mensaje;
import com.nexus.service.MensajeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/mensaje")
@Tag(name = "Mensajes", description = "Gestión de mensajes y chats de productos")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @GetMapping
    @Operation(summary = "Obtener todos los mensajes")
    public ResponseEntity<List<Mensaje>> findAll() {
        return ResponseEntity.ok(mensajeService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mensaje por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado")
    })
    public ResponseEntity<Mensaje> findById(@PathVariable Integer id) {
        Optional<Mensaje> oMensaje = mensajeService.findById(id);
        return oMensaje.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener chat de un producto")
    public ResponseEntity<List<Mensaje>> findByProducto(@PathVariable Integer productoId) {
        List<Mensaje> mensajes = mensajeService.findByProductoId(productoId);
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping("/enviar")
    @Operation(summary = "Enviar un mensaje")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mensaje enviado"),
            @ApiResponse(responseCode = "400", description = "Usuario o Producto no encontrados")
    })
    public ResponseEntity<Object> save(@RequestBody Mensaje mensaje, 
                                       @RequestParam Integer usuarioId, 
                                       @RequestParam Integer productoId) {
        
        Mensaje nuevoMensaje = mensajeService.save(mensaje, usuarioId, productoId);
        
        if (nuevoMensaje != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMensaje);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario o Producto no válidos");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mensaje")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<Mensaje> oMensaje = mensajeService.findById(id);
        
        if (oMensaje.isPresent()) {
            mensajeService.delete(id);
            return ResponseEntity.ok("Mensaje eliminado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mensaje no encontrado");
        }
    }
}