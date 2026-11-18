package br.com.uchub.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;
    @Column(length = 25)
    private String telefone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;
    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "token_version", nullable = false)
    private long tokenVersion = 0;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected Usuario() {}
    public Usuario(String nome, String email, String senhaHash, String telefone, PerfilUsuario perfil) {
        this.nome = nome; this.email = email; this.senhaHash = senhaHash;
        this.telefone = telefone; this.perfil = perfil; this.ativo = true;
        this.criadoEm = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public String getTelefone() { return telefone; }
    public PerfilUsuario getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public long getTokenVersion() { return tokenVersion; }
    public void revogarSessoes() { tokenVersion++; }
    public void setAtivo(boolean ativo) {
        if (this.ativo != ativo) { this.ativo = ativo; revogarSessoes(); }
    }
    public void setPerfil(PerfilUsuario perfil) {
        if (perfil != null && this.perfil != perfil) { this.perfil = perfil; revogarSessoes(); }
    }
    public void alterarSenhaHash(String senhaHash) { this.senhaHash = senhaHash; revogarSessoes(); }
}
