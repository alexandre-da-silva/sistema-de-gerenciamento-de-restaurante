package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProdutoRequest(@NotBlank @Size(max=140) String nome,
                             @Size(max=1000) String descricao,
                             @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal preco,
                             Long categoriaId,
                             Boolean ativo) {}
