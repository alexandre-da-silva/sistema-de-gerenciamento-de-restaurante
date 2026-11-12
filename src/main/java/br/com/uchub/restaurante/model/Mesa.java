package br.com.uchub.restaurante.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mesas")
public class Mesa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Integer numero;
    @Column(nullable = false, length = 20)
    private String status = "LIVRE";
    @Column(name = "capacidade_pessoas")
    private Integer capacidadePessoas;
    protected Mesa() {}
    public Long getId() { return id; }
    public Integer getNumero() { return numero; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
