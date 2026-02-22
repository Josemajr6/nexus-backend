package com.nexus.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.Actor;
import com.nexus.entity.PasswordResetToken;
import com.nexus.repository.ActorRepository;
import com.nexus.repository.PasswordResetTokenRepository;

/**
 * Flujo completo "Olvidé mi contraseña":
 *
 *  1. Angular → POST /auth/forgot-password  { "email": "user@mail.com" }
 *  2. Servidor envía email con link: https://nexus-app.es/reset-password?token=UUID
 *  3. Angular → POST /auth/reset-password   { "token": "UUID", "nuevaPassword": "..." }
 *
 * El token expira en 15 minutos (configurable en application.properties).
 * Se permite solicitar un nuevo token aunque ya exista uno → invalida el anterior.
 */
@Service
public class PasswordResetService {

    @Autowired private ActorRepository           actorRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private EmailService              emailService;
    @Autowired private PasswordEncoder           passwordEncoder;

    @Value("${nexus.password-reset.expiry-minutes:15}")
    private int expiryMinutes;

    @Value("${nexus.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Genera un token y envía el email. Si el email no existe, NO se lanza error
     * (evita enumerar usuarios válidos — buena práctica de seguridad).
     */
    @Transactional
    public void solicitarReset(String email) {
        Optional<Actor> actor = actorRepository.findByEmail(email);
        if (actor.isEmpty()) return; // Silencioso por seguridad

        // Invalidar tokens previos del usuario
        tokenRepository.deleteByActorId(actor.get().getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setActor(actor.get());
        prt.setExpiraEn(LocalDateTime.now().plusMinutes(expiryMinutes));
        tokenRepository.save(prt);

        String link = frontendUrl + "/reset-password?token=" + token;

        emailService.enviarEmailHtml(
            email,
            "Restablecer contraseña — Nexus",
            buildEmailHtml(actor.get().getUser(), link)
        );
    }

    /**
     * Valida el token y actualiza la contraseña.
     * @throws IllegalArgumentException si el token es inválido o expiró.
     */
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (prt.getExpiraEn().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(prt);
            throw new IllegalArgumentException("El enlace ha expirado. Solicita uno nuevo.");
        }

        Actor actor = prt.getActor();
        actor.setPassword(passwordEncoder.encode(nuevaPassword));
        actorRepository.save(actor);
        tokenRepository.delete(prt); // Usar una sola vez
    }

    // ── Email HTML bonito ────────────────────────────────────────────────────

    private String buildEmailHtml(String username, String link) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:32px;background:#f9f9f9;border-radius:12px">
              <h2 style="color:#FF6B35">🔐 Restablecer contraseña</h2>
              <p>Hola <strong>%s</strong>,</p>
              <p>Recibimos una solicitud para restablecer tu contraseña en Nexus.</p>
              <p>Haz clic en el botón para crear una nueva contraseña. Este enlace es válido durante <strong>15 minutos</strong>.</p>
              <a href="%s" style="display:inline-block;padding:14px 28px;background:#FF6B35;color:#fff;text-decoration:none;border-radius:8px;font-weight:bold;margin:16px 0">
                Restablecer contraseña
              </a>
              <p style="color:#888;font-size:13px">Si no solicitaste este cambio, ignora este correo. Tu contraseña no cambiará.</p>
              <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
              <p style="color:#aaa;font-size:12px">© Nexus App</p>
            </div>
            """.formatted(username, link);
    }
}