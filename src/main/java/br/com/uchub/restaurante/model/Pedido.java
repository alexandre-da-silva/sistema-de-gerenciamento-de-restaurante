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
    @Column(name="taxa_servico",precision=12,scale=2) private BigDecimal taxaServico=BigDecimal.ZERO;
    @Column(length=36) private String referencia;
    @Column(name="preparo_em") private LocalDateTime preparoEm;
    @Column(name="pronto_em") private LocalDateTime prontoEm;
    @Column(name="entregue_em") private LocalDateTime entregueEm;
    @Column(name="cancelado_em") private LocalDateTime canceladoEm;
    public BigDecimal getTaxaServico() { return taxaServico; }
    public void definirCheckout(BigDecimal taxa,String referencia) { this.taxaServico=taxa;this.referencia=referencia; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public java.util.Map<String,LocalDateTime> getEtapas() {
        var etapas=new java.util.LinkedHashMap<String,LocalDateTime>();etapas.put("RECEBIDO",criadoEm);
        if(preparoEm!=null)etapas.put("EM_PREPARO",preparoEm);if(prontoEm!=null)etapas.put("PRONTO",prontoEm);
        if(entregueEm!=null)etapas.put("ENTREGUE",entregueEm);if(canceladoEm!=null)etapas.put("CANCELADO",canceladoEm);
        return etapas;
    }
    protected Pedido() {}
    public Pedido(Usuario cliente, Mesa mesa, String observacao) {
        this.cliente = cliente; this.mesa = mesa; this.observacao = observacao;
    }
    public void adicionarItem(ItemPedido item) { itens.add(item); item.setPedido(this); }
    public void definirTotal(BigDecimal total) { this.total = total; }
    public void atualizarStatus(StatusPedido status) { this.status = status; this.atualizadoEm = LocalDateTime.now();
        switch(status) { case EM_PREPARO -> preparoEm=atualizadoEm; case PRONTO -> prontoEm=atualizadoEm;
          case ENTREGUE -> entregueEm=atualizadoEm; case CANCELADO -> canceladoEm=atualizadoEm; default -> {} }
    }
    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public Mesa getMesa() { return mesa; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public List<ItemPedido> getItens() { return itens; }
}
