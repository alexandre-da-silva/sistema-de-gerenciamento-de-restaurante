package br.com.uchub.restaurante;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.service.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {
 final PedidoRepository pedidos=mock(PedidoRepository.class);
 final UsuarioRepository usuarios=mock(UsuarioRepository.class);
 final ProdutoRepository produtos=mock(ProdutoRepository.class);
 final MesaRepository mesas=mock(MesaRepository.class);
 final RecursoService recursos=mock(RecursoService.class);
 final CatalogoOperacionalService catalogo=mock(CatalogoOperacionalService.class);
 final ConsumoEstoqueService estoque=mock(ConsumoEstoqueService.class);
 final PedidoService service=new PedidoService(pedidos,usuarios,produtos,mesas,recursos,catalogo,estoque);
 final Usuario cliente=new Usuario("Cliente","cliente@example.com","hash",null,PerfilUsuario.CLIENTE);
 final String reference="12345678-1234-1234-1234-123456789abc";
 PedidoRequest request(boolean fee){return new PedidoRequest(null,List.of(new ItemPedidoRequest(1L,2)),"Sem cebola",fee,reference);}
 @BeforeEach void setup(){when(usuarios.findByEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));org.springframework.test.util.ReflectionTestUtils.setField(cliente,"id",1L);when(pedidos.saveAndFlush(any())).thenAnswer(a->{Pedido p=a.getArgument(0);org.springframework.test.util.ReflectionTestUtils.setField(p,"id",10L);return p;});when(catalogo.escolher(any(),any())).thenAnswer(a->{Produto p=a.getArgument(0);return new CatalogoOperacionalService.Escolha(p.getPrecoVenda(),"","Cozinha",Map.of());});}
 @Test void priceAndOptionalFeeAreCalculatedFromCatalog(){
  Produto p=new Produto("Prato","Descrição",new BigDecimal("49.90"),null);
  p.apresentar(null,15,"Carne",true,new BigDecimal("34.90"));
  when(produtos.findById(1L)).thenReturn(Optional.of(p));
  PedidoResponse result=service.criar(cliente.getEmail(),request(true));
  assertEquals(new BigDecimal("76.78"),result.total());
  assertEquals(new BigDecimal("6.98"),result.taxaServico());
  assertEquals(new BigDecimal("34.90"),result.itens().getFirst().precoUnitario());
 }
 @Test void adminCannotOrderEvenWhenServiceCalledDirectly(){
  Usuario admin=new Usuario("Admin","admin@example.com","hash",null,PerfilUsuario.ADMIN);
  when(usuarios.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
  assertEquals(403,assertThrows(ResponseStatusException.class,()->service.criar(admin.getEmail(),request(false))).getStatusCode().value());
  verifyNoInteractions(produtos,pedidos);
 }
 @Test void repeatedReferenceReturnsOriginalOrder(){
  Pedido existing=new Pedido(cliente,null,null);existing.definirTotal(new BigDecimal("10.00"));
  when(pedidos.findByClienteEmailAndReferencia(cliente.getEmail(),reference)).thenReturn(Optional.of(existing));
  assertEquals(new BigDecimal("10.00"),service.criar(cliente.getEmail(),request(false)).total());
  verify(pedidos,never()).saveAndFlush(any());verifyNoInteractions(produtos);
 }
 @Test void anotherCustomerCannotCancel(){
  when(pedidos.findById(1L)).thenReturn(Optional.of(new Pedido(cliente,null,null)));
  assertEquals(404,assertThrows(ResponseStatusException.class,()->service.cancelarCliente("other@example.com",1L)).getStatusCode().value());
 }
 @Test void preparationBlocksCustomerCancellation(){
  Pedido order=new Pedido(cliente,null,null);order.atualizarStatus(StatusPedido.EM_PREPARO);
  when(pedidos.findById(1L)).thenReturn(Optional.of(order));
  assertEquals(409,assertThrows(ResponseStatusException.class,()->service.cancelarCliente(cliente.getEmail(),1L)).getStatusCode().value());
  assertEquals(StatusPedido.EM_PREPARO,order.getStatus());
 }
 @Test void receivedOrderCanBeCancelled(){
  Pedido order=new Pedido(cliente,null,null);org.springframework.test.util.ReflectionTestUtils.setField(order,"id",1L);when(pedidos.findById(1L)).thenReturn(Optional.of(order));
  assertEquals(StatusPedido.CANCELADO,service.cancelarCliente(cliente.getEmail(),1L).status());
  assertTrue(order.getEtapas().containsKey("CANCELADO"));
 }
}
