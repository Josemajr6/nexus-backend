package com.nexus.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.Usuario;
import com.nexus.service.StorageService;
import com.nexus.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private StorageService storageService; // <--- NUEVO SERVICIO
    
    // Obtener todos los usuarios
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.findAll();
    }
    
    // Obtener usuario por id
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Integer id) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        
        if (usuarioOptional.isPresent()) {
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Crear usuario (Registro manual administrativo)
    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.save(usuario);
        return ResponseEntity.ok(nuevoUsuario);
    }
    
    // --- NUEVO ENDPOINT: SUBIR AVATAR ---
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir foto de perfil (Avatar)")
    public ResponseEntity<?> subirAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            
            // Subir a Cloudinary
            String url = storageService.subirImagen(file);
            
            if (url == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Fallo al subir la imagen. Revisa configuración de Cloudinary."));
            }
            
            // Guardar URL en BD
            usuario.setFotoPerfil(url); // Asegúrate de que en Usuario.java el campo se llame 'fotoPerfil'
            usuarioService.save(usuario);
            
            return ResponseEntity.ok(Map.of("mensaje", "Avatar actualizado", "url", url));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Actualizar usuario
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Integer id, @RequestBody Usuario usuarioDetalles) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        
        if (usuarioOptional.isPresent()) {
            Usuario usuarioExistente = usuarioOptional.get();
            usuarioExistente.setUser(usuarioDetalles.getUser());
            usuarioExistente.setEmail(usuarioDetalles.getEmail());
            // Puedes añadir más campos aquí si es necesario
            
            Usuario usuarioActualizado = usuarioService.save(usuarioExistente);
            return ResponseEntity.ok(usuarioActualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Eliminar usuario
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario por id")
    public ResponseEntity<String> deleteUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);
        
        if (usuarioOptional.isPresent()) {
            usuarioService.delete(id);
            return ResponseEntity.ok("Usuario eliminado correctamente...");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se ha encontrado el usuario para eliminar");
        }
    }
}