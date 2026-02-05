package com.nexus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nexus.entity.*;
import com.nexus.repository.*;

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
    @Autowired private SparkVotoRepository sparkVotoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public void popular() {
        if (adminRepository.count() > 0) {
            System.out.println("ℹ️  BD ya inicializada.");
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
        maria.setBiografia("Fan de la tecnología y chollos");
        maria.setAvatar("https://i.pravatar.cc/150?u=alice");
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

        System.out.println("✅ Actores creados");

        // --- 2. PRODUCTOS ---
        Producto p1 = new Producto("iPhone 13", "Como nuevo", 600.0, TipoOferta.VENTA, maria, 
            "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5");
        Producto p2 = new Producto("PS5", "Con 2 mandos", 450.0, TipoOferta.VENTA, maria, 
            "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3");
        productoRepository.saveAll(List.of(p1, p2));
        System.out.println("✅ Productos creados");

        // --- 3. MENSAJES ---
        Mensaje m1 = new Mensaje("Hola, ¿sigue disponible?", pepe, p1);
        Mensaje m2 = new Mensaje("Sí, claro. ¿Te interesa?", maria, p1);
        mensajeRepository.saveAll(List.of(m1, m2));
        System.out.println("✅ Mensajes creados");

        // --- 4. CONTRATOS ---
        Contrato c1 = new Contrato();
        c1.setEmpresa(ecentia);
        c1.setTipoContrato(TipoContrato.BANNER);
        contratoRepository.save(c1);
        System.out.println("✅ Contratos creados");

        // --- 5. OFERTAS CON SISTEMA SPARK ---
        Oferta off1 = new Oferta();
        off1.setActor(ecentia);
        off1.setTitulo("Auriculares Sony WH-1000XM5 - Precio MÍNIMO");
        off1.setDescripcion("Los mejores auriculares con cancelación de ruido del mercado. Batería de 30 horas, sonido premium.");
        off1.setTienda("Amazon");
        off1.setImagenPrincipal("https://images.unsplash.com/photo-1546435770-a3e426bf472b");
        off1.setPrecioOriginal(399.0);
        off1.setPrecioOferta(299.0);
        off1.setUrlOferta("https://www.amazon.es/Sony-WH-1000XM5");
        off1.setFechaExpiracion(LocalDateTime.now().plusDays(10));
        off1.setCategoria("Tecnología");
        off1.setSparkCount(127); // ⚡ Mucho interés
        off1.setDripCount(8);
        off1.setNumeroVistas(1542);
        off1.setNumeroCompartidos(89);
        off1.actualizarBadge(); // Calcula automáticamente
        off1 = ofertaRepository.save(off1);
        
        Oferta off2 = new Oferta();
        off2.setActor(maria);
        off2.setTitulo("iPad Air M2 256GB - HISTÓRICO");
        off2.setDescripcion("iPad Air con chip M2, pantalla Liquid Retina de 11 pulgadas. Compatible con Apple Pencil Pro.");
        off2.setTienda("MediaMarkt");
        off2.setImagenPrincipal("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0");
        off2.setPrecioOriginal(849.0);
        off2.setPrecioOferta(699.0);
        off2.setUrlOferta("https://www.mediamarkt.es/es/product/ipad-air-m2");
        off2.setFechaExpiracion(LocalDateTime.now().plusDays(3));
        off2.setCategoria("Tecnología");
        off2.setSparkCount(92);
        off2.setDripCount(12);
        off2.setNumeroVistas(892);
        off2.setNumeroCompartidos(45);
        off2.actualizarBadge();
        off2 = ofertaRepository.save(off2);
        
        Oferta off3 = new Oferta();
        off3.setActor(ecentia);
        off3.setTitulo("Zapatillas Nike Air Max 90 - 40% OFF");
        off3.setDescripcion("Clásicas Air Max 90 en varios colores. Tallas del 38 al 46.");
        off3.setTienda("Nike Store");
        off3.setImagenPrincipal("https://images.unsplash.com/photo-1542291026-7eec264c27ff");
        off3.setPrecioOriginal(140.0);
        off3.setPrecioOferta(84.0);
        off3.setUrlOferta("https://www.nike.com/es/t/air-max-90");
        off3.setFechaExpiracion(LocalDateTime.now().plusDays(7));
        off3.setCategoria("Moda");
        off3.setSparkCount(67);
        off3.setDripCount(15);
        off3.setNumeroVistas(654);
        off3.setNumeroCompartidos(23);
        off3.actualizarBadge();
        off3 = ofertaRepository.save(off3);
        
        Oferta off4 = new Oferta();
        off4.setActor(maria);
        off4.setTitulo("Samsung Galaxy Watch 6 Classic - Black Friday");
        off4.setDescripcion("Smartwatch premium con pantalla AMOLED, GPS, monitorización de salud 24/7.");
        off4.setTienda("PcComponentes");
        off4.setImagenPrincipal("https://images.unsplash.com/photo-1579586337278-3befd40fd17a");
        off4.setPrecioOriginal(469.0);
        off4.setPrecioOferta(329.0);
        off4.setUrlOferta("https://www.pccomponentes.com/samsung-galaxy-watch-6");
        off4.setFechaExpiracion(LocalDateTime.now().plusHours(18)); // ¡Expira pronto!
        off4.setCategoria("Tecnología");
        off4.setSparkCount(158);
        off4.setDripCount(3);
        off4.setNumeroVistas(2103);
        off4.setNumeroCompartidos(112);
        off4.actualizarBadge(); // Badge LEGENDARY
        off4 = ofertaRepository.save(off4);
        
        // Votos Spark de ejemplo
        SparkVoto voto1 = new SparkVoto(maria, off4, true); // ⚡ Spark
        SparkVoto voto2 = new SparkVoto(pepe, off4, true); // ⚡ Spark
        sparkVotoRepository.saveAll(List.of(voto1, voto2));
        
        // Comentarios
        Comentario com1 = new Comentario("¡Qué chollo! Los compré ayer y son brutales", off1, maria);
        Comentario com2 = new Comentario("Ojo que en El Corte Inglés están a 289€", off1, pepe);
        Comentario com3 = new Comentario("Precio mínimo en Amazon, aprovechar", off2, maria);
        comentarioRepository.saveAll(List.of(com1, com2, com3));
        
        off1.actualizarNumeroComentarios();
        off2.actualizarNumeroComentarios();
        ofertaRepository.saveAll(List.of(off1, off2));
        
        System.out.println("✅ Ofertas y Comentarios creados");
        System.out.println("🎉 Base de datos poblada exitosamente");
        System.out.println("📊 Ofertas: " + ofertaRepository.count());
        System.out.println("📦 Productos: " + productoRepository.count());
        System.out.println("⚡ Sistema SPARK activado");
    }
}