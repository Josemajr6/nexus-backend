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

    @Autowired private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration c = new CorsConfiguration();
                c.setAllowedOrigins(List.of(
                    "http://localhost:4200", "http://localhost:4201",
                    "https://nexus-app.es", "https://www.nexus-app.es"));
                c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                c.setAllowedHeaders(List.of("*"));
                c.setAllowCredentials(true);
                return c;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Públicas ──────────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/ws/**").permitAll()          // SockJS handshake HTTP
                .requestMatchers(HttpMethod.GET, "/producto",   "/producto/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/vehiculo",   "/vehiculo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/oferta",     "/oferta/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/votos/**").permitAll()

                // ── Admin ─────────────────────────────────────────────
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/envio/reembolsar/**").hasAuthority("ADMIN")

                // ── Empresa ───────────────────────────────────────────
                .requestMatchers("/empresa/**").hasAuthority("EMPRESA")

                // ── Productos y vehículos (escritura) ─────────────────
                .requestMatchers(HttpMethod.POST,   "/producto/**").hasAnyAuthority("EMPRESA","USUARIO")
                .requestMatchers(HttpMethod.PUT,    "/producto/**").hasAnyAuthority("EMPRESA","USUARIO")
                .requestMatchers(HttpMethod.PATCH,  "/producto/**").hasAnyAuthority("EMPRESA","USUARIO")
                .requestMatchers(HttpMethod.DELETE, "/producto/**").hasAnyAuthority("EMPRESA","USUARIO")
                .requestMatchers(HttpMethod.POST,   "/vehiculo/**").hasAuthority("USUARIO")
                .requestMatchers(HttpMethod.PUT,    "/vehiculo/**").hasAuthority("USUARIO")
                .requestMatchers(HttpMethod.DELETE, "/vehiculo/**").hasAuthority("USUARIO")

                // ── Chat, Compras, Envíos, Devoluciones, Ajustes ─────
                .requestMatchers("/chat/**").authenticated()
                .requestMatchers("/compra/**").hasAnyAuthority("USUARIO","ADMIN")
                .requestMatchers("/envio/**").hasAnyAuthority("USUARIO","ADMIN")
                .requestMatchers("/devolucion/**").hasAnyAuthority("USUARIO","ADMIN")
                .requestMatchers("/ajustes/**").authenticated()
                .requestMatchers("/votos/**").authenticated()

                // ── Todo lo demás ─────────────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}