package com.nexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Verificación de Google reCAPTCHA v3.
 *
 * ═══════════════════════════════════════════════════════════════════
 *  SETUP ANGULAR:
 *
 *  1. npm install ng-recaptcha
 *
 *  2. app.module.ts:
 *     import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module } from 'ng-recaptcha';
 *     imports: [RecaptchaV3Module],
 *     providers: [{ provide: RECAPTCHA_V3_SITE_KEY, useValue: 'TU_SITE_KEY' }]
 *
 *  3. En el componente de login/registro:
 *     constructor(private recaptchaV3Service: ReCaptchaV3Service) {}
 *
 *     async onSubmit() {
 *       const token = await this.recaptchaV3Service.execute('login').toPromise();
 *       // Añadir el token al body de la petición:
 *       this.http.post('/auth/login', { ...datos, captchaToken: token })
 *     }
 *
 *  4. Obtener las claves en: https://www.google.com/recaptcha/admin
 *     → Tipo: reCAPTCHA v3
 *     → Site Key → en Angular (environment.ts)
 *     → Secret Key → en application.properties (recaptcha.secret.key)
 * ═══════════════════════════════════════════════════════════════════
 */
@Service
public class CaptchaService {

    @Value("${recaptcha.secret.key:}")
    private String secretKey;

    @Value("${recaptcha.verify.url:https://www.recaptcha.net/recaptcha/api/siteverify}")
    private String verifyUrl;

    @Value("${recaptcha.min.score:0.5}")
    private double minScore;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verifica el token de reCAPTCHA enviado desde Angular.
     * @return true si el captcha es válido y el score supera el mínimo
     */
    @SuppressWarnings("unchecked")
    public boolean verificar(String captchaToken) {
        // En desarrollo (sin clave configurada) siempre pasa
        if (secretKey == null || secretKey.isBlank()) {
            System.out.println("⚠️ [CAPTCHA-DEV] Sin clave configurada, se omite verificación");
            return true;
        }

        if (captchaToken == null || captchaToken.isBlank()) return false;

        try {
            String url = verifyUrl + "?secret=" + secretKey + "&response=" + captchaToken;
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

            if (response == null) return false;
            boolean success = Boolean.TRUE.equals(response.get("success"));
            double  score   = response.containsKey("score")
                ? ((Number) response.get("score")).doubleValue() : 0.0;

            System.out.println("🤖 CAPTCHA — success=" + success + ", score=" + score);
            return success && score >= minScore;
        } catch (Exception e) {
            System.err.println("❌ Error verificando CAPTCHA: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lanza excepción si el captcha falla. Usar en endpoints críticos.
     */
    public void verificarOLanzar(String token) {
        if (!verificar(token))
            throw new IllegalArgumentException("Verificación de seguridad fallida. Refresca la página e inténtalo de nuevo.");
    }
}