package br.com.uchub.restaurante.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record ProdutoRequest(
 @NotBlank @Size(max=140) String nome, @Size(max=1000) String descricao,
 @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal preco,
 Long categoriaId, Boolean ativo,
 @Pattern(regexp="^/media/[a-f0-9-]+\\.jpg$") @Size(max=255) String imagemUrl,
 @Min(1) @Max(240) Integer tempoPreparo, @Size(max=60) String tipo, Boolean destaque,
 @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal precoPromocional,
 @Size(max=60) String sku, @Pattern(regexp="COZINHA|BAR|CHURRASQUEIRA|CONFEITARIA|OUTRO") String setorProducao,
 @Size(max=100) String porcao, @Min(0) @Max(999999) Integer ordemExibicao,
 @Size(max=500) String observacoesPermitidas, @Size(max=500) String alergenos,
 @Size(max=500) String tags, @Min(0) @Max(100000) Integer calorias,
 @Pattern(regexp="[0-9]{8}") String ncm, @Pattern(regexp="[0-9]{4}") String cfop,
 @Pattern(regexp="[0-9]{2,4}") String cstCsosn,
 @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal aliquotaIcms,
 @Size(max=30) List<@Valid Variacao> variacoes,
 @Size(max=40) List<@Valid Adicional> adicionais,
 @Size(max=100) List<@Valid Insumo> fichaTecnica,
 @Size(max=35) List<@Valid Horario> disponibilidade
) {
 public record Variacao(@NotBlank @Size(max=80) String nome,
    @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal preco) {}
 public record Adicional(@NotBlank @Size(max=80) String nome,
    @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal preco) {}
 public record Insumo(@NotNull @Positive Long ingredienteId,
    @NotNull @DecimalMin("0.001") @Digits(integer=9,fraction=3) BigDecimal quantidade) {}
 public record Horario(@NotNull @Min(1) @Max(7) Integer diaSemana,
    @NotNull LocalTime horaInicio,@NotNull LocalTime horaFim) {}
}
