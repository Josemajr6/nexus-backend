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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                    "http://localhost:4200", 
                    "https://nexus-app.es", 
                    "https://www.nexus-app.es"
                )); 
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- Rutas PÚBLICAS (Corregido con el prefijo /auth) ---
                .requestMatchers("/auth/**").permitAll() 
                .requestMatchers(HttpMethod.GET, "/producto", "/producto/**").permitAll()
                
                // --- Rutas SWAGGER (Documentación) ---
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // --- Rutas ADMIN ---
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                
                // --- Rutas EMPRESA ---
                .requestMatchers("/empresa/**").hasAuthority("EMPRESA")

                // --- GESTIÓN DE PRODUCTOS ---
                .requestMatchers(HttpMethod.POST, "/producto").hasAnyAuthority("EMPRESA", "USUARIO") 
                .requestMatchers(HttpMethod.PUT, "/producto/**").hasAnyAuthority("EMPRESA", "USUARIO")
                .requestMatchers(HttpMethod.DELETE, "/producto/**").hasAnyAuthority("EMPRESA", "USUARIO")

                // --- Rutas USUARIO ---
                .requestMatchers("/compra/**").hasAnyAuthority("USUARIO", "ADMIN")
                
                // --- Rutas Comunes (Autenticados) ---
                .requestMatchers("/contrato/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}