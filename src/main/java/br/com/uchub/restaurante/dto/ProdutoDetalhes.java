package br.com.uchub.restaurante.dto;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record ProdutoDetalhes(
 @Size(max=60) String sku,@Size(max=60) String setorProducao,@Size(max=120) String porcao,
 @Size(max=7) List<@Min(1) @Max(7) Integer> diasSemana,
 @Pattern(regexp="^([01][0-9]|2[0-3]):[0-5][0-9]$") String inicio,
 @Pattern(regexp="^([01][0-9]|2[0-3]):[0-5][0-9]$") String fim,
 boolean controlaEstoque,Boolean permiteObservacoes,@Min(0) @Max(99999) Integer ordemExibicao,
 @Size(max=60) List<@Valid Insumo> fichaTecnica,
 @Size(max=20) List<@Valid Variacao> variacoes,@Size(max=30) List<@Valid Adicional> adicionais,
 @Size(max=30) List<@Size(max=80) String> alergenos,@Size(max=30) List<@Size(max=50) String> tags,
 @DecimalMin("0") @DecimalMax("100000") BigDecimal calorias,@Size(max=1000) String nutricional,
 @Valid Fiscal fiscal) {
 public record Insumo(@NotNull @Positive Long ingredienteId,@NotNull @DecimalMin("0.001") @DecimalMax("100000") @Digits(integer=6,fraction=3) BigDecimal quantidade) {}
 public record Variacao(@NotBlank @Pattern(regexp="[a-zA-Z0-9-]{1,60}") String id,@NotBlank @Size(max=80) String nome,
 @NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal preco,
 @NotNull @DecimalMin("0.001") @DecimalMax("100") @Digits(integer=3,fraction=3) BigDecimal fatorReceita) {}
 public record Adicional(@NotBlank @Pattern(regexp="[a-zA-Z0-9-]{1,60}") String id,@NotBlank @Size(max=80) String nome,
 @NotNull @DecimalMin("0") @Digits(integer=8,fraction=2) BigDecimal preco,@Size(max=30) List<@Valid Insumo> insumos) {}
 public record Fiscal(@Pattern(regexp="[0-9]{8}") String ncm,@Pattern(regexp="[0-9]{4}") String cfop,
 @Pattern(regexp="[0-9]{2,3}") String cstCsosn,
 @DecimalMin("0") @DecimalMax("100") BigDecimal aliquotaIcms,
 @DecimalMin("0") @DecimalMax("100") BigDecimal aliquotaPis,
 @DecimalMin("0") @DecimalMax("100") BigDecimal aliquotaCofins) {}
 public static ProdutoDetalhes vazio(){return new ProdutoDetalhes(null,"Cozinha",null,List.of(),null,null,false,true,0,List.of(),List.of(),List.of(),List.of(),List.of(),null,null,null);}
 public <T> List<T> lista(List<T> value){return value==null?List.of():value;}
 public ProdutoDetalhes publico(){return new ProdutoDetalhes(sku,setorProducao,porcao,diasSemana,inicio,fim,false,permiteObservacoes,ordemExibicao,List.of(),variacoes,
 lista(adicionais).stream().map(a->new Adicional(a.id(),a.nome(),a.preco(),List.of())).toList(),alergenos,tags,calorias,nutricional,null);}
}
