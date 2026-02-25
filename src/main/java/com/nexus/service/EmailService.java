package com.nexus.service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    @Autowired private JavaMailSender mailSender;
    @Value("${nexus.mail.from:somosnexusapp@gmail.com}") private String fromEmail;
    @Value("${nexus.frontend.url:http://localhost:4200}") private String frontendUrl;

    @Async
    public void enviarEmail(String to, String subject, String body) {
        try { SimpleMailMessage m=new SimpleMailMessage(); m.setFrom(fromEmail); m.setTo(to); m.setSubject(subject); m.setText(body); mailSender.send(m); }
        catch(Exception e){System.err.println("Email error: "+e.getMessage());}
    }
    @Async
    public void enviarEmailHtml(String to, String subject, String html) {
        try { MimeMessage m=mailSender.createMimeMessage(); MimeMessageHelper h=new MimeMessageHelper(m,true,"UTF-8"); h.setFrom(fromEmail); h.setTo(to); h.setSubject(subject); h.setText(html,true); mailSender.send(m); }
        catch(Exception e){System.err.println("HTML email error: "+e.getMessage());}
    }
    @Async
    public void enviarVerificacion(String to, String username, String codigo) {
        enviarEmailHtml(to, "Verifica tu cuenta en Nexus: "+codigo,
            "<html><body><h2>Verifica tu cuenta</h2><p>Hola <b>"+username+"</b></p>"
            +"<div style='font-size:36px;font-weight:bold;background:#F5F5F5;padding:16px;text-align:center;color:#FF5722'>"+codigo+"</div>"
            +"<p>Expira en 30 minutos.</p></body></html>");
    }
    @Async
    public void enviarResetPassword(String to, String token) {
        String link=frontendUrl+"/auth/reset-password?token="+token;
        enviarEmailHtml(to,"Restablecer contrasena - Nexus",
            "<html><body><h2>Restablecer contrasena</h2><a href='"+link+"' style='background:#FF5722;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none'>Restablecer</a><p>Expira en 15 min.</p></body></html>");
    }
    /**
     * Requerido por TwoFactorService line 102:
     *   emailService.enviarOtp2FA(email, otp, motivo)
     */
    @Async
    public void enviarOtp2FA(String to, String otp, String motivo) {
        enviarEmailHtml(to,"Codigo 2FA Nexus: "+otp,
            "<html><body><h2>Codigo de verificacion</h2><p>Codigo para <b>"+motivo+"</b>:</p>"
            +"<div style='font-size:36px;font-weight:bold;background:#E3F2FD;padding:16px;text-align:center;color:#1565C0'>"+otp+"</div>"
            +"<p>Expira en 10 minutos. No lo compartas.</p></body></html>");
    }
    @Async public void enviarOtpDosFactores(String to, String otp) { enviarOtp2FA(to,otp,"verificacion"); }
    @Async
    public void enviarConfirmacionCompra(String to, String titulo, Double precio) {
        enviarEmailHtml(to,"Compra confirmada - "+titulo,
            "<html><body><h2>Compra confirmada</h2><p><b>"+titulo+"</b></p><p>"+String.format("%.2f EUR",precio)+"</p></body></html>");
    }
    @Async
    public void enviarNotificacionEnvio(String to, String titulo, String tracking, String transportista) {
        String t=tracking!=null?"<p><b>Seguimiento:</b> "+tracking+" via "+transportista+"</p>":"";
        enviarEmailHtml(to,"Tu pedido esta en camino - Nexus","<html><body><h2>Pedido enviado</h2><p><b>"+titulo+"</b></p>"+t+"</body></html>");
    }
}
