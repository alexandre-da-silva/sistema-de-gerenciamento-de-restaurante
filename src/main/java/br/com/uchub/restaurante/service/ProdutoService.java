package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final RecursoService recursos;
    private final CatalogoOperacionalService catalogo;
    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias, RecursoService recursos, CatalogoOperacionalService catalogo) {
        this.produtos = produtos; this.categorias = categorias; this.recursos = recursos; this.catalogo=catalogo;
    }
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarPublicos() {
        recursos.exigirHabilitado("CARDAPIO_ONLINE");
        var vendas=catalogo.vendas();long max=vendas.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return produtos.findAllByAtivoTrueOrderByNomeAsc().stream().map(p->toResponse(p,false,vendas.getOrDefault(p.getId(),0L),max))
         .sorted(java.util.Comparator.comparingInt(p->p.detalhes().ordemExibicao()==null?0:p.detalhes().ordemExibicao())).toList();
    }
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() { return produtos.findAll().stream().map(this::toResponse).toList(); }
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        validarPromocao(request);
        Categoria categoria = obterCategoria(request.categoriaId());
        Produto produto = produtos.save(new Produto(request.nome().trim(), request.descricao(), request.preco(), categoria));
        if (Boolean.FALSE.equals(request.ativo())) produto.atualizar(request.nome().trim(), request.descricao(), request.preco(), categoria, false);
        aplicarApresentacao(produto, request);
        catalogo.salvar(produto,request.detalhes());
        return toResponse(produto);
    }
    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = produtos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        produto.atualizar(request.nome().trim(), request.descricao(), request.preco(), obterCategoria(request.categoriaId()), !Boolean.FALSE.equals(request.ativo()));
        aplicarApresentacao(produto, request);
        catalogo.salvar(produto,request.detalhes());
        return toResponse(produto);
    }
    @Transactional
    public void excluir(Long id) {
        Produto produto = produtos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        // Exclusão lógica: preserva histórico de pedidos e vendas.
        produto.atualizar(produto.getNome(), produto.getDescricao(), produto.getPreco(), produto.getCategoria(), false);
    }
    private void validarPromocao(ProdutoRequest r) {
        if(r.precoPromocional()!=null && r.precoPromocional().compareTo(r.preco())>=0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A promoção deve ser menor que o preço normal.");
    }
    private void aplicarApresentacao(Produto p, ProdutoRequest r) {
        validarPromocao(r);
        p.apresentar(r.imagemUrl(),r.tempoPreparo(),r.tipo(),Boolean.TRUE.equals(r.destaque()),r.precoPromocional());
    }
    private Categoria obterCategoria(Long id) {
        if (id == null) return null;
        return categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida."));
    }
    @Transactional(readOnly = true)
    public List<ProdutoResponse> recomendacoes(){return listarPublicos().stream().filter(ProdutoResponse::disponivelAgora)
     .sorted(java.util.Comparator.comparingLong(ProdutoResponse::vendas30Dias).reversed().thenComparing(java.util.Comparator.comparing(ProdutoResponse::destaque).reversed()).thenComparing(ProdutoResponse::nome)).limit(16).toList();}
    private ProdutoResponse toResponse(Produto p){return toResponse(p,true,0,0);}
    private ProdutoResponse toResponse(Produto p,boolean admin,long vendas,long max) {
        var d=catalogo.detalhes(p);var custo=admin?catalogo.custo(d):null;
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(),
                p.getCategoria() == null ? null : p.getCategoria().getId(),
                p.getCategoria() == null ? null : p.getCategoria().getNome(), p.isAtivo(),p.getImagemUrl(),p.getTempoPreparo(),p.getTipo(),p.isDestaque(),p.getPrecoPromocional(),admin?d:d.publico(),custo,admin?p.getPrecoVenda().subtract(custo):null,catalogo.disponivel(p),vendas,CatalogoOperacionalService.calor(vendas,max),CatalogoOperacionalService.popular(vendas,max));
    }
}
