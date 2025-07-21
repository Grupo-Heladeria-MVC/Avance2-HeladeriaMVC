package com.utp.controller;

import com.utp.model.*;
import com.utp.service.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//Pasarela de pago con tarjeta
@Controller
public class PagoTarjetaController {


    @Autowired
    private PedidoService pedidoService;
    
     //Para almacenar los detalles del Pedido
    List<DetallePedido> detalles = new ArrayList<DetallePedido>();

    //Detalles del pedido    
    Pedido pedido = new Pedido();

    @PostMapping("/tarjeta/procesar")
    public String procesarPagoTarjeta(
            @RequestParam Integer pedidoId,
            @RequestParam String numeroTarjeta,
            @RequestParam String nombreTarjeta,
            @RequestParam String fechaExpiracion,
            @RequestParam String cvv,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Validaciones básicas de tarjeta (puedes expandir estas)
            if (!validarTarjeta(numeroTarjeta, nombreTarjeta, fechaExpiracion, cvv)) {
                redirectAttributes.addFlashAttribute("error", "Datos de tarjeta inválidos");
                return "redirect:/pagos/tarjeta/" + pedidoId;
            }

            // Simulación de pago (en un sistema real, integrarías con pasarela de pagos)
            Pedido pedido = pedidoService.findById(pedidoId);
            pedido.setMetodoPago(MetodoPago.TARJETA_CREDITO);
            pedido.setEstado(EstadoPedido.PAGADO);

            // Guardar detalles de pago
            PagoPedido pago = new PagoPedido();
            pago.setMontoPagado(pedido.getTotal());
            pago.setFechaPago(new Date());

            pedidoService.save(pedido);

            redirectAttributes.addFlashAttribute("success", "Pago realizado con éxito");
            return "redirect:/pedidos/mis-pedidos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago");
            return "redirect:/pagos/tarjeta/" + pedidoId;
        }
    }

    // Método de validación básica (expandir según necesidades)
    private boolean validarTarjeta(String numero, String nombre, String fecha, String cvv) {
        // Validaciones básicas de ejemplo
        return numero != null && numero.length() == 16
                && nombre != null && !nombre.isEmpty()
                && cvv != null && cvv.length() == 3;
    }
}
