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
    protected ItemPedido() {}
    public ItemPedido(Produto produto, Integer quantidade) {
        this.produto = produto; this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
    void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
