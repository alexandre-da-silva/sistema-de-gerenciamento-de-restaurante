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
    @Column(length=4000) private String configuracao;
    @Column(name="setor_producao",length=60) private String setorProducao="Cozinha";
    public String getConfiguracao(){return configuracao;}
    public String getSetorProducao(){return setorProducao;}
    public void personalizar(BigDecimal preco,String configuracao,String setor){this.precoUnitario=preco;this.subtotal=preco.multiply(BigDecimal.valueOf(quantidade));this.configuracao=configuracao;this.setorProducao=setor;}
    protected ItemPedido() {}
    public ItemPedido(Produto produto, Integer quantidade) {
        this.produto = produto; this.quantidade = quantidade;
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
