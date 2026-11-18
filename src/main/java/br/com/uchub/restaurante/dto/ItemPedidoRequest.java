package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.*;
import java.util.List;
public record ItemPedidoRequest(@NotNull @Positive Long produtoId,@NotNull @Min(1) @Max(99) Integer quantidade,
 @Positive Long variacaoId,@Size(max=20) List<@NotNull @Positive Long> adicionaisIds,
 @Size(max=500) String observacao, @DecimalMin("0.01") @Digits(integer=10,fraction=2) java.math.BigDecimal precoEsperado) {}
