package com.nexus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nexus.entity.Admin;
import com.nexus.entity.Comentario;
import com.nexus.entity.Contrato;
import com.nexus.entity.Empresa;
import com.nexus.entity.Mensaje;
import com.nexus.entity.Oferta;
import com.nexus.entity.Producto;
import com.nexus.entity.TipoContrato;
import com.nexus.entity.TipoOferta;
import com.nexus.entity.Usuario;
import com.nexus.repository.AdminRepository;
import com.nexus.repository.ComentarioRepository;
import com.nexus.repository.ContratoRepository;
import com.nexus.repository.EmpresaRepository;
import com.nexus.repository.MensajeRepository;
import com.nexus.repository.OfertaRepository;
import com.nexus.repository.ProductoRepository;
import com.nexus.repository.UsuarioRepository;

@Component
public class PopulateDB {

    @Autowired private AdminRepository adminRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private OfertaRepository ofertaRepository;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private ComentarioRepository comentarioRepository;
    
    @Autowired private PasswordEncoder passwordEncoder;

    public void popular() {
        if (adminRepository.count() > 0) {
            System.out.println("ℹ️ BD ya inicializada.");
            return;
        }

        System.out.println("🌱 Ejecutando PopulateDB...");
        
        // --- 1. ACTORES ---
        Admin admin = new Admin();
        admin.setUser("admin");
        admin.setEmail("admin@nexus.com");
        admin.setPassword(passwordEncoder.encode("Admin123!")); 
        adminRepository.save(admin);

        Usuario maria = new Usuario();
        maria.setUser("mariapepa01");
        maria.setEmail("mariapepa@gmail.com");
        maria.setPassword(passwordEncoder.encode("Usuario123!"));
        maria.setTelefono("600123456");
        maria.setUbicacion("Madrid");
        maria.setEsVerificado(true);
        maria.setReputacion(5);
        maria.setBiografia("Fan de la tecnología");
        maria.setAvatar("https://i.pravatar.cc/150?u=alice"); // ✅ CORREGIDO
        maria = usuarioRepository.save(maria);
        
        Usuario pepe = new Usuario();
        pepe.setUser("Pepe");
        pepe.setEmail("pepe@gmail.com");
        pepe.setPassword(passwordEncoder.encode("Usuario123!"));
        pepe.setTelefono("600999888");
        pepe.setUbicacion("Sevilla");
        pepe.setEsVerificado(true);
        pepe.setReputacion(3);
        pepe = usuarioRepository.save(pepe);

        Empresa ecentia = new Empresa();
        ecentia.setUser("EcentiaMarketing");
        ecentia.setEmail("contacto@ecentia.com");
        ecentia.setPassword(passwordEncoder.encode("Empresa123!"));
        ecentia.setCif("B12345678");
        ecentia = empresaRepository.save(ecentia);

        System.out.println("✅ Actores creados (Admin, MariaPepa, Pepe y Ecentia).");

        // --- 2. PRODUCTOS (Con imagen principal) ---
        Producto p1 = new Producto("iPhone 13", "Como nuevo", 600.0, TipoOferta.VENTA, maria, "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5");
        Producto p2 = new Producto("PS5", "Con 2 mandos", 450.0, TipoOferta.VENTA, maria, "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3");
        productoRepository.saveAll(List.of(p1, p2));
        System.out.println("✅ Productos creados.");

        // --- 3. MENSAJES ---
        Mensaje m1 = new Mensaje("Hola, ¿sigue disponible?", pepe, p1);
        Mensaje m2 = new Mensaje("Sí, claro. ¿Te interesa?", maria, p1);
        mensajeRepository.saveAll(List.of(m1, m2));
        System.out.println("✅ Mensajes creados.");

        // --- 4. CONTRATOS ---
        Contrato c1 = new Contrato();
        c1.setEmpresa(ecentia);
        c1.setTipoContrato(TipoContrato.BANNER);
        contratoRepository.save(c1);
        System.out.println("✅ Contratos creados.");

        // --- 5. OFERTAS Y COMENTARIOS (Con imagen principal) ---
        Oferta off1 = new Oferta();
        off1.setActor(ecentia);
        off1.setTitulo("Rebajas de Primavera");
        off1.setDescripcion("Descuentos exclusivos en nuestra web.");
        off1.setTienda("Ecentia Store");
        off1.setImagenPrincipal("https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da"); // ✅ CORREGIDO
        off1.setPrecioOriginal(100.0);
        off1.setPrecioOferta(80.0);
        off1.setFechaExpiracion(LocalDateTime.now().plusDays(10));
        off1 = ofertaRepository.save(off1);
        
        Comentario com1 = new Comentario("Gran oferta!", off1, maria);
        comentarioRepository.save(com1);
        
        System.out.println("✅ Ofertas y Comentarios creados.");
        System.out.println("🎉 Base de datos poblada exitosamente.");
    }
}