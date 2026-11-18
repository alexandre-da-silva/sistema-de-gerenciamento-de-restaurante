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
  return jdbc.query("SELECT id,nome,preco FROM variacoes_produto WHERE produto_id=? AND ativo=TRUE ORDER BY id",
   (r,n)->new ProdutoResponse.Variacao(r.getLong("id"),r.getString("nome"),r.getBigDecimal("preco")),id);
 }
 public List<ProdutoResponse.Adicional> adicionais(long id){
  return jdbc.query("SELECT id,nome,preco FROM adicionais_produto WHERE produto_id=? AND ativo=TRUE ORDER BY id",
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
 public List<Map<String,Object>> opcoesComInsumos(long produtoId,boolean variacao){
  String tabela=variacao?"variacoes_produto":"adicionais_produto";
  String receita=variacao?"variacao_ingredientes":"adicional_ingredientes";
  String coluna=variacao?"variacao_id":"adicional_id";
  var opcoes=jdbc.queryForList("SELECT id,nome,preco FROM "+tabela+" WHERE produto_id=? AND ativo=TRUE ORDER BY id",produtoId);
  for(var opcao:opcoes)opcao.put("ingredientes",jdbc.queryForList(
   "SELECT ingrediente_id AS ingredienteId,quantidade_necessaria AS quantidade FROM "+receita+" WHERE "+coluna+"=? ORDER BY ingrediente_id",opcao.get("id")));
  return opcoes;
 }
 public BigDecimal custo(long id){
  return jdbc.queryForObject("SELECT COALESCE(ROUND(SUM(pi.quantidade_necessaria*i.custo_unitario),2),0) "
    + "FROM produto_ingredientes pi JOIN ingredientes i ON i.id=pi.ingrediente_id WHERE pi.produto_id=?",BigDecimal.class,id);
 }
 public void substituir(long id,ProdutoRequest r){
  // Nunca apaga variações ou adicionais referenciados por pedidos / carrinhos.
  if(r.variacoes()!=null) {
   jdbc.update("UPDATE variacoes_produto SET ativo=FALSE WHERE produto_id=?",id);
   for(var v:r.variacoes()) {
   long optionId=upsertOpcao("variacoes_produto",id,v.id(),v.nome(),v.preco());
   atualizarInsumosOpcao("variacao_ingredientes","variacao_id",optionId,v.ingredientes());
   }
  }
  if(r.adicionais()!=null) {
   jdbc.update("UPDATE adicionais_produto SET ativo=FALSE WHERE produto_id=?",id);
   for(var a:r.adicionais()) {
   long optionId=upsertOpcao("adicionais_produto",id,a.id(),a.nome(),a.preco());
   atualizarInsumosOpcao("adicional_ingredientes","adicional_id",optionId,a.ingredientes());
   }
  }
  // Null indica campo não enviado (preservar). Lista vazia indica limpar.
  if(r.disponibilidade()!=null) {
   jdbc.update("DELETE FROM produto_disponibilidade WHERE produto_id=?",id);
   for(var d:r.disponibilidade())
    jdbc.update("INSERT INTO produto_disponibilidade(produto_id,dia_semana,hora_inicio,hora_fim) VALUES(?,?,?,?)",
      id,d.diaSemana(),java.sql.Time.valueOf(d.horaInicio()),java.sql.Time.valueOf(d.horaFim()));
  }
  if(r.fichaTecnica()!=null) {
   jdbc.update("DELETE FROM produto_ingredientes WHERE produto_id=?",id);
   for(var f:r.fichaTecnica())
    jdbc.update("INSERT INTO produto_ingredientes(produto_id,ingrediente_id,quantidade_necessaria) VALUES(?,?,?)",
      id,f.ingredienteId(),f.quantidade());
  }
 }
 private long upsertOpcao(String tabela,long produtoId,Long id,String nome,BigDecimal preco){
  // "tabela" só aceita duas constantes internas, jamais é fornecida pelo cliente.
  if(!tabela.equals("variacoes_produto")&&!tabela.equals("adicionais_produto"))throw new IllegalArgumentException();
  List<Long> candidatos=id!=null?
    jdbc.queryForList("SELECT id FROM "+tabela+" WHERE produto_id=? AND id=?",Long.class,produtoId,id):
    jdbc.queryForList("SELECT id FROM "+tabela+" WHERE produto_id=? AND LOWER(nome)=LOWER(?) ORDER BY id LIMIT 1",Long.class,produtoId,nome.trim());
  if(id!=null && candidatos.isEmpty())
   throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Opção não pertence ao produto.");
  if(!candidatos.isEmpty()){
   jdbc.update("UPDATE "+tabela+" SET nome=?,preco=?,ativo=TRUE WHERE id=?",nome.trim(),preco,candidatos.getFirst());
   return candidatos.getFirst();
  }
  org.springframework.jdbc.support.KeyHolder key=new org.springframework.jdbc.support.GeneratedKeyHolder();
  jdbc.update(con->{var ps=con.prepareStatement("INSERT INTO "+tabela+"(produto_id,nome,preco,ativo) VALUES(?,?,?,TRUE)",java.sql.Statement.RETURN_GENERATED_KEYS);
   ps.setLong(1,produtoId);ps.setString(2,nome.trim());ps.setBigDecimal(3,preco);return ps;},key);
  return key.getKey().longValue();
 }
 private void atualizarInsumosOpcao(String tabela,String coluna,long optionId,List<ProdutoRequest.Insumo> insumos){
  if(insumos==null)return; // Atualizações antigas preservam as receitas atuais.
  jdbc.update("DELETE FROM "+tabela+" WHERE "+coluna+"=?",optionId);
  for(var insumo:insumos)
   jdbc.update("INSERT INTO "+tabela+"("+coluna+",ingrediente_id,quantidade_necessaria) VALUES (?,?,?)",
      optionId,insumo.ingredienteId(),insumo.quantidade());
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
  var values=jdbc.queryForList("SELECT preco FROM variacoes_produto WHERE id=? AND produto_id=? AND ativo=TRUE",BigDecimal.class,variacaoId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public BigDecimal precoAdicional(long produtoId,long adicionalId){
  var values=jdbc.queryForList("SELECT preco FROM adicionais_produto WHERE id=? AND produto_id=? AND ativo=TRUE",BigDecimal.class,adicionalId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public String nomeVariacao(long produtoId,long variacaoId){
  var values=jdbc.queryForList("SELECT nome FROM variacoes_produto WHERE id=? AND produto_id=? AND ativo=TRUE",String.class,variacaoId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
 public String nomeAdicional(long produtoId,long adicionalId){
  var values=jdbc.queryForList("SELECT nome FROM adicionais_produto WHERE id=? AND produto_id=? AND ativo=TRUE",String.class,adicionalId,produtoId);
  return values.isEmpty()?null:values.getFirst();
 }
}
