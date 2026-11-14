package br.com.uchub.restaurante.service;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.*;
import java.util.*;
@Service
public class CatalogoOperacionalService {
 private final ObjectMapper mapper;private final JdbcTemplate jdbc;
 public CatalogoOperacionalService(ObjectMapper mapper,JdbcTemplate jdbc){this.mapper=mapper;this.jdbc=jdbc;}
 public ProdutoDetalhes detalhes(Produto p){try{return p.getDetalhesJson()==null?ProdutoDetalhes.vazio():mapper.readValue(p.getDetalhesJson(),ProdutoDetalhes.class);}catch(Exception e){throw new IllegalStateException("Cadastro do produto inválido",e);}}
 public void salvar(Produto p,ProdutoDetalhes supplied){
  ProdutoDetalhes d=supplied==null?ProdutoDetalhes.vazio():supplied;
  if((d.inicio()==null)!=(d.fim()==null))bad("Informe início e fim da disponibilidade.");
  if(d.inicio()!=null&&d.inicio().equals(d.fim()))bad("Use horários distintos ou deixe ambos vazios para servir o dia todo.");
  if(!d.lista(d.variacoes()).isEmpty()&&p.getPrecoPromocional()!=null)bad("Produtos com variações usam o preço de cada variação; remova a promoção do preço base.");
  Set<String> ids=new HashSet<>();
  for(var v:d.lista(d.variacoes()))if(!ids.add(v.id()))bad("Identificadores de variações duplicados.");
  ids.clear();for(var a:d.lista(d.adicionais())){if(!ids.add(a.id()))bad("Identificadores de adicionais duplicados.");validarInsumos(d.lista(a.insumos()));}
  validarInsumos(d.lista(d.fichaTecnica()));
  if(d.controlaEstoque()&&d.lista(d.fichaTecnica()).isEmpty())bad("Preencha a ficha técnica para controlar estoque.");
  try{p.definirDetalhes(d.sku()==null||d.sku().isBlank()?null:d.sku().trim(),mapper.writeValueAsString(d));}catch(Exception e){throw new IllegalStateException(e);}
  jdbc.update("DELETE FROM produto_ingredientes WHERE produto_id=?",p.getId());
  for(var i:d.lista(d.fichaTecnica()))jdbc.update("INSERT INTO produto_ingredientes(produto_id,ingrediente_id,quantidade_necessaria) VALUES(?,?,?)",p.getId(),i.ingredienteId(),i.quantidade());
 }
 private void validarInsumos(List<ProdutoDetalhes.Insumo> itens){Set<Long> ids=new HashSet<>();for(var i:itens){if(!ids.add(i.ingredienteId()))bad("Não repita o mesmo insumo na ficha.");Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM ingredientes WHERE id=? AND ativo=TRUE",Integer.class,i.ingredienteId());if(count==null||count==0)bad("Insumo inexistente ou inativo.");}}
 public BigDecimal custo(ProdutoDetalhes d){BigDecimal total=BigDecimal.ZERO;for(var i:d.lista(d.fichaTecnica())){BigDecimal unit=jdbc.queryForObject("SELECT custo_unitario FROM ingredientes WHERE id=?",BigDecimal.class,i.ingredienteId());total=total.add(unit.multiply(i.quantidade()));}return total.setScale(2,RoundingMode.HALF_UP);}
 public boolean disponivel(Produto p){return p.isAtivo()&&disponivel(detalhes(p),ZonedDateTime.now(ZoneId.of("America/Bahia")));}
 public static boolean disponivel(ProdutoDetalhes d,ZonedDateTime now){
  LocalTime t=now.toLocalTime();int day=now.getDayOfWeek().getValue();
  if(d.inicio()==null||d.fim()==null)return d.lista(d.diasSemana()).isEmpty()||d.diasSemana().contains(day);
  LocalTime start=LocalTime.parse(d.inicio()),end=LocalTime.parse(d.fim());boolean overnight=end.isBefore(start);
  int serviceDay=overnight&&t.isBefore(end)?now.minusDays(1).getDayOfWeek().getValue():day;
  boolean allowed=d.lista(d.diasSemana()).isEmpty()||d.diasSemana().contains(serviceDay);
  return allowed&&(overnight?(!t.isBefore(start)||t.isBefore(end)):(!t.isBefore(start)&&t.isBefore(end)));
 }
 public Map<Long,Long> vendas(){Map<Long,Long> m=new HashMap<>();jdbc.query("SELECT i.produto_id,SUM(i.quantidade) unidades FROM itens_pedido i JOIN pedidos p ON p.id=i.pedido_id WHERE p.status='ENTREGUE' AND EXISTS(SELECT 1 FROM pagamentos pg WHERE pg.pedido_id=p.id AND pg.status='CONFIRMADO' AND pg.criado_em>=?) GROUP BY i.produto_id",rs->{m.put(rs.getLong(1),rs.getLong(2));},LocalDateTime.now(ZoneId.of("America/Bahia")).minusDays(30));return m;}
 public static double calor(long count,long max){return max==0?0:Math.log1p(count)/Math.log1p(max);}
 public static boolean popular(long count,long max){return count>=5&&count>=Math.ceil(max*0.35);}
 public record Escolha(BigDecimal preco,String resumo,String setor,Map<Long,BigDecimal> consumo) {}
 public Escolha escolher(Produto p,ItemPedidoRequest item){
  if(!disponivel(p))throw new ResponseStatusException(HttpStatus.CONFLICT,p.getNome()+" não está disponível neste horário.");
  ProdutoDetalhes d=detalhes(p);BigDecimal value=p.getPrecoVenda(),factor=BigDecimal.ONE;List<String> names=new ArrayList<>();
  if(!d.lista(d.variacoes()).isEmpty()){
   var v=d.variacoes().stream().filter(x->x.id().equals(item.variacaoId())).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Escolha uma variação válida de "+p.getNome()));
   value=v.preco();factor=v.fatorReceita();names.add(v.nome());
  }else if(item.variacaoId()!=null&&!item.variacaoId().isBlank())bad("Variação não existe mais.");
  Map<Long,BigDecimal> use=new TreeMap<>();if(d.controlaEstoque())for(var i:d.lista(d.fichaTecnica()))use.merge(i.ingredienteId(),i.quantidade().multiply(factor),BigDecimal::add);
  Set<String> unique=new HashSet<>();for(String id:item.adicionais()==null?List.<String>of():item.adicionais()){
   if(!unique.add(id))bad("Adicional repetido.");
   var a=d.lista(d.adicionais()).stream().filter(x->x.id().equals(id)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Adicional indisponível."));
   value=value.add(a.preco());names.add("+ "+a.nome());
   if(d.controlaEstoque())for(var i:d.lista(a.insumos()))use.merge(i.ingredienteId(),i.quantidade(),BigDecimal::add);
  }
  if(item.observacao()!=null&&!item.observacao().isBlank()){if(Boolean.FALSE.equals(d.permiteObservacoes()))bad("Este produto não permite observações.");names.add(item.observacao().trim());}
  return new Escolha(value,String.join(" · ",names),d.setorProducao()==null||d.setorProducao().isBlank()?"Cozinha":d.setorProducao(),use);
 }
 private static void bad(String msg){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,msg);}
}
