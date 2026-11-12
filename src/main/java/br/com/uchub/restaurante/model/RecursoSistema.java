package br.com.uchub.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recursos_sistema")
public class RecursoSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String codigo;
    @Column(nullable = false, length = 180)
    private String descricao;
    @Column(nullable = false)
    private boolean habilitado;
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
    protected RecursoSistema() {}
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public boolean isHabilitado() { return habilitado; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void alterarHabilitado(boolean habilitado) { this.habilitado = habilitado; this.atualizadoEm = LocalDateTime.now(); }
}
