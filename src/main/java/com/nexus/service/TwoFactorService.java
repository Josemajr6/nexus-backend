package com.nexus.service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexus.entity.Actor;
import com.nexus.repository.ActorRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
/**
 * Todos los overloads necesarios:
 *
 * AjustesController:
 *   configurarTotp(Integer)                        line 214
 *   verificarCodigoTotp(Integer, String)           line 257
 *   enviarOtpEmail(String, Integer)                line 241
 *
 * ActorController:
 *   enviarOtpEmail(Integer, String, String)        line 66
 *   verificarLoginTotp(Integer, String)            line 101
 */
@Service
public class TwoFactorService {
    @Autowired private ActorRepository actorRepository;
    @Autowired private EmailService    emailService;
    @Value("${totp.issuer:Nexus App}") private String issuer;
    @Value("${nexus.two-factor-email.expiry-minutes:10}") private int expiry;
    private final ConcurrentHashMap<Integer,OtpEntry> otpStore=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,String> pendingSecrets=new ConcurrentHashMap<>();

    // --- TOTP ---
    public Map<String,String> configurarTotp(Integer actorId) {
        Actor a=actorRepository.findById(actorId).orElseThrow(()->new IllegalArgumentException("Actor no encontrado"));
        String secret=new DefaultSecretGenerator().generate();
        pendingSecrets.put(actorId,secret);
        try{
            QrData qr=new QrData.Builder().label(a.getEmail()).secret(secret).issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();
            byte[]bytes=new ZxingPngQrGenerator().generate(qr);
            return Map.of("secret",secret,"qr","data:image/png;base64,"+Base64.getEncoder().encodeToString(bytes),"issuer",issuer,"accountName",a.getEmail());
        }catch(Exception e){throw new RuntimeException("Error QR: "+e.getMessage());}
    }
    @Transactional
    public boolean confirmarActivacionTotp(Integer actorId, String codigo) {
        String secret=pendingSecrets.get(actorId);
        if(secret==null||!verificarTotp(secret,codigo))return false;
        Actor a=actorRepository.findById(actorId).orElseThrow(()->new IllegalArgumentException("Actor no encontrado"));
        a.setTwoFactorSecret(secret); a.setTwoFactorEnabled(true); a.setTwoFactorMethod("TOTP");
        actorRepository.save(a); pendingSecrets.remove(actorId); return true;
    }
    /** AjustesController line 257: (Integer, String) */
    public boolean verificarCodigoTotp(Integer actorId, String codigo) {
        Actor a=actorRepository.findById(actorId).orElse(null);
        if(a==null||a.getTwoFactorSecret()==null)return false;
        return verificarTotp(a.getTwoFactorSecret(),codigo);
    }
    /** ActorController line 101 */
    public boolean verificarLoginTotp(Integer actorId, String codigo) { return verificarCodigoTotp(actorId,codigo); }
    /** Overload con secret directo */
    public boolean verificarCodigoTotp(String secret, String codigo) { return verificarTotp(secret,codigo); }
    private boolean verificarTotp(String secret, String codigo) {
        try{return new DefaultCodeVerifier(new DefaultCodeGenerator(),new SystemTimeProvider()).isValidCode(secret,codigo);}
        catch(Exception e){return false;}
    }

    // --- OTP Email ---
    /** AjustesController line 241: enviarOtpEmail(String email, Integer actorId) */
    public void enviarOtpEmail(String email, Integer actorId) { _sendOtp(email,actorId,"verificacion de identidad"); }
    /** AjustesController 3 args String primero */
    public void enviarOtpEmail(String email, Integer actorId, String motivo) { _sendOtp(email,actorId,motivo); }
    /** ActorController line 66: enviarOtpEmail(Integer actorId, String email, String motivo) */
    public void enviarOtpEmail(Integer actorId, String email, String motivo) { _sendOtp(email,actorId,motivo); }

    private void _sendOtp(String email, Integer actorId, String motivo) {
        String otp=String.format("%06d",new SecureRandom().nextInt(999999));
        otpStore.put(actorId,new OtpEntry(otp,LocalDateTime.now().plusMinutes(expiry)));
        emailService.enviarOtp2FA(email,otp,motivo);
    }
    public boolean verificarOtpEmail(Integer actorId, String codigo) {
        OtpEntry e=otpStore.get(actorId); if(e==null)return false;
        if(e.expira().isBefore(LocalDateTime.now())){otpStore.remove(actorId);return false;}
        boolean ok=e.otp().equals(codigo); if(ok)otpStore.remove(actorId); return ok;
    }
    private record OtpEntry(String otp, LocalDateTime expira){}
}
