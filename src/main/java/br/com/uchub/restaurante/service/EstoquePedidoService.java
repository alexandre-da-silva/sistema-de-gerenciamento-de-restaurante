package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.model.Pedido;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

/** Movimentos de estoque transacionais e idempotentes, vinculados ao pedido. */
@Service
public class EstoquePedidoService {
 private final JdbcTemplate jdbc;
 public EstoquePedidoService(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public void registrarAdicionais(Pedido pedido) {
  for(var item:pedido.getItens())for(Long adicionalId:item.getAdicionaisIds())
   jdbc.update("INSERT INTO item_pedido_adicionais(item_pedido_id,adicional_id) VALUES (?,?)",item.getId(),adicionalId);
 }
 public void baixar(Pedido pedido){
  if(pedido.isEstoqueBaixado())return;
  boolean habilitado=Boolean.TRUE.equals(jdbc.queryForObject(
    "SELECT habilitado FROM recursos_sistema WHERE codigo='CONTROLE_ESTOQUE'",Boolean.class));
  if(!habilitado)return;
  List<Map<String,Object>> itens=jdbc.queryForList("SELECT ingredienteId,SUM(quantidade) AS quantidade FROM ("
   + " SELECT pi.ingrediente_id ingredienteId, pi.quantidade_necessaria*ip.quantidade quantidade"
   + " FROM itens_pedido ip JOIN produto_ingredientes pi ON pi.produto_id=ip.produto_id WHERE ip.pedido_id=?"
   + " UNION ALL SELECT vi.ingrediente_id, vi.quantidade_necessaria*ip.quantidade"
   + " FROM itens_pedido ip JOIN variacao_ingredientes vi ON vi.variacao_id=ip.variacao_id WHERE ip.pedido_id=?"
   + " UNION ALL SELECT ai.ingrediente_id, ai.quantidade_necessaria*ip.quantidade"
   + " FROM itens_pedido ip JOIN item_pedido_adicionais ia ON ia.item_pedido_id=ip.id"
   + " JOIN adicional_ingredientes ai ON ai.adicional_id=ia.adicional_id WHERE ip.pedido_id=?"
   + " ) consumo GROUP BY ingredienteId ORDER BY ingredienteId",pedido.getId(),pedido.getId(),pedido.getId());
  for(var item:itens){
   long id=((Number)item.get("ingredienteId")).longValue();
   BigDecimal precisa=(BigDecimal)item.get("quantidade");
   var saldos=jdbc.queryForList("SELECT quantidade_atual FROM ingredientes WHERE id=? AND ativo=TRUE FOR UPDATE",BigDecimal.class,id);
   if(saldos.isEmpty()||saldos.getFirst().compareTo(precisa)<0)
    throw new ResponseStatusException(HttpStatus.CONFLICT,"Estoque insuficiente para concluir o pedido. Consulte o restaurante.");
   jdbc.update("UPDATE ingredientes SET quantidade_atual=quantidade_atual-? WHERE id=?",precisa,id);
   jdbc.update("INSERT INTO movimentacoes_estoque(ingrediente_id,tipo,quantidade,motivo,usuario_id,pedido_id) VALUES (?,'SAIDA',?,?,?,?)",
    id,precisa,"Consumo do pedido #"+pedido.getId(),pedido.getCliente().getId(),pedido.getId());
  }
  pedido.marcarEstoqueBaixado();
 }
 public void estornar(Pedido pedido){
  if(!pedido.isEstoqueBaixado()||pedido.isEstoqueEstornado())return;
  List<Map<String,Object>> itens=jdbc.queryForList("SELECT ingrediente_id,SUM(quantidade) quantidade "
   + "FROM movimentacoes_estoque WHERE pedido_id=? AND tipo='SAIDA' GROUP BY ingrediente_id ORDER BY ingrediente_id",pedido.getId());
  for(var item:itens){
   long id=((Number)item.get("ingrediente_id")).longValue();
   BigDecimal quantidade=(BigDecimal)item.get("quantidade");
   jdbc.queryForList("SELECT quantidade_atual FROM ingredientes WHERE id=? FOR UPDATE",BigDecimal.class,id);
   jdbc.update("UPDATE ingredientes SET quantidade_atual=quantidade_atual+? WHERE id=?",quantidade,id);
   jdbc.update("INSERT INTO movimentacoes_estoque(ingrediente_id,tipo,quantidade,motivo,usuario_id,pedido_id) VALUES (?,'ENTRADA',?,?,?,?)",
    id,quantidade,"Estorno do pedido #"+pedido.getId(),pedido.getCliente().getId(),pedido.getId());
  }
  pedido.marcarEstoqueEstornado();
 }
}
