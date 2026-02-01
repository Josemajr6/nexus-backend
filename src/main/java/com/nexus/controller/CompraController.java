package com.nexus.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.entity.Compra;
import com.nexus.entity.EstadoCompra;
import com.nexus.entity.Producto;
import com.nexus.entity.Usuario;
import com.nexus.repository.CompraRepository;
import com.nexus.service.ProductoService;
import com.nexus.service.StripeService;
import com.nexus.service.UsuarioService;
import com.stripe.model.PaymentIntent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compra")
@Tag(name = "Compras", description = "Gestión de transacciones y Pagos con Stripe")
public class CompraController {

    @Autowired private CompraRepository compraRepository;
    @Autowired private ProductoService productoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private StripeService stripeService;

    @GetMapping("/historial/{usuarioId}")
    @Operation(summary = "Obtener historial de compras de un usuario")
    public List<Compra> historial(@PathVariable Integer usuarioId) {
        return compraRepository.findAll().stream()
                .filter(c -> c.getComprador().getId() == usuarioId)
                .toList();
    }

    // --- INICIAR PAGO (Stripe) ---
    @PostMapping("/intent")
    @Operation(summary = "Iniciar proceso de pago seguro (Stripe PaymentIntent)")
    public ResponseEntity<?> crearIntentoPago(@RequestParam Integer productoId, @RequestParam Integer compradorId) {
        Optional<Producto> p = productoService.findById(productoId);
        Optional<Usuario> u = usuarioService.findById(compradorId);
        
        if (p.isEmpty() || u.isEmpty()) {
            return ResponseEntity.badRequest().body("Producto o Comprador no válidos");
        }
        
        try {
            // 1. Crear PaymentIntent en Stripe
            PaymentIntent intent = stripeService.crearIntentoPago(p.get().getPrecio(), "Compra Nexus: " + p.get().getTitulo());
            
            // 2. Guardar Compra PENDIENTE en BD
            Compra compra = new Compra();
            compra.setComprador(u.get());
            compra.setProducto(p.get());
            compra.setFechaCompra(LocalDateTime.now());
            compra.setEstado(EstadoCompra.PENDIENTE); 
            compraRepository.save(compra);
            
            // 3. Devolver el Secret al Frontend/Postman
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("compraId", compra.getId());
            response.put("precio", p.get().getPrecio());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error conectando con Stripe: " + e.getMessage());
        }
    }
}