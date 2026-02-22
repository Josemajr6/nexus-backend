package com.nexus.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para envío de emails.
 * Todos los envíos son @Async → no bloquean el hilo HTTP.
 *
 * Usos en la app:
 *  - Verificación de cuenta tras el registro
 *  - Olvidé mi contraseña
 *  - Código OTP de dos factores (2FA)
 *  - Confirmación de pedido y cambios de estado del envío
 *  - Notificación de nuevo mensaje (si el usuario tiene el ajuste activado)
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${nexus.mail.from:noreply@nexus-app.es}")
    private String from;

    @Async
    public void enviarEmailHtml(String destinatario, String asunto, String htmlBody) {
        if (mailSender == null) {
            System.out.println("📧 [EMAIL-DEV] Para: " + destinatario + " | Asunto: " + asunto);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("❌ Error enviando email a " + destinatario + ": " + e.getMessage());
        }
    }

    @Async
    public void enviarEmail(String destinatario, String asunto, String texto) {
        enviarEmailHtml(destinatario, asunto,
            "<div style='font-family:Arial,sans-serif;padding:24px'>" + texto + "</div>");
    }

    /** Email de verificación de cuenta tras registro */
    @Async
    public void enviarVerificacion(String destinatario, String username, String codigo) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:32px;background:#f9f9f9;border-radius:12px">
              <h2 style="color:#FF6B35">📧 Verifica tu cuenta en Nexus</h2>
              <p>Hola <strong>%s</strong>, gracias por registrarte.</p>
              <p>Introduce este código en la app para activar tu cuenta:</p>
              <div style="font-size:36px;font-weight:bold;letter-spacing:8px;text-align:center;padding:24px;background:#fff;border-radius:8px;border:2px solid #FF6B35;color:#FF6B35">%s</div>
              <p style="color:#888;font-size:13px">Este código expira en 30 minutos.</p>
            </div>
            """.formatted(username, codigo);
        enviarEmailHtml(destinatario, "Código de verificación — Nexus", html);
    }

    /** Email con código OTP para 2FA */
    @Async
    public void enviarOtp2FA(String destinatario, String username, String codigo) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:32px;background:#f9f9f9;border-radius:12px">
              <h2 style="color:#FF6B35">🔒 Verificación en dos pasos</h2>
              <p>Hola <strong>%s</strong>,</p>
              <p>Tu código de acceso único para Nexus es:</p>
              <div style="font-size:40px;font-weight:bold;letter-spacing:10px;text-align:center;padding:24px;background:#fff;border-radius:8px;border:2px solid #FF6B35;color:#FF6B35">%s</div>
              <p style="color:#888;font-size:13px">Este código expira en 10 minutos. No lo compartas con nadie.</p>
            </div>
            """.formatted(username, codigo);
        enviarEmailHtml(destinatario, "Código de acceso — Nexus", html);
    }

    /** Notificación de pedido enviado */
    @Async
    public void enviarNotificacionPedidoEnviado(String destinatario, String username,
                                                 String producto, String transportista,
                                                 String tracking, String urlTracking) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:32px;background:#f9f9f9;border-radius:12px">
              <h2 style="color:#FF6B35">🚚 ¡Tu pedido está en camino!</h2>
              <p>Hola <strong>%s</strong>,</p>
              <p>Tu pedido <strong>%s</strong> ha sido enviado.</p>
              <table style="width:100%%;border-collapse:collapse;margin:16px 0">
                <tr><td style="padding:8px;color:#888">Transportista</td><td style="padding:8px;font-weight:bold">%s</td></tr>
                <tr><td style="padding:8px;color:#888">Nº seguimiento</td><td style="padding:8px;font-weight:bold">%s</td></tr>
              </table>
              <a href="%s" style="display:inline-block;padding:14px 28px;background:#FF6B35;color:#fff;text-decoration:none;border-radius:8px;font-weight:bold">
                Seguir mi pedido
              </a>
            </div>
            """.formatted(username, producto, transportista, tracking, urlTracking);
        enviarEmailHtml(destinatario, "Tu pedido está en camino — Nexus", html);
    }
}