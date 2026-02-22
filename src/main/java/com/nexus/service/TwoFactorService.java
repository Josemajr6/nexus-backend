package com.nexus.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.exceptions.QrGenerationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de Verificación en Dos Pasos (2FA).
 *
 * Soporta DOS métodos (el usuario elige en ajustes):
 *
 * ── Método 1: TOTP (Google Authenticator / Authy) ──────────────────────
 *   - 100% gratuito, sin límites, funciona offline
 *   - Librería: dev.samstevens.totp (open source)
 *   - Flujo:
 *       1. POST /ajustes/2fa/totp/setup → devuelve QR en base64
 *       2. Usuario escanea QR con Google Authenticator
 *       3. POST /ajustes/2fa/totp/verificar { "codigo": "123456" } → activa
 *       4. En cada login: si 2FA activo → Angular pide código de 6 dígitos
 *
 * ── Método 2: Email OTP ────────────────────────────────────────────────
 *   - Envía un código de 6 dígitos al email del usuario
 *   - Sin apps necesarias, más fácil para usuarios no técnicos
 *   - Expira en 10 minutos
 *
 * NOTA SOBRE REDIS: Se usa para almacenar el secret TOTP temporal durante
 * el setup. Si no tienes Redis, hay un fallback con Map en memoria (válido
 * para desarrollo). En producción añadir spring-boot-starter-data-redis.
 */
@Service
public class TwoFactorService {

    @Autowired private ActorService  actorService;
    @Autowired private EmailService  emailService;

    @Value("${totp.issuer:Nexus App}")
    private String issuer;

    // Map en memoria como fallback si no hay Redis (solo para dev)
    private final java.util.concurrent.ConcurrentHashMap<Integer, String> tempSecrets
        = new java.util.concurrent.ConcurrentHashMap<>();

    private final java.util.concurrent.ConcurrentHashMap<Integer, OtpEntry> emailOtps
        = new java.util.concurrent.ConcurrentHashMap<>();

    // ── TOTP (Google Authenticator) ───────────────────────────────────────

    /**
     * Genera un nuevo secret TOTP y devuelve el QR en base64 para mostrar en Angular.
     * Angular debe mostrar: <img [src]="'data:image/png;base64,' + qrBase64">
     */
    public SetupTotpResponse configurarTotp(Integer usuarioId, String email) throws Exception {
        String secret = new DefaultSecretGenerator().generate();

        // Guardar temporalmente hasta que el usuario confirme con un código
        tempSecrets.put(usuarioId, secret);

        QrData qrData = new QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer(issuer)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(qrData);
        String qrBase64 = Base64.getEncoder().encodeToString(imageData);

        return new SetupTotpResponse(secret, qrBase64);
    }

    /**
     * El usuario introduce el primer código del authenticator para confirmar que funcionó.
     * Si es correcto, guarda el secret en la BD del usuario y activa el 2FA.
     */
    public boolean confirmarActivacionTotp(Integer usuarioId, String codigoUsuario) {
        String secret = tempSecrets.get(usuarioId);
        if (secret == null) throw new IllegalStateException("No hay configuración TOTP pendiente");

        boolean valido = verificarCodigoTotp(secret, codigoUsuario);
        if (valido) {
            actorService.activar2FA(usuarioId, "TOTP", secret);
            tempSecrets.remove(usuarioId);
        }
        return valido;
    }

    /**
     * Verifica el código TOTP durante el login.
     * Admite ventana de ±1 período (30s) para compensar desfases de reloj.
     */
    public boolean verificarCodigoTotp(String secret, String codigo) {
        CodeVerifier verifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(),
            new SystemTimeProvider()
        );
        return verifier.isValidCode(secret, codigo);
    }

    /**
     * Verifica el código TOTP usando el secret almacenado en la BD del usuario.
     */
    public boolean verificarLoginTotp(Integer usuarioId, String codigo) {
        String secret = actorService.getTotpSecret(usuarioId);
        if (secret == null) throw new IllegalStateException("2FA TOTP no está configurado");
        return verificarCodigoTotp(secret, codigo);
    }

    // ── Email OTP ─────────────────────────────────────────────────────────

    /**
     * Genera y envía un código OTP de 6 dígitos al email del usuario.
     * Llamado automáticamente tras el login si el usuario tiene 2FA por email activo.
     */
    public void enviarOtpEmail(Integer usuarioId, String email, String username) {
        String codigo = String.format("%06d", new SecureRandom().nextInt(999999));
        long expira   = System.currentTimeMillis() + (10 * 60 * 1000L); // 10 minutos

        emailOtps.put(usuarioId, new OtpEntry(codigo, expira));
        emailService.enviarOtp2FA(email, username, codigo);
    }

    /**
     * Verifica el OTP enviado por email.
     */
    public boolean verificarOtpEmail(Integer usuarioId, String codigo) {
        OtpEntry entry = emailOtps.get(usuarioId);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expira()) {
            emailOtps.remove(usuarioId);
            return false;
        }
        boolean ok = entry.codigo().equals(codigo);
        if (ok) emailOtps.remove(usuarioId); // Un solo uso
        return ok;
    }

    public void desactivar2FA(Integer usuarioId) {
        actorService.desactivar2FA(usuarioId);
        tempSecrets.remove(usuarioId);
        emailOtps.remove(usuarioId);
    }

    // ── DTOs internos ─────────────────────────────────────────────────────

    public record SetupTotpResponse(String secret, String qrBase64) {}
    private record OtpEntry(String codigo, long expira) {}
}