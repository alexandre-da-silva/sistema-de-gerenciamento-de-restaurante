package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/cliente/pedidos")
public class ClientePedidoController {
    private final PedidoService pedidos;
    public ClientePedidoController(PedidoService pedidos) { this.pedidos = pedidos; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PedidoResponse criar(@Valid @RequestBody PedidoRequest request, Authentication auth) {
        return pedidos.criar(auth.getName(), request);
    }
    @GetMapping public List<PedidoResponse> meusPedidos(Authentication auth) { return pedidos.meusPedidos(auth.getName()); }
}
