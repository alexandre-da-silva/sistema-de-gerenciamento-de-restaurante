package br.com.uchub.restaurante.repository;

import br.com.uchub.restaurante.dto.ProdutoRequest;
import br.com.uchub.restaurante.dto.ProdutoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class ProdutoDetalhesRepository {
 private final JdbcTemplate jdbc;
 public ProdutoDetalhesRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public List<ProdutoResponse.Variacao> variacoes(long id){
  return jdbc.query("SELECT id,nome,preco FROM variacoes_produto WHERE produto_id=? ORDER BY id",
   (r,n)->new ProdutoResponse.Variacao(r.getLong("id"),r.getString("nome"),r.getBigDecimal("preco")),id);
 }
 public List<ProdutoResponse.Adicional> adicionais(long id){
  return jdbc.query("SELECT id,nome,preco FROM adicionais_produto WHERE produto_id=? ORDER BY id",
   (r,n)->new ProdutoResponse.Adicional(r.getLong("id"),r.getString("nome"),r.getBigDecimal("preco")),id);
 }
 public List<ProdutoResponse.Horario> horarios(long id){
  return jdbc.query("SELECT dia_semana,hora_inicio,hora_fim FROM produto_disponibilidade WHERE produto_id=? ORDER BY dia_semana,hora_inicio",
   (r,n)->new ProdutoResponse.Horario(r.getInt("dia_semana"),r.getTime("hora_inicio").toLocalTime(),r.getTime("hora_fim").toLocalTime()),id);
 }
 public List<Map<String,Object>> ingredientes(){
  return jdbc.queryForList("SELECT id,nome,unidade_medida,quantidade_atual,custo_unitario FROM ingredientes WHERE ativo=TRUE ORDER BY nome LIMIT 500");
 }
 public List<Map<String,Object>> ficha(long id){
  return jdbc.queryForList("SELECT i.id ingredienteId,i.nome,i.unidade_medida unidade,pi.quantidade_necessaria quantidade,"
    + "i.custo_unitario custoUnitario,ROUND(pi.quantidade_necessaria*i.custo_unitario,2) subtotal "
    + "FROM produto_ingredientes pi JOIN ingredientes i ON i.id=pi.ingrediente_id WHERE pi.produto_id=? ORDER BY i.nome",id);
 }
 public BigDecimal custo(long id){
  return jdbc.queryForObject("SELECT COALESCE(ROUND(SUM(pi.quantidade_necessaria*i.custo_unitario),2),0) "
    + "FROM produto_ingredientes pi JOIN ingredientes i ON i.id=pi.ingrediente_id WHERE pi.produto_id=?",BigDecimal.class,id);
 }
 public void substituir(long id,ProdutoRequest r){
  jdbc.update("DELETE FROM variacoes_produto WHERE produto_id=?",id);
  jdbc.update("DELETE FROM adicionais_produto WHERE produto_id=?",id);
  jdbc.update("DELETE FROM produto_disponibilidade WHERE produto_id=?",id);
  jdbc.update("DELETE FROM produto_ingredientes WHERE produto_id=?",id);
  if(r.variacoes()!=null)for(var v:r.variacoes())
   jdbc.update("INSERT INTO variacoes_produto(produto_id,nome,preco) VALUES(?,?,?)",id,v.nome().trim(),v.preco());
  if(r.adicionais()!=null)for(var a:r.adicionais())
   jdbc.update("INSERT INTO adicionais_produto(produto_id,nome,preco) VALUES(?,?,?)",id,a.nome().trim(),a.preco());
  if(r.disponibilidade()!=null)for(var d:r.disponibilidade())
   jdbc.update("INSERT INTO produto_disponibilidade(produto_id,dia_semana,hora_inicio,hora_fim) VALUES(?,?,?,?)",
      id,d.diaSemana(),java.sql.Time.valueOf(d.horaInicio()),java.sql.Time.valueOf(d.horaFim()));
  if(r.fichaTecnica()!=null)for(var f:r.fichaTecnica())
   jdbc.update("INSERT INTO produto_ingredientes(produto_id,ingrediente_id,quantidade_necessaria) VALUES(?,?,?)",
      id,f.ingredienteId(),f.quantidade());
 }
 public Map<Long,Integer> vendidos(){
  var items=jdbc.queryForList("SELECT i.produto_id produtoId,SUM(i.quantidade) unidades FROM itens_pedido i "
   + "JOIN pedidos p ON p.id=i.pedido_id WHERE p.status='ENTREGUE' "
   + "AND p.criado_em>=DATE_SUB(NOW(),INTERVAL 30 DAY) GROUP BY i.produto_id ORDER BY unidades DESC");
  Map<Long,Integer> result=new HashMap<>();
  for(var x:items)result.put(((Number)x.get("produtoId")).longValue(),((Number)x.get("unidades")).intValue());
  return result;
 }
 public BigDecimal precoVariacao(long produtoId,long variacaoId){
  var values=jdbc.queryForList("SELECT preco FROM variacoes_produto WHERE id=? AND produto_id=?",BigDecimal.class,variacaoId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public BigDecimal precoAdicional(long produtoId,long adicionalId){
  var values=jdbc.queryForList("SELECT preco FROM adicionais_produto WHERE id=? AND produto_id=?",BigDecimal.class,adicionalId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public String nomeVariacao(long produtoId,long variacaoId){
  var values=jdbc.queryForList("SELECT nome FROM variacoes_produto WHERE id=? AND produto_id=?",String.class,variacaoId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public String nomeAdicional(long produtoId,long adicionalId){
  var values=jdbc.queryForList("SELECT nome FROM adicionais_produto WHERE id=? AND produto_id=?",String.class,adicionalId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
}
