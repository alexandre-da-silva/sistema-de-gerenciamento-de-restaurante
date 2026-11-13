package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final MesaRepository mesas;
    private final RecursoService recursos;
    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios, ProdutoRepository produtos,
                         MesaRepository mesas, RecursoService recursos) {
        this.pedidos = pedidos; this.usuarios = usuarios; this.produtos = produtos;
        this.mesas = mesas; this.recursos = recursos;
    }
    @Transactional
    public PedidoResponse criar(String emailCliente, PedidoRequest request) {
        recursos.exigirHabilitado("PEDIDOS_ONLINE");
        Usuario cliente = usuarios.findByEmail(emailCliente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        if(cliente.getPerfil()!=PerfilUsuario.CLIENTE || !cliente.isAtivo())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Somente clientes podem criar pedidos.");
        var anterior=pedidos.findByClienteEmailAndReferencia(emailCliente,request.referencia());
        if(anterior.isPresent())return toResponse(anterior.get());
        if (request.mesaId() != null) recursos.exigirHabilitado("MESAS_E_COMANDAS");
        Mesa mesa = request.mesaId() == null ? null : mesas.findById(request.mesaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesa não encontrada."));
        if (mesa != null && !"LIVRE".equals(mesa.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Mesa não está disponível.");
        Pedido pedido = new Pedido(cliente, mesa, request.observacao());
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest itemRequest : request.itens()) {
            if (itemRequest.quantidade() == null || itemRequest.quantidade() < 1 || itemRequest.quantidade() > 99)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade deve ficar entre 1 e 99.");
            Produto produto = produtos.findById(itemRequest.produtoId())
                    .filter(Produto::isAtivo)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inexistente ou indisponível."));
            ItemPedido item = new ItemPedido(produto, itemRequest.quantidade());
            pedido.adicionarItem(item);
            total = total.add(item.getSubtotal());
        }
        BigDecimal taxa=Boolean.TRUE.equals(request.taxaServico()) ? total.multiply(new BigDecimal("0.10")).setScale(2,java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        pedido.definirCheckout(taxa,request.referencia());
        pedido.definirTotal(total.add(taxa));
        if (mesa != null) mesa.setStatus("OCUPADA");
        return toResponse(pedidos.save(pedido));
    }
    @Transactional(readOnly = true)
    public List<PedidoResponse> meusPedidos(String email) {
        return pedidos.findByClienteEmailOrderByCriadoEmDesc(email).stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarCozinha(StatusPedido status) {
        recursos.exigirHabilitado("PAINEL_COZINHA");
        List<Pedido> lista = status == null ? pedidos.findAllByOrderByCriadoEmDesc() : pedidos.findByStatusOrderByCriadoEmAsc(status);
        return lista.stream().filter(p -> p.getStatus() != StatusPedido.ENTREGUE && p.getStatus() != StatusPedido.CANCELADO)
                .map(this::toResponse).toList();
    }
    @Transactional
    public PedidoResponse alterarStatus(Long id, StatusPedido novoStatus) {
        recursos.exigirHabilitado("PAINEL_COZINHA");
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        StatusPedido atual = pedido.getStatus();
        boolean permitido = switch (atual) {
            case RECEBIDO -> novoStatus == StatusPedido.EM_PREPARO || novoStatus == StatusPedido.CANCELADO;
            case EM_PREPARO -> novoStatus == StatusPedido.PRONTO || novoStatus == StatusPedido.CANCELADO;
            case PRONTO -> novoStatus == StatusPedido.ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
        if (!permitido) throw new ResponseStatusException(HttpStatus.CONFLICT, "Transição de status não permitida.");
        pedido.atualizarStatus(novoStatus);
        if (pedido.getMesa() != null && (novoStatus == StatusPedido.CANCELADO)) {
            pedido.getMesa().setStatus("LIVRE");
        }
        return toResponse(pedido);
    }
    @Transactional
    public PedidoResponse cancelarCliente(String email, Long id) {
        Pedido pedido=pedidos.findById(id).filter(p -> p.getCliente().getEmail().equals(email))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Pedido não encontrado."));
        if(pedido.getStatus()!=StatusPedido.RECEBIDO)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Só é possível cancelar antes de iniciar o preparo.");
        pedido.atualizarStatus(StatusPedido.CANCELADO);
        if(pedido.getMesa()!=null)pedido.getMesa().setStatus("LIVRE");
        return toResponse(pedido);
    }
    private PedidoResponse toResponse(Pedido p) {
        List<PedidoResponse.Item> itens = p.getItens().stream().map(i -> new PedidoResponse.Item(
                i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(),
                i.getPrecoUnitario(), i.getSubtotal(),i.getProduto().getImagemUrl())).toList();
        return new PedidoResponse(p.getId(), p.getCliente().getNome(),
                p.getMesa() == null ? null : p.getMesa().getNumero(), p.getStatus(),
                p.getTotal(), p.getObservacao(), p.getCriadoEm(), itens,p.getTaxaServico(),p.getAtualizadoEm(),p.getEtapas(),
                p.getItens().stream().map(i -> i.getProduto().getTempoPreparo()==null?20:i.getProduto().getTempoPreparo()).max(Integer::compare).orElse(20));
    }
}
