package com.nexus.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nexus.entity.Actor;
import com.nexus.entity.Admin;
import com.nexus.entity.Empresa;
import com.nexus.entity.Usuario;
import com.nexus.repository.ActorRepository;
import com.nexus.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActorRepository actorRepository; 
    
    @Autowired
    @Lazy 
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; 

    @Value("${google.client.id}")
    private String googleClientId;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Actor actor = actorRepository.findByUser(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        if (actor instanceof Usuario && !((Usuario) actor).isEsVerificado()) {
             throw new UsernameNotFoundException("Cuenta no verificada. Revisa tu correo.");
        }

        String rol = obtenerRol(actor);
        return new User(actor.getUser(), actor.getPassword(), Collections.singletonList(new SimpleGrantedAuthority(rol)));
    }

    // --- REGISTRO NORMAL ---
    public Usuario registrarUsuario(Usuario usuario) {
        if (actorRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEsVerificado(false); 
        
        String codigo = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoVerificacion(codigo);
        
        Usuario guardado = usuarioRepository.save(usuario);
        emailService.enviarCodigoVerificacion(usuario.getEmail(), codigo);
        return guardado;
    }

    // --- VERIFICAR CÓDIGO ---
    public boolean verificarCuenta(String email, String codigo) {
        Optional<Actor> oActor = actorRepository.findByEmail(email);
        if (oActor.isPresent() && oActor.get() instanceof Usuario) {
            Usuario usuario = (Usuario) oActor.get();
            if (codigo != null && codigo.equals(usuario.getCodigoVerificacion())) {
                usuario.setEsVerificado(true);
                usuario.setCodigoVerificacion(null);
                usuarioRepository.save(usuario);
                return true;
            }
        }
        return false;
    }

    // --- LOGIN CON GOOGLE ---
    public Actor ingresarConGoogle(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");

            Optional<Actor> oActor = actorRepository.findByEmail(email);

            if (oActor.isPresent()) {
                return oActor.get(); 
            } else {
                Usuario nuevo = new Usuario();
                nuevo.setEmail(email);
                nuevo.setUser(email); 
                nuevo.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                nuevo.setFechaRegistro(LocalDateTime.now());
                
                // ✅ CORREGIDO: Ahora usa setAvatar
                nuevo.setAvatar(pictureUrl);
                
                nuevo.setEsVerificado(true); 
                nuevo.setTelefono("Pendiente"); 
                nuevo.setUbicacion("Desconocida"); 

                return usuarioRepository.save(nuevo);
            }
        } else {
            throw new IllegalArgumentException("Token Google inválido");
        }
    }
    
    public String obtenerRol(Actor actor) {
        if (actor instanceof Admin) return "ADMIN";
        if (actor instanceof Empresa) return "EMPRESA";
        return "USUARIO";
    }

    public Optional<Usuario> findById(Integer id) { return usuarioRepository.findById(id); }
    public List<Usuario> findAll() { return usuarioRepository.findAll(); }
    public Usuario save(Usuario usuario) { return usuarioRepository.save(usuario); } 
    public void delete(Integer id) { usuarioRepository.deleteById(id); }
}