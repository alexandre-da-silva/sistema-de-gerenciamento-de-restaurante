package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import br.com.uchub.restaurante.model.PerfilUsuario;
public class GestaoRequests {
    public record Categoria(@NotBlank @Size(max=80) String nome, @Size(max=255) String descricao) {}
    public record Mesa(@Min(1) int numero, @Min(1) int capacidade) {}
    public record Ingrediente(@NotBlank @Size(max=120) String nome, @NotBlank @Size(max=20) String unidade) {}
    public record Movimento(@NotNull @Digits(integer=9,fraction=3) BigDecimal quantidade, @NotBlank @Size(max=255) String motivo) {}
    public record Pagamento(@NotBlank @Pattern(regexp="PIX|DINHEIRO|CARTAO") String forma, @NotBlank @Size(max=120) String referencia) {}
    public record Usuario(@NotBlank @Size(max=120) String nome, @Email @NotBlank @Size(max=180) String email,
        @NotBlank @Size(min=12,max=60) String senha, @NotNull PerfilUsuario perfil) {}
}
