package com.nexus.service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.*;
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
    @Value("${google.client.id:}") private String googleClientId;
    @Value("${nexus.verification.expiry-minutes:30}") private int verifyExpiry;
    private final ConcurrentHashMap<Integer,VerifEntry> codes=new ConcurrentHashMap<>();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Actor actor=actorRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("No encontrado: "+username));
        if(actor.isCuentaEliminada())throw new UsernameNotFoundException("Cuenta eliminada");
        Collection<GrantedAuthority> auth=List.of(new SimpleGrantedAuthority(obtenerRol(actor)));
        return new User(actor.getUser(),actor.getPassword(),auth);
    }
    public List<Usuario>     findAll()            { return usuarioRepository.findAll(); }
    public Optional<Usuario> findById(Integer id) { return usuarioRepository.findById(id); }
    @Transactional
    public Usuario save(Usuario u) {
        // FIX UsuarioService line 73: "$2a$" NO necesita escape
        if(u.getPassword()!=null&&!u.getPassword().startsWith("$2a$"))
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        return usuarioRepository.save(u);
    }
    @Transactional
    public void delete(Integer id) {
        usuarioRepository.findById(id).ifPresent(u->{u.setCuentaEliminada(true);u.setEmail("deleted_"+id+"@nexus.deleted");usuarioRepository.save(u);});
    }
    @Transactional
    public Usuario registrarUsuario(Usuario u) {
        if(actorRepository.findByUsername(u.getUser()).isPresent())throw new IllegalArgumentException("Username en uso");
        if(actorRepository.findByEmail(u.getEmail()).isPresent())throw new IllegalArgumentException("Email registrado");
        u.setPassword(passwordEncoder.encode(u.getPassword())); u.setCuentaVerificada(false);
        Usuario g=usuarioRepository.save(u);
        String cod=codigo6(); codes.put(g.getId(),new VerifEntry(cod,g.getEmail(),LocalDateTime.now().plusMinutes(verifyExpiry)));
        emailService.enviarVerificacion(g.getEmail(),g.getUser(),cod); return g;
    }
    @Transactional
    public boolean verificarCuenta(String email, String codigo) {
        Actor a=actorRepository.findByEmail(email).orElse(null); if(a==null)return false;
        VerifEntry e=codes.get(a.getId()); if(e==null||!e.cod().equals(codigo))return false;
        if(e.expira().isBefore(LocalDateTime.now())){codes.remove(a.getId());return false;}
        a.setCuentaVerificada(true); actorRepository.save(a); codes.remove(a.getId()); return true;
    }
    @Transactional
    public Actor ingresarConGoogle(String tokenId) throws Exception {
        GoogleIdTokenVerifier v=new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),GsonFactory.getDefaultInstance()).setAudience(List.of(googleClientId)).build();
        GoogleIdToken t=v.verify(tokenId); if(t==null)throw new IllegalArgumentException("Token invalido");
        GoogleIdToken.Payload p=t.getPayload(); String email=p.getEmail(),nombre=(String)p.get("given_name"),foto=(String)p.get("picture");
        return actorRepository.findByEmail(email).orElseGet(()->{
            Usuario nu=new Usuario(); nu.setEmail(email); nu.setUser(usernameUnico(nombre));
            nu.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); nu.setAvatar(foto); nu.setCuentaVerificada(true);
            return usuarioRepository.save(nu);
        });
    }
    public String obtenerRol(Actor a) {
        if(a instanceof Admin)return"ADMIN"; if(a instanceof Empresa)return"EMPRESA"; return"USUARIO";
    }
    private String codigo6(){return String.format("%06d",new SecureRandom().nextInt(999999));}
    private String usernameUnico(String base){
        String l=(base!=null)?base.replaceAll("[^a-zA-Z0-9]","").toLowerCase():"user"; if(l.isEmpty())l="user";
        String c=l; int i=1; while(actorRepository.findByUsername(c).isPresent())c=l+i++; return c;
    }
    private record VerifEntry(String cod, String email, LocalDateTime expira){}
}
