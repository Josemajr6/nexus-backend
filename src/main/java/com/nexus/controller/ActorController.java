package com.nexus.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Actor;
import com.nexus.entity.Usuario;
import com.nexus.security.JWTUtils;
import com.nexus.service.FacebookAuthService;
import com.nexus.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Login y registro")
public class ActorController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JWTUtils jwtUtils;
    @Autowired private UsuarioService usuarioService;
    @Autowired private FacebookAuthService facebookAuthService;

    // --- LOGIN NORMAL ---
    @PostMapping("/login")
    @Operation(summary = "Login con credenciales")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciales) {
        try {
            String user = credenciales.get("user");
            String password = credenciales.get("password");
            
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user, password);
            Authentication authentication = authenticationManager.authenticate(authInputToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String token = jwtUtils.generateToken(authentication);
            String rol = authentication.getAuthorities().iterator().next().getAuthority();
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("rol", rol);
            response.put("username", authentication.getName());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("mensaje", "Credenciales inválidas: " + e.getMessage()), 
                HttpStatus.UNAUTHORIZED
            );
        }
    }
    
    // --- LOGIN GOOGLE ---
    @PostMapping("/google")
    @Operation(summary = "Login con Google")
    public ResponseEntity<Map<String, Object>> loginGoogle(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("token");
            Actor actor = usuarioService.ingresarConGoogle(idToken);
            
            UserDetails userDetails = usuarioService.loadUserByUsername(actor.getUser());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            String token = jwtUtils.generateToken(auth);
            String rol = usuarioService.obtenerRol(actor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("rol", rol);
            response.put("username", actor.getUser());
            response.put("userId", actor.getId());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("mensaje", "Error validando Google: " + e.getMessage()), 
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // --- LOGIN FACEBOOK (NUEVO) ---
    @PostMapping("/facebook")
    @Operation(summary = "Login con Facebook")
    public ResponseEntity<Map<String, Object>> loginFacebook(@RequestBody Map<String, String> body) {
        try {
            String accessToken = body.get("token");
            
            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Token de Facebook requerido"));
            }
            
            Actor actor = facebookAuthService.loginConFacebook(accessToken);
            
            UserDetails userDetails = usuarioService.loadUserByUsername(actor.getUser());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            String token = jwtUtils.generateToken(auth);
            String rol = usuarioService.obtenerRol(actor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("rol", rol);
            response.put("username", actor.getUser());
            response.put("userId", actor.getId());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Token de Facebook inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error conectando con Facebook: " + e.getMessage()));
        }
    }
    
    // --- REGISTRO ---
    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.registrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensaje", "Usuario registrado. Revisa tu correo.", 
                    "email", nuevo.getEmail(),
                    "userId", nuevo.getId()
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Error en el registro: " + e.getMessage()));
        }
    }
    
    // --- VERIFICAR CÓDIGO ---
    @PostMapping("/verificar")
    @Operation(summary = "Verificar cuenta con código")
    public ResponseEntity<?> verificar(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String codigo = payload.get("codigo");
        
        boolean verificado = usuarioService.verificarCuenta(email, codigo);
        
        if (verificado) {
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta verificada correctamente"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Código incorrecto o expirado"));
        }
    }
}