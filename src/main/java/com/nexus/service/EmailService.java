package com.nexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Servicio de email.
 * Todos los metodos son @Async para no bloquear el hilo HTTP.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${nexus.mail.from:somosnexusapp@gmail.com}")
    private String fromEmail;

    @Value("${nexus.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ---- Email de texto plano ------------------------------------------

    @Async
    public void enviarEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Error enviando email a " + to + ": " + e.getMessage());
        }
    }

    // ---- Email HTML -----------------------------------------------------

    @Async
    public void enviarEmailHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Error enviando HTML email a " + to + ": " + e.getMessage());
        }
    }

    // ---- Emails especificos de la app ----------------------------------

    @Async
    public void enviarVerificacion(String to, String username, String codigo) {
        String html = "<html><body style='font-family:sans-serif;max-width:600px;margin:auto'>"
            + "<h2>Verifica tu cuenta en Nexus</h2>"
            + "<p>Hola <b>" + username + "</b>,</p>"
            + "<p>Tu codigo de verificacion es:</p>"
            + "<div style='font-size:36px;font-weight:bold;letter-spacing:12px;"
            + "background:#F5F5F5;padding:16px;text-align:center;border-radius:8px'>"
            + codigo + "</div>"
            + "<p>Este codigo expira en 30 minutos.</p>"
            + "<p>Si no has creado una cuenta en Nexus, ignora este email.</p>"
            + "</body></html>";
        enviarEmailHtml(to, "Verifica tu cuenta en Nexus", html);
    }

    @Async
    public void enviarResetPassword(String to, String token) {
        String link = frontendUrl + "/auth/reset-password?token=" + token;
        String html = "<html><body style='font-family:sans-serif;max-width:600px;margin:auto'>"
            + "<h2>Restablecer contrasena - Nexus</h2>"
            + "<p>Haz clic en el siguiente enlace para restablecer tu contrasena:</p>"
            + "<a href='" + link + "' style='background:#FF5722;color:#fff;padding:12px 24px;"
            + "border-radius:6px;text-decoration:none;display:inline-block;margin:16px 0'>"
            + "Restablecer contrasena</a>"
            + "<p style='color:#666;font-size:12px'>Este enlace expira en 15 minutos. "
            + "Si no solicitaste este cambio, ignora este email.</p>"
            + "</body></html>";
        enviarEmailHtml(to, "Restablecer contrasena - Nexus", html);
    }

    @Async
    public void enviarOtpDosFactores(String to, String otp) {
        String html = "<html><body style='font-family:sans-serif;max-width:600px;margin:auto'>"
            + "<h2>Codigo de verificacion - Nexus</h2>"
            + "<p>Tu codigo de verificacion de dos factores es:</p>"
            + "<div style='font-size:36px;font-weight:bold;letter-spacing:12px;"
            + "background:#F5F5F5;padding:16px;text-align:center;border-radius:8px'>"
            + otp + "</div>"
            + "<p>Este codigo expira en 10 minutos. No lo compartas con nadie.</p>"
            + "</body></html>";
        enviarEmailHtml(to, "Codigo de verificacion - Nexus", html);
    }

    @Async
    public void enviarConfirmacionCompra(String to, String tituloProducto, Double precio) {
        String html = "<html><body style='font-family:sans-serif;max-width:600px;margin:auto'>"
            + "<h2>Compra confirmada - Nexus</h2>"
            + "<p>Tu compra ha sido confirmada correctamente.</p>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='padding:8px;border-bottom:1px solid #eee'><b>Producto</b></td>"
            + "<td style='padding:8px;border-bottom:1px solid #eee'>" + tituloProducto + "</td></tr>"
            + "<tr><td style='padding:8px'><b>Total</b></td>"
            + "<td style='padding:8px'>" + String.format("%.2f", precio) + " EUR</td></tr>"
            + "</table>"
            + "<p>El vendedor ha sido notificado y preparara tu pedido.</p>"
            + "</body></html>";
        enviarEmailHtml(to, "Compra confirmada - Nexus", html);
    }

    @Async
    public void enviarNotificacionEnvio(String to, String tituloProducto,
                                         String trackingNum, String transportista) {
        String html = "<html><body style='font-family:sans-serif;max-width:600px;margin:auto'>"
            + "<h2>Tu pedido ha sido enviado - Nexus</h2>"
            + "<p>Tu compra de <b>" + tituloProducto + "</b> ha sido enviada.</p>"
            + (trackingNum != null
                ? "<p><b>Numero de seguimiento:</b> " + trackingNum + " (" + transportista + ")</p>"
                : "")
            + "</body></html>";
        enviarEmailHtml(to, "Tu pedido esta en camino - Nexus", html);
    }
}