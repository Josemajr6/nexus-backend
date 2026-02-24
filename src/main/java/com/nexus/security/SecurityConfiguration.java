package com.nexus.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JWTAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ---- Swagger (solo en dev) --------------------------------
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // ---- WebSocket -------------------------------------------
                .requestMatchers("/ws/**").permitAll()

                // ---- Auth ------------------------------------------------
                .requestMatchers("/auth/**").permitAll()

                // ---- Newsletter (suscribir, confirmar y baja son publicos) --
                .requestMatchers(HttpMethod.POST, "/newsletter/suscribir").permitAll()
                .requestMatchers(HttpMethod.GET,  "/newsletter/confirmar").permitAll()
                .requestMatchers(HttpMethod.GET,  "/newsletter/baja").permitAll()
                .requestMatchers("/newsletter/**").authenticated()

                // ---- Categorias (lectura publica, escritura admin) --------
                .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                .requestMatchers("/categorias/**").hasAuthority("ADMIN")

                // ---- Ofertas (lectura publica, escritura autenticada) -----
                .requestMatchers(HttpMethod.GET, "/oferta/**").permitAll()
                .requestMatchers("/oferta/**").authenticated()

                // ---- Productos (lectura publica) -------------------------
                .requestMatchers(HttpMethod.GET, "/producto/**").permitAll()
                .requestMatchers("/producto/**").authenticated()

                // ---- Vehiculos (lectura publica) -------------------------
                .requestMatchers(HttpMethod.GET, "/vehiculo/**").permitAll()
                .requestMatchers("/vehiculo/**").authenticated()

                // ---- Usuarios (perfil publico, resto autenticado) --------
                .requestMatchers(HttpMethod.GET, "/usuario/*/perfil").permitAll()
                .requestMatchers("/usuario/**").authenticated()

                // ---- Notificaciones, ajustes, compras, envios... ---------
                .requestMatchers("/notificaciones/**").authenticated()
                .requestMatchers("/ajustes/**").authenticated()
                .requestMatchers("/compra/**").authenticated()
                .requestMatchers("/envio/**").authenticated()
                .requestMatchers("/devolucion/**").authenticated()
                .requestMatchers("/valoracion/**").authenticated()
                .requestMatchers("/chat/**").authenticated()
                .requestMatchers("/bloqueo/**").authenticated()
                .requestMatchers("/reporte/**").authenticated()
                .requestMatchers("/spark-voto/**").authenticated()

                // ---- Admin -----------------------------------------------
                .requestMatchers("/admin/**").hasAuthority("ADMIN")

                // ---- El resto requiere autenticacion --------------------
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "https://*.nexus.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }
}