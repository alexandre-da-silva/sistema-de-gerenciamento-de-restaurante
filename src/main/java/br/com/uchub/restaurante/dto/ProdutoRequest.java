package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProdutoRequest(@NotBlank @Size(max=140) String nome,
 @Size(max=1000) String descricao, @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal preco,
 Long categoriaId, Boolean ativo,
 @Pattern(regexp="^/media/[a-f0-9-]+\\.jpg$") @Size(max=255) String imagemUrl,
 @Min(1) @Max(240) Integer tempoPreparo, @Size(max=60) String tipo, Boolean destaque,
 @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal precoPromocional) {}
