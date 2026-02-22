package com.nexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.entity.*;
import com.nexus.repository.ActorRepository;
import com.nexus.security.JWTUtils;
import com.nexus.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Ajustes de cuenta completos.
 *
 * ═══════════════════════════════════════════════════════════════════
 *  GUÍA ANGULAR — Secciones del panel /ajustes
 * ═══════════════════════════════════════════════════════════════════
 *
 *  1. Perfil         → PATCH /ajustes/perfil
 *  2. Avatar         → PATCH /ajustes/avatar (multipart)
 *  3. Contraseña     → PATCH /ajustes/password
 *  4. Email          → PATCH /ajustes/email (pide código de verificación)
 *  5. 2FA            → GET/POST /ajustes/2fa/... (ver TwoFactorService)
 *  6. Notificaciones → PATCH /ajustes/notificaciones
 *  7. Privacidad     → PATCH /ajustes/privacidad
 *  8. Dirección def. → PATCH /ajustes/direccion
 *  9. Vincular OAuth → POST /ajustes/vincular-google, /vincular-facebook
 * 10. Eliminar cuenta→ DELETE /ajustes/cuenta
 * ═══════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/ajustes")
@Tag(name = "Ajustes", description = "Panel de configuración completo de la cuenta")
public class AjustesController {

    @Autowired private JWTUtils             jwtUtils;
    @Autowired private ActorRepository      actorRepository;
    @Autowired private UsuarioService       usuarioService;
    @Autowired private PasswordEncoder      passwordEncoder;
    @Autowired private StorageService       storageService;
    @Autowired private TwoFactorService     twoFactorService;
    @Autowired private EmailService         emailService;
    @Autowired private PasswordResetService passwordResetService;

    // ── 1. PERFIL ──────────────────────────────────────────────────────────

