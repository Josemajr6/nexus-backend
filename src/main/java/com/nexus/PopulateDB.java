package com.nexus;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nexus.entity.*;
import com.nexus.repository.*;

import java.time.LocalDateTime;

/**
 * Datos de prueba iniciales.
 * Solo se ejecuta si la BD esta vacia.
 *
 * CORRECCIONES respecto a versiones anteriores:
 *  - setCategoria(Categoria) ahora recibe un objeto Categoria, NO un String
 *  - SparkVoto constructor correcto: new SparkVoto(actor, oferta, esSpark)
 *  - Producto constructor: new Producto(titulo, desc, precio, tipoOferta, actor, imagen)
 *  - actualizarBadge() y actualizarNumeroComentarios() son metodos de instancia
 */
@Component
public class PopulateDB implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired private ActorRepository     actorRepository;
    @Autowired private UsuarioRepository   usuarioRepository;
    @Autowired private ProductoRepository  productoRepository;
    @Autowired private OfertaRepository    ofertaRepository;
    @Autowired private SparkVotoRepository sparkVotoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private PasswordEncoder     passwordEncoder;

    private boolean executed = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (executed) return;
        if (actorRepository.count() > 0) { executed = true; return; }
        executed = true;

        // ---- Categorias raiz ----------------------------------------
        Categoria catElectronica = getOrCreate("Electronica",  "electronica",  "devices",       "#1565C0", 1);
        Categoria catRopa        = getOrCreate("Ropa",         "ropa",         "checkroom",     "#6A1B9A", 2);
        Categoria catHogar       = getOrCreate("Hogar",        "hogar",        "home",          "#2E7D32", 3);
        Categoria catDeportes    = getOrCreate("Deportes",     "deportes",     "sports",        "#E65100", 4);
        Categoria catVehiculos   = getOrCreate("Vehiculos",    "vehiculos",    "directions_car","#1976D2", 5);
        Categoria catInformatica = getOrCreate("Informatica",  "informatica",  "laptop",        "#00838F", 6);
        Categoria catLibros      = getOrCreate("Libros",       "libros",       "menu_book",     "#4E342E", 7);

        // Sub-categorias de Electronica
        Categoria catMoviles  = getOrCreateHija("Moviles",  "moviles",  "smartphone", catElectronica);
        Categoria catAudio    = getOrCreateHija("Audio",    "audio",    "headphones", catElectronica);
        Categoria catTV       = getOrCreateHija("TV y Video","tv-video","tv",         catElectronica);

        // Sub-categorias de Vehiculos
        Categoria catCoches   = getOrCreateHija("Coches",   "coches",  "directions_car", catVehiculos);
        Categoria catMotos    = getOrCreateHija("Motos",    "motos",   "two_wheeler",    catVehiculos);

        // ---- Usuarios -----------------------------------------------
        Usuario carlos = new Usuario();
        carlos.setUser("carlos_vendedor");
        carlos.setEmail("carlos@nexus.test");
        carlos.setPassword(passwordEncoder.encode("password123"));
        carlos.setCuentaVerificada(true);
        carlos.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=carlos");
        carlos.setBiografia("Vendo articulos de electronica en buen estado.");
        carlos.setUbicacion("Madrid");
        usuarioRepository.save(carlos);

        Usuario maria = new Usuario();
        maria.setUser("maria_compradora");
        maria.setEmail("maria@nexus.test");
        maria.setPassword(passwordEncoder.encode("password123"));
        maria.setCuentaVerificada(true);
        maria.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=maria");
        maria.setUbicacion("Barcelona");
        usuarioRepository.save(maria);

        Usuario admin = new Admin();
        admin.setUser("admin");
        admin.setEmail("admin@nexus.test");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setCuentaVerificada(true);
        actorRepository.save(admin);

        // ---- Productos (wallapop) ------------------------------------
        Producto p1 = new Producto(
            "iPhone 14 Pro 128GB Purpura - Perfecto estado",
            "Caja original, todos los accesorios. Sin rasgunos.",
            750.0, TipoOferta.VENTA, carlos,
            "https://images.unsplash.com/photo-1678685888221-cda773a3dcdb?w=600");
        p1.setCategoria(catMoviles);   // <-- objeto Categoria, NO String
        p1.setMarca("Apple");
        p1.setModelo("iPhone 14 Pro");
        p1.setCondicion(CondicionProducto.COMO_NUEVO);
        p1.setAdmiteEnvio(true);
        p1.setPrecioEnvio(5.0);
        p1.setUbicacion("Madrid");
        productoRepository.save(p1);

        Producto p2 = new Producto(
            "Nike Air Max 90 Talla 42 - Usadas 3 veces",
            "Sin defectos. Caja original.",
            80.0, TipoOferta.VENTA, carlos,
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600");
        p2.setCategoria(catRopa);
        p2.setMarca("Nike");
        p2.setCondicion(CondicionProducto.MUY_BUEN_ESTADO);
        p2.setAdmiteEnvio(true);
        p2.setPrecioEnvio(4.0);
        productoRepository.save(p2);

        Producto p3 = new Producto(
            "Monitor Gaming LG 27GP850-B 27 165Hz",
            "6 meses de uso. Sin pixel muerto.",
            280.0, TipoOferta.VENTA, maria,
            "https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=600");
        p3.setCategoria(catInformatica);
        p3.setMarca("LG");
        p3.setCondicion(CondicionProducto.BUEN_ESTADO);
        p3.setAdmiteEnvio(true);
        p3.setPrecioEnvio(12.0);
        productoRepository.save(p3);

        // ---- Ofertas (chollometro) -----------------------------------
        Oferta o1 = new Oferta();
        o1.setTitulo("AirPods Pro 2a gen NUEVOS - Amazon");
        o1.setDescripcion("Precio historico minimo. Incluye estuche MagSafe USB-C.");
        o1.setPrecioOferta(199.0);
        o1.setPrecioOriginal(279.0);
        o1.setTienda("Amazon");
        o1.setUrlOferta("https://amazon.es/dp/MQDY3LL");
        o1.setCategoria(catAudio);          // objeto Categoria, NO String
        o1.setActor(carlos);
        o1.setSparkCount(42);
        o1.setDripCount(3);
        o1.setNumeroVistas(1200);
        o1.setNumeroCompartidos(87);
        o1.setEsActiva(true);
        o1.actualizarBadge();
        ofertaRepository.save(o1);

        Oferta o2 = new Oferta();
        o2.setTitulo("Roomba i5+ Robot Aspirador - El Corte Ingles");
        o2.setDescripcion("Minimo historico. Vaciado automatico. Envio gratis.");
        o2.setPrecioOferta(299.0);
        o2.setPrecioOriginal(549.0);
        o2.setTienda("El Corte Ingles");
        o2.setUrlOferta("https://elcorteingles.es");
        o2.setCategoria(catHogar);
        o2.setActor(maria);
        o2.setSparkCount(28);
        o2.setDripCount(1);
        o2.setNumeroVistas(890);
        o2.setNumeroCompartidos(44);
        o2.setEsActiva(true);
        o2.actualizarBadge();
        ofertaRepository.save(o2);

        Oferta o3 = new Oferta();
        o3.setTitulo("Windows 11 Pro OEM por 9.99 euros");
        o3.setDescripcion("Key digital oficial. Activacion inmediata.");
        o3.setPrecioOferta(9.99);
        o3.setPrecioOriginal(145.0);
        o3.setTienda("Kinguin");
        o3.setUrlOferta("https://kinguin.net");
        o3.setCategoria(catInformatica);
        o3.setActor(carlos);
        o3.setSparkCount(157);
        o3.setDripCount(12);
        o3.setNumeroVistas(4500);
        o3.setNumeroCompartidos(320);
        o3.setEsActiva(true);
        o3.actualizarBadge();
        ofertaRepository.save(o3);

        Oferta o4 = new Oferta();
        o4.setTitulo("Xiaomi Redmi Note 13 Pro 256GB - MediaMarkt");
        o4.setDescripcion("Mejor precio. AMOLED 200MP. 5G.");
        o4.setPrecioOferta(249.0);
        o4.setPrecioOriginal(399.0);
        o4.setTienda("MediaMarkt");
        o4.setUrlOferta("https://mediamarkt.es");
        o4.setCategoria(catMoviles);
        o4.setActor(maria);
        o4.setSparkCount(89);
        o4.setDripCount(5);
        o4.setNumeroVistas(2100);
        o4.setNumeroCompartidos(143);
        o4.setEsActiva(true);
        o4.actualizarBadge();
        ofertaRepository.save(o4);

        // ---- SparkVotos (constructor correcto) ----------------------
        // new SparkVoto(actor, oferta, esSpark)
        sparkVotoRepository.save(new SparkVoto(maria,  o1, true));
        sparkVotoRepository.save(new SparkVoto(carlos, o2, true));
        sparkVotoRepository.save(new SparkVoto(maria,  o3, true));
        sparkVotoRepository.save(new SparkVoto(carlos, o4, true));

        System.out.println("=== PopulateDB: datos de prueba creados correctamente ===");
    }

    // ---- Helpers --------------------------------------------------------

    private Categoria getOrCreate(String nombre, String slug, String icono,
                                   String color, int orden) {
        return categoriaRepository.findBySlug(slug).orElseGet(() -> {
            Categoria c = new Categoria(nombre, slug, icono);
            c.setColor(color);
            c.setOrden(orden);
            c.setActiva(true);
            return categoriaRepository.save(c);
        });
    }

    private Categoria getOrCreateHija(String nombre, String slug, String icono,
                                       Categoria parent) {
        return categoriaRepository.findBySlug(slug).orElseGet(() -> {
            Categoria c = new Categoria(nombre, slug, icono);
            c.setParent(parent);
            c.setActiva(true);
            c.setOrden(0);
            return categoriaRepository.save(c);
        });
    }
}