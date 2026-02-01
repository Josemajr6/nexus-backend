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
        // Filtramos usando stream ya que en el repositorio base findAll trae todo
        return compraRepository.findAll().stream()
                .filter(c -> c.getComprador().getId() == usuarioId)
                .toList();
    }

    public Optional<Compra> findById(Integer id) {
        return compraRepository.findById(id);
    }

    // --- NUEVO: CONFIRMAR COMPRA (Tras pago exitoso en Stripe) ---
    @Transactional
    public Compra confirmarCompra(Integer compraId) {
        Optional<Compra> oCompra = compraRepository.findById(compraId);
        
        if (oCompra.isPresent()) {
            Compra compra = oCompra.get();
            Producto producto = compra.getProducto();
            
            // Verificamos si sigue disponible
            if (producto.getEstadoProducto() != EstadoProducto.DISPONIBLE) {
                throw new IllegalStateException("Error: El producto ya ha sido vendido.");
            }

            // 1. Cambiar estado de la compra a COMPLETADA
            compra.setEstado(EstadoCompra.COMPLETADA);
            
            // 2. Marcar producto como VENDIDO
            producto.setEstadoProducto(EstadoProducto.VENDIDO);
            productoRepository.save(producto);
            
            return compraRepository.save(compra);
        }
        return null;
    }

    // --- PROCESAR COMPRA DIRECTA (Lógica antigua corregida) ---
    // Útil si decides hacer compras sin pasarela de pago en el futuro
    @Transactional
    public Compra procesarCompraDirecta(Integer productoId, Integer compradorId) {
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
        nuevaCompra.setFechaCompra(LocalDateTime.now());
        

        nuevaCompra.setEstado(EstadoCompra.COMPLETADA); 
        
        producto.setEstadoProducto(EstadoProducto.VENDIDO);
        productoRepository.save(producto);

        return compraRepository.save(nuevaCompra);
    }
}