package com.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SOLUCION AL CICLO CIRCULAR:
 *
 *   JWTAuthenticationFilter -> UserDetailsService (UsuarioService)
 *        -> PasswordEncoder -> SecurityConfiguration
 *        -> JWTAuthenticationFilter   <- CICLO
 *
 * El problema: PasswordEncoder estaba definido como @Bean dentro de
 * SecurityConfiguration, la cual inyecta JWTAuthenticationFilter,
 * que a su vez necesita UsuarioService, que necesita PasswordEncoder.
 *
 * La solucion: sacar el @Bean PasswordEncoder a esta clase independiente.
 * Asi SecurityConfiguration ya no tiene dependencia transitiva de si misma.
 *
 * PASOS:
 *   1. Crea este archivo en com/nexus/security/PasswordConfig.java
 *   2. En SecurityConfiguration.java: ELIMINA el metodo @Bean passwordEncoder()
 *   3. Reinicia. El ciclo desaparece.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}