    /**
     * Actualizar datos del perfil.
     * Body (todos opcionales):
     * { "username": "nuevo_user", "biografia": "...", "ubicacion": "Madrid", "telefono": "600..." }
     */
    @PatchMapping("/perfil")
    @Operation(summary = "Actualizar nombre de usuario, biografía, teléfono, ubicación")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> cambios) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        if (cambios.containsKey("username")) {
            String nu = cambios.get("username");
            if (actorRepository.findByUsername(nu).filter(a -> !a.getId().equals(actor.getId())).isPresent())
                return ResponseEntity.badRequest().body(Map.of("error", "Ese nombre de usuario ya está en uso"));
            actor.setUser(nu);
        }

        if (actor instanceof Usuario u) {
            if (cambios.containsKey("biografia"))  u.setBiografia(cambios.get("biografia"));
            if (cambios.containsKey("ubicacion"))  u.setUbicacion(cambios.get("ubicacion"));
            if (cambios.containsKey("telefono"))   u.setTelefono(cambios.get("telefono"));
        } else if (actor instanceof Empresa e) {
            if (cambios.containsKey("descripcion")) e.setDescripcion(cambios.get("descripcion"));
            if (cambios.containsKey("web"))         e.setWeb(cambios.get("web"));
            if (cambios.containsKey("telefono"))    e.setTelefono(cambios.get("telefono"));
        }

        actorRepository.save(actor);
        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado correctamente"));
    }

    // ── 2. AVATAR ──────────────────────────────────────────────────────────

    @PatchMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cambiar foto de perfil")
    public ResponseEntity<?> cambiarAvatar(@RequestPart("imagen") MultipartFile imagen) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        if (!(actor instanceof Usuario u)) return ResponseEntity.badRequest().body(Map.of("error", "Solo usuarios pueden cambiar avatar"));

        String nuevaUrl = storageService.subirImagen(imagen);
        if (nuevaUrl == null) return ResponseEntity.internalServerError().body(Map.of("error", "Error al subir imagen"));

        // Eliminar avatar anterior si existe y no es el default
        if (u.getAvatar() != null && u.getAvatar().contains("cloudinary")) {
            storageService.eliminarImagen(u.getAvatar());
        }

        u.setAvatar(nuevaUrl);
        actorRepository.save(u);
        return ResponseEntity.ok(Map.of("avatar", nuevaUrl, "mensaje", "Avatar actualizado"));
    }

    // ── 3. CONTRASEÑA ──────────────────────────────────────────────────────

    /**
     * Cambiar contraseña (usuario ya logueado, conoce la contraseña actual).
     * Body: { "passwordActual": "...", "passwordNueva": "...", "confirmar": "..." }
     */
    @PatchMapping("/password")
    @Operation(summary = "Cambiar contraseña (requiere la contraseña actual)")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        String actual   = body.get("passwordActual");
        String nueva    = body.get("passwordNueva");
        String confirmar= body.get("confirmar");

        if (!passwordEncoder.matches(actual, actor.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña actual no es correcta"));

        if (!nueva.equals(confirmar))
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));

        if (nueva.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 8 caracteres"));

        actor.setPassword(passwordEncoder.encode(nueva));
        actorRepository.save(actor);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }

    // ── 4. EMAIL ──────────────────────────────────────────────────────────

    /**
     * Cambiar email: envía un código de verificación al nuevo email.
     * Body: { "emailNuevo": "nuevo@mail.com" }
     */
    @PatchMapping("/email/solicitar")
    @Operation(summary = "Solicitar cambio de email (envía código al nuevo email)")
    public ResponseEntity<?> solicitarCambioEmail(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        String nuevoEmail = body.get("emailNuevo");
        if (actorRepository.findByEmail(nuevoEmail).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Ese email ya está en uso"));

        // Reutilizamos el PasswordResetService para enviar el código de verificación
        String codigo = usuarioService.generarCodigoVerificacion(actor.getId(), nuevoEmail);
        emailService.enviarVerificacion(nuevoEmail, actor.getUser(), codigo);

        return ResponseEntity.ok(Map.of("mensaje", "Código enviado a " + nuevoEmail));
    }

    /**
     * Confirmar cambio de email con el código recibido.
     * Body: { "emailNuevo": "nuevo@mail.com", "codigo": "123456" }
     */
    @PatchMapping("/email/confirmar")
    @Operation(summary = "Confirmar cambio de email con el código recibido")
    public ResponseEntity<?> confirmarCambioEmail(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        boolean ok = usuarioService.verificarCambioEmail(
            actor.getId(), body.get("emailNuevo"), body.get("codigo"));

        if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "Código incorrecto o expirado"));

        actor.setEmail(body.get("emailNuevo"));
        actorRepository.save(actor);
        return ResponseEntity.ok(Map.of("mensaje", "Email actualizado correctamente"));
    }

    // ── 5. DOS FACTORES (2FA) ──────────────────────────────────────────────

    @GetMapping("/2fa/estado")
    @Operation(summary = "Ver si el 2FA está activo y qué método usa")
    public ResponseEntity<?> estado2FA() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        return ResponseEntity.ok(Map.of(
            "activo",  actor.isTwoFactorEnabled(),
            "metodo",  actor.getTwoFactorMethod() != null ? actor.getTwoFactorMethod() : "ninguno"
        ));
    }

    /**
     * Paso 1 para activar Google Authenticator: genera el QR.
     * Angular muestra el QR → usuario lo escanea con Google Authenticator / Authy.
     * Respuesta: { "qrBase64": "...", "secret": "..." }
     * Angular: <img [src]="'data:image/png;base64,' + qrBase64">
     */
    @PostMapping("/2fa/totp/setup")
    @Operation(summary = "Generar QR para Google Authenticator")
    public ResponseEntity<?> setupTotp() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        try {
            TwoFactorService.SetupTotpResponse r =
                twoFactorService.configurarTotp(actor.getId(), actor.getEmail());
            return ResponseEntity.ok(Map.of("qrBase64", r.qrBase64(), "secret", r.secret()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Paso 2: confirmar que el QR fue escaneado correctamente.
     * Body: { "codigo": "123456" }
     */
    @PostMapping("/2fa/totp/activar")
    @Operation(summary = "Confirmar código TOTP y activar Google Authenticator")
    public ResponseEntity<?> activarTotp(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        boolean ok = twoFactorService.confirmarActivacionTotp(actor.getId(), body.get("codigo"));
        return ok
            ? ResponseEntity.ok(Map.of("mensaje", "Google Authenticator activado correctamente ✅"))
            : ResponseEntity.badRequest().body(Map.of("error", "Código incorrecto. Asegúrate de que la hora de tu teléfono es correcta."));
    }

    /**
     * Activar 2FA por email.
     */
    @PostMapping("/2fa/email/activar")
    @Operation(summary = "Activar verificación en dos pasos por email")
    public ResponseEntity<?> activarEmail2FA() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        actorRepository.findById(actor.getId()).ifPresent(a -> {
            a.setTwoFactorEnabled(true);
            a.setTwoFactorMethod("EMAIL");
            actorRepository.save(a);
        });
        return ResponseEntity.ok(Map.of("mensaje", "2FA por email activado. Se pedirá un código en cada inicio de sesión."));
    }

    @PostMapping("/2fa/desactivar")
    @Operation(summary = "Desactivar la verificación en dos pasos")
    public ResponseEntity<?> desactivar2FA(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        // Requiere contraseña para desactivar (seguridad)
        if (!passwordEncoder.matches(body.get("password"), actor.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña incorrecta"));

        twoFactorService.desactivar2FA(actor.getId());
        return ResponseEntity.ok(Map.of("mensaje", "Verificación en dos pasos desactivada"));
    }

    // ── 6. NOTIFICACIONES ──────────────────────────────────────────────────

    /**
     * Configurar qué notificaciones recibir.
     * Body (todos opcionales, boolean):
     * { "emailNuevoMensaje": true, "emailNuevaCompra": true,
     *   "emailMarketing": false, "pushNuevoMensaje": true }
     */
    @PatchMapping("/notificaciones")
    @Operation(summary = "Configurar preferencias de notificaciones")
    public ResponseEntity<?> notificaciones(@RequestBody Map<String, Boolean> prefs) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        ActorNotificacionConfig config = actor.getNotificacionConfig();
        if (config == null) config = new ActorNotificacionConfig();

        if (prefs.containsKey("emailNuevoMensaje"))  config.setEmailNuevoMensaje(prefs.get("emailNuevoMensaje"));
        if (prefs.containsKey("emailNuevaCompra"))   config.setEmailNuevaCompra(prefs.get("emailNuevaCompra"));
        if (prefs.containsKey("emailEstadoEnvio"))   config.setEmailEstadoEnvio(prefs.get("emailEstadoEnvio"));
        if (prefs.containsKey("emailMarketing"))     config.setEmailMarketing(prefs.get("emailMarketing"));
        if (prefs.containsKey("pushNuevoMensaje"))   config.setPushNuevoMensaje(prefs.get("pushNuevoMensaje"));
        if (prefs.containsKey("pushNuevaCompra"))    config.setPushNuevaCompra(prefs.get("pushNuevaCompra"));

        actor.setNotificacionConfig(config);
        actorRepository.save(actor);
        return ResponseEntity.ok(Map.of("mensaje", "Notificaciones actualizadas", "config", config));
    }

    // ── 7. PRIVACIDAD ──────────────────────────────────────────────────────

    /**
     * Body (todos opcionales):
     * { "perfilPublico": true, "mostrarTelefono": false, "mostrarUbicacion": true }
     */
    @PatchMapping("/privacidad")
    @Operation(summary = "Configurar privacidad del perfil")
    public ResponseEntity<?> privacidad(@RequestBody Map<String, Boolean> prefs) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        if (!(actor instanceof Usuario u)) return ResponseEntity.badRequest().body("Solo para usuarios");

        if (prefs.containsKey("perfilPublico"))     u.setPerfilPublico(prefs.get("perfilPublico"));
        if (prefs.containsKey("mostrarTelefono"))   u.setMostrarTelefono(prefs.get("mostrarTelefono"));
        if (prefs.containsKey("mostrarUbicacion"))  u.setMostrarUbicacion(prefs.get("mostrarUbicacion"));

        actorRepository.save(u);
        return ResponseEntity.ok(Map.of("mensaje", "Configuración de privacidad actualizada"));
    }

    // ── 8. DIRECCIÓN POR DEFECTO ───────────────────────────────────────────

    /**
     * Guardar la dirección de envío por defecto para agilizar compras.
     * Body: { "nombre": "...", "direccion": "...", "ciudad": "...", "cp": "...", "pais": "...", "telefono": "..." }
     */
    @PatchMapping("/direccion")
    @Operation(summary = "Guardar dirección de envío por defecto")
    public ResponseEntity<?> direccion(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        if (!(actor instanceof Usuario u)) return ResponseEntity.badRequest().body("Solo para usuarios");

        DireccionEnvio dir = u.getDireccionPorDefecto();
        if (dir == null) dir = new DireccionEnvio();

        if (body.containsKey("nombre"))    dir.setNombre(body.get("nombre"));
        if (body.containsKey("direccion")) dir.setDireccion(body.get("direccion"));
        if (body.containsKey("ciudad"))    dir.setCiudad(body.get("ciudad"));
        if (body.containsKey("cp"))        dir.setCodigoPostal(body.get("cp"));
        if (body.containsKey("pais"))      dir.setPais(body.get("pais"));
        if (body.containsKey("telefono"))  dir.setTelefono(body.get("telefono"));

        u.setDireccionPorDefecto(dir);
        actorRepository.save(u);
        return ResponseEntity.ok(Map.of("mensaje", "Dirección guardada", "direccion", dir));
    }

    @GetMapping("/direccion")
    @Operation(summary = "Obtener dirección de envío por defecto")
    public ResponseEntity<?> getDireccion() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        if (!(actor instanceof Usuario u)) return ResponseEntity.badRequest().body("Solo para usuarios");
        return u.getDireccionPorDefecto() != null
            ? ResponseEntity.ok(u.getDireccionPorDefecto())
            : ResponseEntity.noContent().build();
    }

    // ── 9. SESIONES ACTIVAS ────────────────────────────────────────────────

    @GetMapping("/sesiones")
    @Operation(summary = "Ver sesiones activas (dispositivos conectados)")
    public ResponseEntity<?> sesiones() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        // En una implementación completa esto consultaría una tabla de sesiones/JWT activos
        // Por ahora devuelve la sesión actual
        return ResponseEntity.ok(Map.of(
            "mensaje", "Para ver todas las sesiones activas se necesita almacenamiento de tokens en BD",
            "sugerencia", "Implementar TokenBlacklistRepository para invalidar sesiones individuales"
        ));
    }

    @PostMapping("/sesiones/cerrar-todas")
    @Operation(summary = "Cerrar sesión en todos los dispositivos")
    public ResponseEntity<?> cerrarTodo() {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();
        // Cambiar el campo jwtVersion del usuario → invalida todos los tokens anteriores
        actorRepository.findById(actor.getId()).ifPresent(a -> {
            a.setJwtVersion(a.getJwtVersion() + 1);
            actorRepository.save(a);
        });
        return ResponseEntity.ok(Map.of("mensaje", "Todas las sesiones han sido cerradas"));
    }

    // ── 10. ELIMINAR CUENTA ────────────────────────────────────────────────

    /**
     * Elimina la cuenta permanentemente (soft delete: marca como eliminada).
     * Body: { "password": "...", "confirmacion": "ELIMINAR" }
     */
    @DeleteMapping("/cuenta")
    @Operation(summary = "Eliminar cuenta permanentemente")
    public ResponseEntity<?> eliminarCuenta(@RequestBody Map<String, String> body) {
        Actor actor = jwtUtils.userLogin();
        if (actor == null) return noAutorizado();

        if (!passwordEncoder.matches(body.get("password"), actor.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña incorrecta"));

        if (!"ELIMINAR".equals(body.get("confirmacion")))
            return ResponseEntity.badRequest().body(Map.of("error", "Escribe ELIMINAR para confirmar"));

        // Soft delete: no borramos los datos para cumplir normativa (pedidos históricos, etc.)
        actor.setCuentaEliminada(true);
        actor.setEmail("deleted_" + actor.getId() + "@nexus.deleted"); // Liberar el email
        actorRepository.save(actor);

        return ResponseEntity.ok(Map.of("mensaje", "Cuenta eliminada. Los datos se borrarán en 30 días según nuestra política de privacidad."));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Sesión no válida. Por favor, inicia sesión de nuevo."));
    }
}