package br.com.uchub.restaurante;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.service.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {
    final PedidoRepository pedidos = mock(PedidoRepository.class);
    final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    final ProdutoRepository produtos = mock(ProdutoRepository.class);
    final MesaRepository mesas = mock(MesaRepository.class);
    final RecursoService recursos = mock(RecursoService.class);
    final ProdutoDetalhesRepository detalhes = mock(ProdutoDetalhesRepository.class);
    final EstoquePedidoService estoque = mock(EstoquePedidoService.class);
    final PedidoService service = new PedidoService(pedidos, usuarios, produtos, mesas, recursos, detalhes, estoque);
    final Usuario cliente = new Usuario("Cliente", "cliente@example.com", "hash", null, PerfilUsuario.CLIENTE);
    final String referencia = "12345678-1234-1234-1234-123456789abc";

    PedidoRequest request(boolean taxa) {
        return new PedidoRequest(null, List.of(new ItemPedidoRequest(1L, 2, null, List.of(), null, new BigDecimal("34.90"))), "Sem cebola", taxa, referencia);
    }
    Produto produto() {
        Produto produto = spy(new Produto("Prato", "Descrição", new BigDecimal("49.90"), null));
        doReturn(1L).when(produto).getId();
        produto.apresentar(null, 15, "Carne", true, new BigDecimal("34.90"));
        return produto;
    }
    @BeforeEach void setup() {
        when(usuarios.findByEmailForUpdate(cliente.getEmail())).thenReturn(Optional.of(cliente));
        when(pedidos.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(detalhes.horarios(anyLong())).thenReturn(List.of());
        when(detalhes.variacoes(anyLong())).thenReturn(List.of());
    }
    @Test void priceAndOptionalFeeAreCalculatedFromCatalog() {
        Produto prato=produto();
        when(produtos.findById(1L)).thenReturn(Optional.of(prato));
        PedidoResponse resposta = service.criar(cliente.getEmail(), request(true));
        assertEquals(new BigDecimal("76.78"), resposta.total());
        assertEquals(new BigDecimal("6.98"), resposta.taxaServico());
        assertEquals(new BigDecimal("34.90"), resposta.itens().getFirst().precoUnitario());
        verify(estoque).baixar(any(Pedido.class));
    }
    @Test void adminCannotOrderEvenWhenServiceCalledDirectly() {
        Usuario admin = new Usuario("Admin", "admin@example.com", "hash", null, PerfilUsuario.ADMIN);
        when(usuarios.findByEmailForUpdate(admin.getEmail())).thenReturn(Optional.of(admin));
        assertEquals(403,assertThrows(ResponseStatusException.class,
            () -> service.criar(admin.getEmail(),request(false))).getStatusCode().value());
        verifyNoInteractions(produtos, pedidos);
    }
    @Test void repeatedReferenceReturnsOriginalOrderWhenPayloadIsIdentical() throws Exception {
        Pedido existing = new Pedido(cliente, null, null);
        existing.definirTotal(new BigDecimal("10.00"));
        existing.definirRequestHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(request(false).toString().getBytes(StandardCharsets.UTF_8))));
        when(pedidos.findByClienteEmailAndReferencia(cliente.getEmail(),referencia))
            .thenReturn(Optional.of(existing));
        assertEquals(new BigDecimal("10.00"),service.criar(cliente.getEmail(),request(false)).total());
        verify(pedidos,never()).saveAndFlush(any());
        verifyNoInteractions(produtos);
    }
    @Test void repeatedReferenceWithDifferentPayloadIsRejected() {
        Pedido existing = new Pedido(cliente, null, null);
        existing.definirRequestHash("outro-hash");
        when(pedidos.findByClienteEmailAndReferencia(cliente.getEmail(),referencia))
            .thenReturn(Optional.of(existing));
        assertEquals(409,assertThrows(ResponseStatusException.class,
            () -> service.criar(cliente.getEmail(),request(true))).getStatusCode().value());
        verify(pedidos,never()).saveAndFlush(any());
    }
    @Test void checkoutRejectsPriceChangeInsteadOfChargingItSilently() {
        Produto prato=produto();
        when(produtos.findById(1L)).thenReturn(Optional.of(prato));
        var changed=new PedidoRequest(null,List.of(new ItemPedidoRequest(1L,1,null,List.of(),null,
                new BigDecimal("29.90"))),null,false,referencia);
        assertEquals(409,assertThrows(ResponseStatusException.class,
                () -> service.criar(cliente.getEmail(),changed)).getStatusCode().value());
        verify(pedidos,never()).saveAndFlush(any());
    }
    @Test void anotherCustomerCannotCancel() {
        when(pedidos.findById(1L)).thenReturn(Optional.of(new Pedido(cliente,null,null)));
        assertEquals(404,assertThrows(ResponseStatusException.class,
            () -> service.cancelarCliente("other@example.com",1L)).getStatusCode().value());
    }
    @Test void preparationBlocksCustomerCancellation() {
        Pedido pedido = new Pedido(cliente,null,null);pedido.atualizarStatus(StatusPedido.EM_PREPARO);
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        assertEquals(409,assertThrows(ResponseStatusException.class,
            () -> service.cancelarCliente(cliente.getEmail(),1L)).getStatusCode().value());
        assertEquals(StatusPedido.EM_PREPARO,pedido.getStatus());
    }
    @Test void receivedOrderCanBeCancelled() {
        Pedido pedido=new Pedido(cliente,null,null);
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        assertEquals(StatusPedido.CANCELADO,service.cancelarCliente(cliente.getEmail(),1L).status());
        assertTrue(pedido.getEtapas().containsKey("CANCELADO"));
    }

    @Test void deliveryReleasesTableWhenCashierDisabled() {
        Mesa mesa=mock(Mesa.class);
        Pedido pedido=new Pedido(cliente,mesa,null);pedido.atualizarStatus(StatusPedido.PRONTO);
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(recursos.habilitado("CAIXA_E_PAGAMENTOS")).thenReturn(false);
        assertEquals(StatusPedido.ENTREGUE,service.alterarStatus(1L,StatusPedido.ENTREGUE).status());
        verify(mesa).setStatus("LIVRE");
    }
    @Test void deliveryKeepsTableUntilPaymentWhenCashierEnabled() {
        Mesa mesa=mock(Mesa.class);
        Pedido pedido=new Pedido(cliente,mesa,null);pedido.atualizarStatus(StatusPedido.PRONTO);
        when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(recursos.habilitado("CAIXA_E_PAGAMENTOS")).thenReturn(true);
        service.alterarStatus(1L,StatusPedido.ENTREGUE);
        verify(mesa,never()).setStatus(anyString());
    }
}
