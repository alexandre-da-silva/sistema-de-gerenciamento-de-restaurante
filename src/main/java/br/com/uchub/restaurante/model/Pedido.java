package br.com.uchub.restaurante.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "mesa_id")
    private Mesa mesa;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.RECEBIDO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
    protected Pedido() {}
    public Pedido(Usuario cliente, Mesa mesa, String observacao) {
        this.cliente = cliente; this.mesa = mesa; this.observacao = observacao;
    }
    public void adicionarItem(ItemPedido item) { itens.add(item); item.setPedido(this); }
    public void definirTotal(BigDecimal total) { this.total = total; }
    public void atualizarStatus(StatusPedido status) { this.status = status; this.atualizadoEm = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public Mesa getMesa() { return mesa; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public List<ItemPedido> getItens() { return itens; }
}
