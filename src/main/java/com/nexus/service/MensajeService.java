package com.nexus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nexus.entity.Mensaje;
import com.nexus.entity.Producto;
import com.nexus.entity.Usuario;
import com.nexus.repository.MensajeRepository;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ProductoService productoService;

    // Obtener todos los mensajes (Admin)
    public List<Mensaje> findAll() {
        return mensajeRepository.findAll();
    }

    // Buscar mensaje por ID
    public Optional<Mensaje> findById(Integer id) {
        return mensajeRepository.findById(id);
    }

    // Obtener mensajes de un producto (Chat) ordenados por los más recientes
    public List<Mensaje> findByProductoId(Integer productoId) {
        return mensajeRepository.findByProductoIdOrderByFechaCreacionDesc(productoId);
    }

    // Guardar/Enviar un nuevo mensaje
    public Mensaje save(Mensaje mensaje, Integer usuarioId, Integer productoId) {
        Optional<Usuario> oUsuario = usuarioService.findById(usuarioId);
        Optional<Producto> oProducto = productoService.findById(productoId);

        if (oUsuario.isPresent() && oProducto.isPresent()) {
            mensaje.setUsuario(oUsuario.get());
            mensaje.setProducto(oProducto.get());
            
            // Aseguramos valores por defecto si no vienen
            if (mensaje.getFechaCreacion() == null) {
                mensaje.setFechaCreacion(LocalDateTime.now());
            }
            if (!mensaje.isEstaActivo()) {
                mensaje.setEstaActivo(true);
            }
            
            return mensajeRepository.save(mensaje);
        }
        
        return null; // O lanzar una excepción personalizada
    }

    // Eliminado lógico (desactivar mensaje) o físico
    public void delete(Integer id) {
        mensajeRepository.deleteById(id);
    }
}