package com.nexus.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.nexus.entity.*;
import com.nexus.repository.*;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired private ActorRepository   actorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder   passwordEncoder;
    @Autowired private EmailService      emailService;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${nexus.verification.expiry-minutes:30}")
    private int verificationExpiryMinutes;

    private final ConcurrentHashMap<Integer, VerificationEntry> verificationCodes
        = new ConcurrentHashMap<>();

    // ---- UserDetailsService (Spring Security) ---------------------------

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Actor actor = actorRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        if (actor.isCuentaEliminada())
            throw new UsernameNotFoundException("Cuenta eliminada");
        Collection<GrantedAuthority> authorities =
            List.of(new SimpleGrantedAuthority(obtenerRol(actor)));
        return new User(actor.getUser(), actor.getPassword(), authorities);
    }

    // ---- CRUD -----------------------------------------------------------

    public List<Usuario> findAll() { return usuarioRepository.findAll(); }

    public Optional<Usuario> findById(Integer id) { return usuarioRepository.findById(id); }

    @Transactional
    public Usuario save(Usuario usuario) {
        // NOTA: en Java el signo $ no necesita escape en strings.
        // "$2a$" es el prefijo de los hashes BCrypt.
        if (usuario.getPassword() != null
                && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Integer id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setCuentaEliminada(true);
            u.setEmail("deleted_" + id + "@nexus.deleted");
            usuarioRepository.save(u);
        });
    }

    // ---- Registro -------------------------------------------------------

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (actorRepository.findByUsername(usuario.getUser()).isPresent())
            throw new IllegalArgumentException("Ese nombre de usuario ya esta en uso");
        if (actorRepository.findByEmail(usuario.getEmail()).isPresent())
            throw new IllegalArgumentException("Ese email ya esta registrado");

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setCuentaVerificada(false);
        Usuario guardado = usuarioRepository.save(usuario);

        String codigo = generarCodigo6Digitos();
        verificationCodes.put(guardado.getId(),
            new VerificationEntry(codigo, guardado.getEmail(),
                LocalDateTime.now().plusMinutes(verificationExpiryMinutes)));
        emailService.enviarVerificacion(guardado.getEmail(), guardado.getUser(), codigo);
        return guardado;
    }

    @Transactional
    public boolean verificarCuenta(String email, String codigo) {
        Actor actor = actorRepository.findByEmail(email).orElse(null);
        if (actor == null) return false;
        VerificationEntry entry = verificationCodes.get(actor.getId());
        if (entry == null || !entry.codigo().equals(codigo)) return false;
        if (entry.expira().isBefore(LocalDateTime.now())) {
            verificationCodes.remove(actor.getId());
            return false;
        }
        actor.setCuentaVerificada(true);
        actorRepository.save(actor);
        verificationCodes.remove(actor.getId());
        return true;
    }

    // ---- Cambio de email ------------------------------------------------

    public String generarCodigoVerificacion(Integer actorId, String nuevoEmail) {
        String codigo = generarCodigo6Digitos();
        verificationCodes.put(actorId,
            new VerificationEntry(codigo, nuevoEmail,
                LocalDateTime.now().plusMinutes(verificationExpiryMinutes)));
        return codigo;
    }

    public boolean verificarCambioEmail(Integer actorId, String nuevoEmail, String codigo) {
        VerificationEntry entry = verificationCodes.get(actorId);
        if (entry == null || !entry.codigo().equals(codigo)) return false;
        if (!entry.emailDestino().equals(nuevoEmail)) return false;
        if (entry.expira().isBefore(LocalDateTime.now())) {
            verificationCodes.remove(actorId);
            return false;
        }
        verificationCodes.remove(actorId);
        return true;
    }

    // ---- Google OAuth ---------------------------------------------------

    @Transactional
    public Actor ingresarConGoogle(String tokenId) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(List.of(googleClientId))
            .build();
        GoogleIdToken idToken = verifier.verify(tokenId);
        if (idToken == null) throw new IllegalArgumentException("Token de Google invalido");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email  = payload.getEmail();
        String nombre = (String) payload.get("given_name");
        String foto   = (String) payload.get("picture");

        return actorRepository.findByEmail(email).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setUser(generarUsernameUnico(nombre));
            nuevo.setPassword(passwordEncoder.encode(
                java.util.UUID.randomUUID().toString()));
            nuevo.setAvatar(foto);
            nuevo.setCuentaVerificada(true);
            return usuarioRepository.save(nuevo);
        });
    }

    // ---- Helpers --------------------------------------------------------

    public String obtenerRol(Actor actor) {
        if (actor instanceof Admin)   return "ADMIN";
        if (actor instanceof Empresa) return "EMPRESA";
        return "USUARIO";
    }

    private String generarCodigo6Digitos() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }

    private String generarUsernameUnico(String base) {
        String limpio = (base != null)
            ? base.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() : "user";
        if (limpio.isEmpty()) limpio = "user";
        String candidato = limpio;
        int i = 1;
        while (actorRepository.findByUsername(candidato).isPresent())
            candidato = limpio + i++;
        return candidato;
    }

    private record VerificationEntry(
        String codigo, String emailDestino, LocalDateTime expira) {}
}