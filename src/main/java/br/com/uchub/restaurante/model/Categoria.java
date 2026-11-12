package br.com.uchub.restaurante.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String nome;
    @Column(length = 255)
    private String descricao;
    @Column(nullable = false)
    private boolean ativa = true;
    protected Categoria() {}
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public boolean isAtiva() { return ativa; }
}
