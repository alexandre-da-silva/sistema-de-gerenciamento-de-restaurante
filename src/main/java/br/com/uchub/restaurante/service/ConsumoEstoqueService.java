package br.com.uchub.restaurante.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;
@Service
public class ConsumoEstoqueService {
 private final JdbcTemplate jdbc;private final RecursoService recursos;
 public ConsumoEstoqueService(JdbcTemplate jdbc,RecursoService recursos){this.jdbc=jdbc;this.recursos=recursos;}
 // Called inside the order transaction. All ingredient locks follow ID order.
 public void baixar(long pedido,long usuario,Map<Long,BigDecimal> consumo){
  if(consumo.isEmpty())return;recursos.exigirHabilitado("CONTROLE_ESTOQUE");
  for(var e:new TreeMap<>(consumo).entrySet()){
   var ingredient=jdbc.queryForMap("SELECT nome,quantidade_atual,ativo FROM ingredientes WHERE id=? FOR UPDATE",e.getKey());
   BigDecimal q=e.getValue().setScale(3,RoundingMode.HALF_UP),saldo=(BigDecimal)ingredient.get("quantidade_atual");
   if(q.signum()<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantidade de insumo insuficiente para a precisão do estoque.");
   if(saldo.compareTo(q)<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Estoque insuficiente: "+ingredient.get("nome"));
   jdbc.update("UPDATE ingredientes SET quantidade_atual=quantidade_atual-? WHERE id=?",q,e.getKey());
   jdbc.update("INSERT INTO pedido_consumos(pedido_id,ingrediente_id,quantidade) VALUES(?,?,?)",pedido,e.getKey(),q);
   jdbc.update("INSERT INTO movimentacoes_estoque(ingrediente_id,tipo,quantidade,motivo,usuario_id) VALUES(?,'SAIDA',?,?,?)",e.getKey(),q,"Pedido #"+pedido,usuario);
  }
 }
 // The order itself is locked by PedidoRepository before cancellation.
 public void devolver(long pedido,long usuario){
  var rows=jdbc.queryForList("SELECT ingrediente_id,quantidade FROM pedido_consumos WHERE pedido_id=? AND devolvido=FALSE ORDER BY ingrediente_id",pedido);
  for(var row:rows){long id=((Number)row.get("ingrediente_id")).longValue();BigDecimal q=(BigDecimal)row.get("quantidade");
   jdbc.queryForObject("SELECT quantidade_atual FROM ingredientes WHERE id=? FOR UPDATE",BigDecimal.class,id);
   jdbc.update("UPDATE ingredientes SET quantidade_atual=quantidade_atual+? WHERE id=?",q,id);
   jdbc.update("UPDATE pedido_consumos SET devolvido=TRUE WHERE pedido_id=? AND ingrediente_id=?",pedido,id);
   jdbc.update("INSERT INTO movimentacoes_estoque(ingrediente_id,tipo,quantidade,motivo,usuario_id) VALUES(?,'ENTRADA',?,?,?)",id,q,"Cancelamento pedido #"+pedido,usuario);
  }
 }
}
