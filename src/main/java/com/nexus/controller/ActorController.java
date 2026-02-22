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

import com.nexus.entity.*;
import com.nexus.repository.ActorRepository;
import com.nexus.security.JWTUtils;
import com.nexus.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación")
public class ActorController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JWTUtils              jwtUtils;
    @Autowired private UsuarioService        usuarioService;
    @Autowired private FacebookAuthService   facebookAuthService;
    @Autowired private ActorRepository       actorRepository;
    @Autowired private PasswordResetService  passwordResetService;
    @Autowired private TwoFactorService      twoFactorService;

    // ── LOGIN ─────────────────────────────────────────────────────────────

    /**
     * Login con email+password.
     *
     * Si el usuario tiene 2FA activo, el servidor devuelve:
     *   { "requiere2FA": true, "metodo2FA": "TOTP"|"EMAIL", "usuarioId": 5 }
     *
     * Angular redirige a la pantalla de 2FA y llama a POST /auth/2fa/verificar
     * con el código recibido para obtener el JWT real.
     */
    @PostMapping("/login")
    @Operation(summary = "Login. Si tiene 2FA, devuelve requiere2FA=true en lugar del JWT")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> creds) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(creds.get("user"), creds.get("password")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            Actor actor = actorRepository.findByUsername(auth.getName()).orElse(null);

            // ── 2FA activo: no devolvemos el JWT todavía ──
            if (actor != null && actor.isTwoFactorEnabled()) {
                if ("EMAIL".equals(actor.getTwoFactorMethod())) {
                    twoFactorService.enviarOtpEmail(actor.getId(), actor.getEmail(), actor.getUser());
                }
                return ResponseEntity.ok(Map.of(
                    "requiere2FA",  true,
                    "metodo2FA",    actor.getTwoFactorMethod(),
                    "usuarioId",    actor.getId()
                ));
            }

            // ── Sin 2FA: JWT directo ──
            String token = jwtUtils.generateToken(auth);
            return ResponseEntity.ok(buildLoginResponse(token, auth, actor));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Credenciales inválidas"));
        }
    }

    /**
     * Verificar el código 2FA y obtener el JWT.
     * Body: { "usuarioId": 5, "codigo": "123456" }
     */
    @PostMapping("/2fa/verificar")
    @Operation(summary = "Verificar código 2FA (TOTP o email OTP) y obtener JWT")
    public ResponseEntity<Map<String, Object>> verificar2FA(@RequestBody Map<String, Object> body) {
        Integer usuarioId = Integer.valueOf(body.get("usuarioId").toString());
        String  codigo    = (String) body.get("codigo");

        Actor actor = actorRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean valido = "TOTP".equals(actor.getTwoFactorMethod())
            ? twoFactorService.verificarLoginTotp(usuarioId, codigo)
            : twoFactorService.verificarOtpEmail(usuarioId, codigo);

        if (!valido) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Código incorrecto o expirado"));

        UserDetails ud = usuarioService.loadUserByUsername(actor.getUser());
        Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String token = jwtUtils.generateToken(auth);

        return ResponseEntity.ok(buildLoginResponse(token, auth, actor));
    }

    // ── OLVIDÉ MI CONTRASEÑA ──────────────────────────────────────────────

    /**
     * Solicitar enlace de recuperación.
     * Body: { "email": "usuario@mail.com" }
     * Siempre devuelve 200 (no revela si el email existe).
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar email para restablecer contraseña")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        passwordResetService.solicitarReset(body.get("email"));
        return ResponseEntity.ok(Map.of("mensaje",
            "Si ese email está registrado, recibirás un enlace en breve."));
    }

    /**
     * Restablecer contraseña con el token del email.
     * Body: { "token": "UUID...", "nuevaPassword": "nueva123" }
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con el token del email")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetearPassword(body.get("token"), body.get("nuevaPassword"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente. Ya puedes iniciar sesión."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── OAUTH ─────────────────────────────────────────────────────────────

    @PostMapping("/google")
    @Operation(summary = "Login con Google")
    public ResponseEntity<Map<String, Object>> loginGoogle(@RequestBody Map<String, String> body) {
        try {
            Actor actor = usuarioService.ingresarConGoogle(body.get("token"));
            return buildOAuthResponse(actor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Error validando Google: " + e.getMessage()));
        }
    }

    @PostMapping("/facebook")
    @Operation(summary = "Login con Facebook")
    public ResponseEntity<Map<String, Object>> loginFacebook(@RequestBody Map<String, String> body) {
        try {
            Actor actor = facebookAuthService.loginConFacebook(body.get("token"));
            return buildOAuthResponse(actor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", "Error con Facebook: " + e.getMessage()));
        }
    }

    // ── REGISTRO ──────────────────────────────────────────────────────────

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.registrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "✅ Cuenta creada. Revisa tu correo para verificarla.",
                "email",   nuevo.getEmail(),
                "userId",  nuevo.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Error en el registro: " + e.getMessage()));
        }
    }

    @PostMapping("/verificar")
    @Operation(summary = "Verificar cuenta con el código del email")
    public ResponseEntity<?> verificar(@RequestBody Map<String, String> payload) {
        boolean ok = usuarioService.verificarCuenta(payload.get("email"), payload.get("codigo"));
        return ok
            ? ResponseEntity.ok(Map.of("mensaje", "Cuenta verificada correctamente"))
            : ResponseEntity.badRequest().body(Map.of("mensaje", "Código incorrecto o expirado"));
    }

    // ── PERFIL PROPIO (/me) ───────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado (requiere JWT)")
    public ResponseEntity<?> me() {
        try {
            Actor actor = jwtUtils.userLogin();
            if (actor == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión expirada"));

            Map<String, Object> perfil = new HashMap<>();
            perfil.put("id",             actor.getId());
            perfil.put("username",       actor.getUser());
            perfil.put("email",          actor.getEmail());
            perfil.put("rol",            usuarioService.obtenerRol(actor));
            perfil.put("fechaRegistro",  actor.getFechaRegistro());
            perfil.put("twoFactorActivo",actor.isTwoFactorEnabled());
            perfil.put("metodo2FA",      actor.getTwoFactorMethod());

            if (actor instanceof Usuario u) {
                perfil.put("avatar",          u.getAvatar());
                perfil.put("telefono",        u.getTelefono());
                perfil.put("biografia",       u.getBiografia());
                perfil.put("ubicacion",       u.getUbicacion());
                perfil.put("reputacion",      u.getReputacion());
                perfil.put("esVerificado",    u.isEsVerificado());
                perfil.put("perfilPublico",   u.isPerfilPublico());
                perfil.put("direccionDefecto",u.getDireccionPorDefecto());
                perfil.put("notificaciones",  u.getNotificacionConfig());
            } else if (actor instanceof Empresa e) {
                perfil.put("cif",         e.getCif());
                perfil.put("descripcion", e.getDescripcion());
                perfil.put("web",         e.getWeb());
            } else if (actor instanceof Admin a) {
                perfil.put("nivelAcceso", a.getNivelAcceso());
            }

            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Map<String, Object> buildLoginResponse(String token, Authentication auth, Actor actor) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("token",    token);
        resp.put("rol",      auth.getAuthorities().iterator().next().getAuthority());
        resp.put("username", auth.getName());
        resp.put("userId",   actor != null ? actor.getId() : null);
        resp.put("requiere2FA", false);
        return resp;
    }

    private ResponseEntity<Map<String, Object>> buildOAuthResponse(Actor actor) {
        UserDetails ud = usuarioService.loadUserByUsername(actor.getUser());
        Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.ok(buildLoginResponse(jwtUtils.generateToken(auth), auth, actor));
    }
}