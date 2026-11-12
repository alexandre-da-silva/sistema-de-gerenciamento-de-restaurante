package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.StatusPedido;
import br.com.uchub.restaurante.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/cozinha/pedidos")
public class CozinhaController {
    private final PedidoService pedidos;
    public CozinhaController(PedidoService pedidos) { this.pedidos = pedidos; }
    @GetMapping public List<PedidoResponse> listar(@RequestParam(required=false) StatusPedido status) { return pedidos.listarCozinha(status); }
    @PatchMapping("/{id}/status") public PedidoResponse atualizarStatus(@PathVariable Long id, @Valid @RequestBody StatusPedidoRequest request) {
        return pedidos.alterarStatus(id, request.status());
    }
}
