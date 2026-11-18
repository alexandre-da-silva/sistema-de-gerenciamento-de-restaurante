package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ProdutoService {
 private static final ZoneId FUSO=ZoneId.of("America/Sao_Paulo");
 private final ProdutoRepository produtos;
 private final CategoriaRepository categorias;
 private final RecursoService recursos;
 private final ProdutoDetalhesRepository detalhes;
 private final ObjectMapper mapper;
 public ProdutoService(ProdutoRepository produtos,CategoriaRepository categorias,RecursoService recursos,
                       ProdutoDetalhesRepository detalhes,ObjectMapper mapper){
  this.produtos=produtos;this.categorias=categorias;this.recursos=recursos;this.detalhes=detalhes;this.mapper=mapper;
 }
 @Transactional(readOnly=true)
 public List<ProdutoResponse> listarPublicos(){
  recursos.exigirHabilitado("CARDAPIO_ONLINE");
  return produtos.findAllByAtivoTrueOrderByNomeAsc().stream().map(this::toResponse)
    .sorted(Comparator.comparingInt(ProdutoResponse::ordemExibicao).thenComparing(ProdutoResponse::nome)).toList();
 }
 @Transactional(readOnly=true)
 public List<Map<String,Object>> ingredientes(){return detalhes.ingredientes();}
 @Transactional(readOnly=true)
 public List<Map<String,Object>> listarTodos(){
  return produtos.findAll().stream().map(p->{
   Map<String,Object> item=new LinkedHashMap<>(mapper.convertValue(toResponse(p),Map.class));
   item.put("sku",p.getSku());item.put("ncm",p.getNcm());item.put("cfop",p.getCfop());
   item.put("cstCsosn",p.getCstCsosn());item.put("aliquotaIcms",p.getAliquotaIcms());
   item.put("fichaTecnica",detalhes.ficha(p.getId()));
   item.put("variacoes",detalhes.opcoesComInsumos(p.getId(),true));
   item.put("adicionais",detalhes.opcoesComInsumos(p.getId(),false));
   BigDecimal custo=detalhes.custo(p.getId()),venda=p.getPrecoVenda();
   item.put("custoPrato",custo);item.put("margemLucro",venda.subtract(custo));
   item.put("percentualMargem",venda.signum()>0?venda.subtract(custo).multiply(new BigDecimal("100")).divide(venda,2,java.math.RoundingMode.HALF_UP):BigDecimal.ZERO);
   return item;
  }).toList();
 }
 @Transactional(readOnly=true)
 public List<Map<String,Object>> emAlta(){
  recursos.exigirHabilitado("CARDAPIO_ONLINE");
  Map<Long,Integer> vendidos=detalhes.vendidos();
  int maior=vendidos.values().stream().mapToInt(Integer::intValue).max().orElse(0);
  Set<Long> ativos=new HashSet<>();
  produtos.findAllByAtivoTrueOrderByNomeAsc().forEach(p->ativos.add(p.getId()));
  return vendidos.entrySet().stream().filter(e->ativos.contains(e.getKey()))
   .sorted((a,b)->{int n=Integer.compare(b.getValue(),a.getValue());return n==0?Long.compare(a.getKey(),b.getKey()):n;})
   .map(e->{Map<String,Object> item=new LinkedHashMap<>();item.put("produtoId",e.getKey());
    item.put("unidadesVendidas",e.getValue());
    // Selo apenas quando há evidência: 3+ unidades e pelo menos 50% do líder no período.
    item.put("popular",e.getValue()>=3 && e.getValue()*2>=maior);return item;})
   .toList();
 }
 @Transactional
 public ProdutoResponse criar(ProdutoRequest request){
  validar(request);
  Categoria categoria=obterCategoria(request.categoriaId());
  Produto produto=new Produto(request.nome().trim(),request.descricao(),request.preco(),categoria);
  if(Boolean.FALSE.equals(request.ativo()))produto.atualizar(request.nome().trim(),request.descricao(),request.preco(),categoria,false);
  configurar(produto,request);
  produtos.saveAndFlush(produto);
  detalhes.substituir(produto.getId(),request);
  return toResponse(produto);
 }
 @Transactional
 public ProdutoResponse atualizar(Long id,ProdutoRequest request){
  validar(request);
  Produto produto=produtos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Produto não encontrado."));
  produto.atualizar(request.nome().trim(),request.descricao(),request.preco(),obterCategoria(request.categoriaId()),!Boolean.FALSE.equals(request.ativo()));
  configurar(produto,request);
  produtos.saveAndFlush(produto);
  detalhes.substituir(id,request);
  return toResponse(produto);
 }
 @Transactional
 public void excluir(Long id){
  Produto p=produtos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Produto não encontrado."));
  p.atualizar(p.getNome(),p.getDescricao(),p.getPreco(),p.getCategoria(),false);
 }
 private void validar(ProdutoRequest r){
  if(r.precoPromocional()!=null && r.precoPromocional().compareTo(r.preco())>=0)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A promoção deve ser menor que o preço normal.");
  Set<Long> ids=new HashSet<>();
  if(r.fichaTecnica()!=null)for(var f:r.fichaTecnica()){
   if(!ids.add(f.ingredienteId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ingrediente duplicado na ficha técnica.");
  }
  if(r.disponibilidade()!=null)for(var d:r.disponibilidade())
   if(d.horaInicio().equals(d.horaFim()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Informe um intervalo de horário não vazio.");
 }
 private void configurar(Produto p,ProdutoRequest r){
  p.apresentar(r.imagemUrl(),r.tempoPreparo(),r.tipo(),Boolean.TRUE.equals(r.destaque()),r.precoPromocional());
  p.configurar(r.sku(),r.setorProducao(),r.porcao(),r.ordemExibicao(),r.observacoesPermitidas(),
   r.alergenos(),r.tags(),r.calorias(),r.ncm(),r.cfop(),r.cstCsosn(),r.aliquotaIcms());
 }
 private Categoria obterCategoria(Long id){
  if(id==null)return null;
  return categorias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Categoria inválida."));
 }
 private ProdutoResponse toResponse(Produto p){
  var horarios=detalhes.horarios(p.getId());
  return new ProdutoResponse(p.getId(),p.getNome(),p.getDescricao(),p.getPreco(),
   p.getCategoria()==null?null:p.getCategoria().getId(),p.getCategoria()==null?null:p.getCategoria().getNome(),
   p.isAtivo(),p.getImagemUrl(),p.getTempoPreparo(),p.getTipo(),p.isDestaque(),p.getPrecoPromocional(),
   p.getSetorProducao(),p.getPorcao(),p.getOrdemExibicao(),p.getObservacoesPermitidas(),p.getAlergenos(),
   p.getTags(),p.getCalorias(),disponivel(horarios),detalhes.variacoes(p.getId()),detalhes.adicionais(p.getId()),horarios);
 }
 public static boolean disponivel(List<ProdutoResponse.Horario> horarios){
  if(horarios.isEmpty())return true;
  ZonedDateTime agora=ZonedDateTime.now(FUSO);
  int hoje=agora.getDayOfWeek().getValue(), ontem=hoje==1?7:hoje-1;
  LocalTime hora=agora.toLocalTime();
  return horarios.stream().anyMatch(h->{
   if(h.horaInicio().isBefore(h.horaFim()))
    return h.diaSemana()==hoje && !hora.isBefore(h.horaInicio()) && hora.isBefore(h.horaFim());
   return (h.diaSemana()==hoje && !hora.isBefore(h.horaInicio()))
     || (h.diaSemana()==ontem && hora.isBefore(h.horaFim()));
  });
 }
}
