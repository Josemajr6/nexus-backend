package com.nexus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexus.entity.EstadoProducto;
import com.nexus.entity.Producto;
import com.nexus.entity.Usuario;
import com.nexus.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Integer id) {
        return productoRepository.findById(id);
    }

    public List<Producto> findDisponibles() {
        return productoRepository.findByEstadoProducto(EstadoProducto.DISPONIBLE);
    }

    public Producto publicar(Producto producto, Integer usuarioId) {
        Optional<Usuario> oUsuario = usuarioService.findById(usuarioId);
        
        if (oUsuario.isEmpty()) {
            return null;
        }

        Usuario usuario = oUsuario.get();
        producto.setPublicador(usuario);
        producto.setEstadoProducto(EstadoProducto.DISPONIBLE);
        
        return productoRepository.save(producto);
    }

    public Producto update(Integer id, Producto productoDetalles) {
        Optional<Producto> oProducto = productoRepository.findById(id);

        if (oProducto.isPresent()) {
            Producto producto = oProducto.get();
            
            producto.setTitulo(productoDetalles.getTitulo());
            producto.setDescripcion(productoDetalles.getDescripcion());
            producto.setPrecio(productoDetalles.getPrecio());
            producto.setTipoOferta(productoDetalles.getTipoOferta());
            
     
            return productoRepository.save(producto);
        }
        
        return null;
    }

    public void delete(Integer id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
        }
    }
}