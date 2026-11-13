package br.com.uchub.restaurante.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String nome;
    @Column(length = 1000)
    private String descricao;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
    @Column(name="imagem_url", length=255) private String imagemUrl;
    @Column(name="tempo_preparo") private Integer tempoPreparo = 20;
    @Column(length=60) private String tipo;
    private boolean destaque;
    @Column(name="preco_promocional", precision=12, scale=2) private BigDecimal precoPromocional;
    public String getImagemUrl() { return imagemUrl; }
    public Integer getTempoPreparo() { return tempoPreparo; }
    public String getTipo() { return tipo; }
    public boolean isDestaque() { return destaque; }
    public BigDecimal getPrecoPromocional() { return precoPromocional; }
    public BigDecimal getPrecoVenda() { return precoPromocional == null ? preco : precoPromocional; }
    public void apresentar(String imagem, Integer tempo, String tipo, boolean destaque, BigDecimal promocao) {
        this.imagemUrl=imagem; this.tempoPreparo=tempo==null?20:tempo;
        this.tipo=tipo; this.destaque=destaque; this.precoPromocional=promocao;
    }
    protected Produto() {}
    public Produto(String nome, String descricao, BigDecimal preco, Categoria categoria) {
        this.nome = nome; this.descricao = descricao; this.preco = preco;
        this.categoria = categoria; this.ativo = true; this.criadoEm = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public Categoria getCategoria() { return categoria; }
    public boolean isAtivo() { return ativo; }
    public void atualizar(String nome, String descricao, BigDecimal preco, Categoria categoria, boolean ativo) {
        this.nome = nome; this.descricao = descricao; this.preco = preco;
        this.categoria = categoria; this.ativo = ativo;
    }
}
