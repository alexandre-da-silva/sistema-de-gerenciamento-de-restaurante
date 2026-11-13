package br.com.uchub.restaurante.controller;

import br.com.uchub.restaurante.repository.PainelRepository;
import br.com.uchub.restaurante.repository.RecursoSistemaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.PedidoService;
import br.com.uchub.restaurante.repository.UsuarioRepository;
import br.com.uchub.restaurante.model.PerfilUsuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin")
public class PainelController {
    private final PainelRepository painel;
    private final RecursoSistemaRepository recursos;
    private final PedidoService pedidos;
    private final UsuarioRepository usuarios;
    public PainelController(PainelRepository painel, RecursoSistemaRepository recursos, PedidoService pedidos, UsuarioRepository usuarios) {
        this.painel=painel; this.recursos=recursos; this.pedidos=pedidos; this.usuarios=usuarios;
    }
    public record NovoPedido(@NotNull Long clienteId, Long mesaId,
        @NotEmpty List<@Valid ItemPedidoRequest> itens, @Size(max=1000) String observacao) {}
    @PostMapping("/pedidos")
    public PedidoResponse criar(@Valid @RequestBody NovoPedido request) {
        var cliente=usuarios.findById(request.clienteId())
            .filter(u -> u.isAtivo() && u.getPerfil()==PerfilUsuario.CLIENTE)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecione um cliente ativo."));
        return pedidos.criar(cliente.getEmail(), new PedidoRequest(request.mesaId(),request.itens(),request.observacao()));
    }
    @GetMapping("/dashboard")
    public Map<String,Object> dashboard(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate data) {
        return painel.resumo(data==null ? LocalDate.now(ZoneId.of("America/Sao_Paulo")) : data, habilitado("CAIXA_E_PAGAMENTOS"), habilitado("CONTROLE_ESTOQUE"));
    }
    @GetMapping("/pedidos/detalhes")
    public List<Map<String,Object>> pedidos() { return painel.pedidos(); }
    @GetMapping("/clientes")
    public List<Map<String,Object>> clientes() { return painel.clientes(); }
    private boolean habilitado(String codigo) { return recursos.findByCodigo(codigo).map(r -> r.isHabilitado()).orElse(false); }
}
