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
    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias, RecursoService recursos) {
        this.produtos = produtos; this.categorias = categorias; this.recursos = recursos;
    }
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarPublicos() {
        recursos.exigirHabilitado("CARDAPIO_ONLINE");
        return produtos.findAllByAtivoTrueOrderByNomeAsc().stream().map(this::toResponse).toList();
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
        return toResponse(produto);
    }
    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = produtos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        produto.atualizar(request.nome().trim(), request.descricao(), request.preco(), obterCategoria(request.categoriaId()), !Boolean.FALSE.equals(request.ativo()));
        aplicarApresentacao(produto, request);
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
    private ProdutoResponse toResponse(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(),
                p.getCategoria() == null ? null : p.getCategoria().getId(),
                p.getCategoria() == null ? null : p.getCategoria().getNome(), p.isAtivo(),p.getImagemUrl(),p.getTempoPreparo(),p.getTipo(),p.isDestaque(),p.getPrecoPromocional());
    }
}
