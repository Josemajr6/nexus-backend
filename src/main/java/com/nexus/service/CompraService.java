package com.nexus.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.entity.Compra;
import com.nexus.entity.EstadoCompra;
import com.nexus.entity.EstadoProducto;
import com.nexus.entity.Producto;
import com.nexus.entity.Usuario;
import com.nexus.repository.CompraRepository;
import com.nexus.repository.ProductoRepository;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    public List<Compra> findAll() {
        return compraRepository.findAll();
    }

    public List<Compra> findHistorialUsuario(Integer usuarioId) {
        Optional<Usuario> oUsuario = usuarioService.findById(usuarioId);
        
        if (oUsuario.isPresent()) {
            return compraRepository.findByComprador(oUsuario.get());
        } else {
            return new ArrayList<>();
        }
    }

    public Optional<Compra> findById(Integer id) {
        return compraRepository.findById(id);
    }

    @Transactional
    public Compra procesarCompra(Integer productoId, Integer compradorId) {
        Optional<Producto> oProducto = productoRepository.findById(productoId);
        Optional<Usuario> oComprador = usuarioService.findById(compradorId);

        if (oProducto.isEmpty() || oComprador.isEmpty()) {
            return null;
        }

        Producto producto = oProducto.get();
        Usuario comprador = oComprador.get();

        if (producto.getEstadoProducto() != EstadoProducto.DISPONIBLE) {
            throw new IllegalStateException("El producto ya no está disponible.");
        }

        if (producto.getPublicador().getId() == comprador.getId()) {
            throw new IllegalArgumentException("No puedes comprar tu propio producto.");
        }

        Compra nuevaCompra = new Compra();
        nuevaCompra.setProducto(producto);
        nuevaCompra.setComprador(comprador);
        nuevaCompra.setPrecioFinal(producto.getPrecio());
        nuevaCompra.setFechaCompra(LocalDateTime.now());
        nuevaCompra.setEstadoCompra(EstadoCompra.ACEPTADO);
        producto.setEstadoProducto(EstadoProducto.VENDIDO);
        productoRepository.save(producto);

        return compraRepository.save(nuevaCompra);
    }
}