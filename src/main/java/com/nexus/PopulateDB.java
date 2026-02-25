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
@Component
public class PopulateDB implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired private ActorRepository     actorRepository;
    @Autowired private UsuarioRepository   usuarioRepository;
    @Autowired private ProductoRepository  productoRepository;
    @Autowired private OfertaRepository    ofertaRepository;
    @Autowired private SparkVotoRepository sparkVotoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private PasswordEncoder     passwordEncoder;
    private boolean done=false;

    @Override @Transactional
    public void onApplicationEvent(ContextRefreshedEvent e){
        if(done||actorRepository.count()>0){done=true;return;} done=true;

        // CATEGORIAS
        Categoria el=cat("Electronica","electronica","devices","#1565C0",null,1);
        Categoria ro=cat("Ropa","ropa","checkroom","#6A1B9A",null,2);
        Categoria ho=cat("Hogar","hogar","home","#2E7D32",null,3);
        Categoria ve=cat("Vehiculos","vehiculos","directions_car","#1976D2",null,4);
        Categoria in=cat("Informatica","informatica","laptop","#00838F",null,5);
        Categoria jg=cat("Videojuegos","videojuegos","sports_esports","#7B1FA2",null,6);
        Categoria mo=cat("Moviles","moviles","smartphone","#1565C0",el,0);
        Categoria au=cat("Audio","audio","headphones","#1565C0",el,1);
        Categoria tv=cat("TV y Video","tv-video","tv","#1565C0",el,2);
        Categoria pc=cat("PC y Portatiles","pcs","computer","#00838F",in,0);
        Categoria sw=cat("Software","software","code","#00838F",in,1);

        // USUARIOS
        Usuario carlos=u("carlos_vendedor","carlos@nexus.test","Vendo electronica.","Madrid");
        Usuario maria=u("maria_nexus","maria@nexus.test","Cazadora de chollos.","Barcelona");
        Usuario pedro=u("pedro_games","pedro@nexus.test","Gamer","Valencia");

        // ADMIN -- Admin NO es Usuario: usar actorRepository
        Admin admin=new Admin();
        admin.setUser("admin");
        admin.setEmail("admin@nexus.test");
        admin.setPassword(passwordEncoder.encode("admin2026!"));
        admin.setCuentaVerificada(true);
        admin.setNivelAcceso(3);
        actorRepository.save(admin);

        // PRODUCTOS -- setCategoria recibe Categoria objeto, NUNCA String
        Producto p1=new Producto("iPhone 14 Pro 128GB","Perfecto estado.",750.0,TipoOferta.VENTA,carlos,"https://images.unsplash.com/photo-1678685888221-cda773a3dcdb?w=800");
        p1.setCategoria(mo); p1.setMarca("Apple"); p1.setCondicion(CondicionProducto.COMO_NUEVO);
        p1.setAdmiteEnvio(true); p1.setPrecioEnvio(5.0); p1.setUbicacion("Madrid"); p1.setPrecioNegociable(false);
        productoRepository.save(p1);

        Producto p2=new Producto("Nike Air Max 90 T42","Practicamente nuevas.",80.0,TipoOferta.VENTA,carlos,"https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800");
        p2.setCategoria(ro); p2.setMarca("Nike"); p2.setCondicion(CondicionProducto.MUY_BUEN_ESTADO);
        p2.setAdmiteEnvio(true); p2.setPrecioEnvio(4.0); p2.setPrecioNegociable(true);
        productoRepository.save(p2);

        Producto p3=new Producto("Monitor LG 27GP850 27\" 165Hz","6 meses de uso.",290.0,TipoOferta.VENTA,maria,"https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=800");
        p3.setCategoria(pc); p3.setMarca("LG"); p3.setCondicion(CondicionProducto.BUEN_ESTADO);
        p3.setAdmiteEnvio(true); p3.setPrecioEnvio(15.0); p3.setPrecioNegociable(false);
        productoRepository.save(p3);

        Producto p4=new Producto("PS5 + 3 Juegos","Todo perfecto.",450.0,TipoOferta.VENTA,pedro,"https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800");
        p4.setCategoria(jg); p4.setMarca("Sony"); p4.setCondicion(CondicionProducto.COMO_NUEVO);
        p4.setAdmiteEnvio(false); p4.setUbicacion("Valencia"); p4.setPrecioNegociable(true);
        productoRepository.save(p4);

        Producto p5=new Producto("Sony WH-1000XM5","4 meses de uso.",220.0,TipoOferta.VENTA,maria,"https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=800");
        p5.setCategoria(au); p5.setMarca("Sony"); p5.setCondicion(CondicionProducto.COMO_NUEVO);
        p5.setAdmiteEnvio(true); p5.setPrecioEnvio(6.0); p5.setPrecioNegociable(false);
        productoRepository.save(p5);

        // OFERTAS
        Oferta o1=o("AirPods Pro 2a Gen - Minimo historico","MagSafe USB-C.",199.0,279.0,"Amazon","https://amazon.es",au,carlos,"https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800",42,3,1200,87);
        Oferta o2=o("Roomba i5+ - El Corte Ingles","Vaciado automatico.",299.0,549.0,"El Corte Ingles","https://elcorteingles.es",ho,maria,"https://images.unsplash.com/photo-1589782431773-c7b9ad4c37b2?w=800",28,1,890,44);
        Oferta o3=o("Windows 11 Pro OEM por 9.99","Key digital.",9.99,145.0,"Kinguin","https://kinguin.net",sw,carlos,"https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800",157,12,4500,320);
        Oferta o4=o("Xiaomi Redmi Note 13 Pro 256GB 5G","200MP.",249.0,399.0,"MediaMarkt","https://mediamarkt.es",mo,maria,"https://images.unsplash.com/photo-1591337676887-a217a6970a8a?w=800",89,5,2100,143);
        Oferta o5=o("PS Plus Essential 12m por 39.99","Codigo digital.",39.99,71.99,"PlayStation Store","https://store.playstation.com",jg,pedro,"https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800",63,2,1800,95);
        Oferta o6=o("Samsung QLED 55\" 4K 2024","120Hz.",499.0,799.0,"PcComponentes","https://pccomponentes.com",tv,carlos,"https://images.unsplash.com/photo-1593359677879-a4bb92f4834c?w=800",35,4,1100,72);

        sparkVotoRepository.save(new SparkVoto(maria,o1,true)); sparkVotoRepository.save(new SparkVoto(pedro,o1,true));
        sparkVotoRepository.save(new SparkVoto(carlos,o2,true)); sparkVotoRepository.save(new SparkVoto(pedro,o2,true));
        sparkVotoRepository.save(new SparkVoto(maria,o3,true)); sparkVotoRepository.save(new SparkVoto(pedro,o3,true));
        sparkVotoRepository.save(new SparkVoto(carlos,o4,true)); sparkVotoRepository.save(new SparkVoto(carlos,o5,true));
        sparkVotoRepository.save(new SparkVoto(maria,o6,true));
        System.out.println("=== PopulateDB OK ===");
    }

    private Categoria cat(String nombre,String slug,String icono,String color,Categoria parent,int orden){
        return categoriaRepository.findBySlug(slug).orElseGet(()->{
            Categoria c=new Categoria(nombre,slug,icono); c.setColor(color); c.setOrden(orden); c.setActiva(true);
            if(parent!=null)c.setParent(parent); return categoriaRepository.save(c);
        });
    }
    private Usuario u(String user,String email,String bio,String ubicacion){
        Usuario u=new Usuario(); u.setUser(user); u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123")); u.setCuentaVerificada(true);
        u.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed="+user);
        u.setBiografia(bio); u.setUbicacion(ubicacion); u.setPerfilPublico(true);
        return usuarioRepository.save(u);
    }
    private Oferta o(String titulo,String desc,double precio,double orig,String tienda,String url,
                     Categoria cat,Actor actor,String img,int sparks,int drips,int vistas,int comp){
        Oferta of=new Oferta(); of.setTitulo(titulo); of.setDescripcion(desc);
        of.setPrecioOferta(precio); of.setPrecioOriginal(orig); of.setTienda(tienda);
        of.setUrlOferta(url);       // urlOferta, no urlExterna
        of.setCategoria(cat);       // objeto Categoria, nunca String
        of.setActor(actor); of.setImagenPrincipal(img);
        of.setSparkCount(sparks); of.setDripCount(drips);
        of.setNumeroVistas(vistas); of.setNumeroCompartidos(comp);
        of.setEsActiva(true); of.setFechaPublicacion(LocalDateTime.now().minusHours((long)(Math.random()*72)));
        of.actualizarBadge(); return ofertaRepository.save(of);
    }
}
