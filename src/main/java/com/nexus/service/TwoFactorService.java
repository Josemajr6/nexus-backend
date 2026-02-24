package com.nexus.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2FA — Google Authenticator (TOTP) + Email OTP.
 *
 * FIX v4:
 *  - Inyecta ActorService (en lugar de llamar al mismo bean circularmente)
 *  - Los tempSecrets y emailOtps son ConcurrentHashMap (no hay Redis necesario)
 *
 * Angular flujo TOTP:
 *  1. POST /ajustes/2fa/totp/setup → { qrBase64, secret }
 *  2. Mostrar: <img [src]="'data:image/png;base64,' + qrBase64">
 *  3. Usuario escanea con Google Authenticator
 *  4. POST /ajustes/2fa/totp/activar { "codigo": "123456" } → activa
 *
 * Angular flujo EMAIL OTP:
 *  1. POST /ajustes/2fa/email/activar → envía código al email
 *  2. En cada login: POST /auth/2fa/verificar { "usuarioId", "codigo" }
 */
@Service
public class TwoFactorService {

    @Autowired private ActorService  actorService;
    @Autowired private EmailService  emailService;

    @Value("${totp.issuer:Nexus App}")
    private String issuer;

    // Secret TOTP temporal durante el setup (antes de confirmar con el primer código)
    private final ConcurrentHashMap<Integer, String> tempSecrets = new ConcurrentHashMap<>();

    // OTPs de email temporales: actorId → {codigo, expiraMs}
    private final ConcurrentHashMap<Integer, long[]> emailOtpExpiry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> emailOtpCodigo = new ConcurrentHashMap<>();

    // ── TOTP ──────────────────────────────────────────────────────────────

    public SetupTotpResponse configurarTotp(Integer usuarioId, String email) throws Exception {
        String secret = new DefaultSecretGenerator().generate();
        tempSecrets.put(usuarioId, secret);

        QrData qrData = new QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer(issuer)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();

        byte[] qrBytes = new ZxingPngQrGenerator().generate(qrData);
        return new SetupTotpResponse(secret, Base64.getEncoder().encodeToString(qrBytes));
    }

    /** Confirma que el usuario escaneó el QR correctamente y activa el 2FA TOTP. */
    public boolean confirmarActivacionTotp(Integer usuarioId, String codigoUsuario) {
        String secret = tempSecrets.get(usuarioId);
        if (secret == null) throw new IllegalStateException("No hay configuración TOTP pendiente. Vuelve a escanear el QR.");
        boolean valido = verificarCodigoTotp(secret, codigoUsuario);
        if (valido) {
            actorService.activar2FA(usuarioId, "TOTP", secret);
            tempSecrets.remove(usuarioId);
        }
        return valido;
    }

    /** Verifica un código TOTP de 6 dígitos contra el secret almacenado en BD. */
    public boolean verificarLoginTotp(Integer usuarioId, String codigo) {
        String secret = actorService.getTotpSecret(usuarioId);
        if (secret == null) throw new IllegalStateException("2FA TOTP no configurado");
        return verificarCodigoTotp(secret, codigo);
    }

    public boolean verificarCodigoTotp(String secret, String codigo) {
        return new DefaultCodeVerifier(
            new DefaultCodeGenerator(), new SystemTimeProvider()
        ).isValidCode(secret, codigo);
    }

    // ── Email OTP ─────────────────────────────────────────────────────────

    public void enviarOtpEmail(Integer usuarioId, String email, String username) {
        String codigo = String.format("%06d", new SecureRandom().nextInt(999999));
        long expira   = System.currentTimeMillis() + 10 * 60 * 1000L; // 10 min
        emailOtpCodigo.put(usuarioId, codigo);
        emailOtpExpiry.put(usuarioId, new long[]{expira});
        emailService.enviarOtp2FA(email, username, codigo);
    }

    public boolean verificarOtpEmail(Integer usuarioId, String codigo) {
        String stored = emailOtpCodigo.get(usuarioId);
        long[] expiry = emailOtpExpiry.get(usuarioId);
        if (stored == null || expiry == null) return false;
        if (System.currentTimeMillis() > expiry[0]) {
            emailOtpCodigo.remove(usuarioId);
            emailOtpExpiry.remove(usuarioId);
            return false;
        }
        boolean ok = stored.equals(codigo);
        if (ok) {
            emailOtpCodigo.remove(usuarioId);
            emailOtpExpiry.remove(usuarioId);
        }
        return ok;
    }

    public void desactivar2FA(Integer usuarioId) {
        actorService.desactivar2FA(usuarioId);
        tempSecrets.remove(usuarioId);
        emailOtpCodigo.remove(usuarioId);
        emailOtpExpiry.remove(usuarioId);
    }

    public record SetupTotpResponse(String secret, String qrBase64) {}
}