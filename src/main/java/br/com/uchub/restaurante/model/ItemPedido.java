package br.com.uchub.restaurante.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
    @Column(nullable = false)
    private Integer quantidade;
    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
    @Column(name="produto_nome",length=140) private String produtoNome;
    @Column(name="imagem_url_snapshot",length=255) private String imagemUrlSnapshot;
    @Column(name="variacao_id") private Long variacaoId;
    @Transient private java.util.List<Long> adicionaisIds=java.util.List.of();
    public String getProdutoNome() { return produtoNome != null ? produtoNome : produto.getNome(); }
    public String getImagemUrlSnapshot() { return produtoNome != null ? imagemUrlSnapshot : produto.getImagemUrl(); }
    public Long getVariacaoId() { return variacaoId; }
    public java.util.List<Long> getAdicionaisIds() { return adicionaisIds; }
    @Column(name="observacao_item",length=500) private String observacao;
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao=observacao==null?null:observacao.trim(); }
    @Column(name="variacao_nome",length=80) private String variacaoNome;
    @Column(name="adicionais_descricao",length=500) private String adicionaisDescricao;
    public String getVariacaoNome(){return variacaoNome;}
    public String getAdicionaisDescricao(){return adicionaisDescricao;}
    public void personalizar(String variacao,String adicionais,BigDecimal preco){
       variacaoNome=variacao;adicionaisDescricao=adicionais;precoUnitario=preco;
       subtotal=preco.multiply(BigDecimal.valueOf(quantidade));
    }
    public void personalizar(String variacao,String adicionais,BigDecimal preco,Long varId,java.util.List<Long> extras) {
        personalizar(variacao,adicionais,preco);
        this.variacaoId=varId;
        this.adicionaisIds=java.util.List.copyOf(extras);
    }
    protected ItemPedido() {}
    public ItemPedido(Produto produto, Integer quantidade) {
        this.produto = produto; this.quantidade = quantidade;
        this.produtoNome=produto.getNome();this.imagemUrlSnapshot=produto.getImagemUrl();
        this.precoUnitario = produto.getPrecoVenda();
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
    void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
