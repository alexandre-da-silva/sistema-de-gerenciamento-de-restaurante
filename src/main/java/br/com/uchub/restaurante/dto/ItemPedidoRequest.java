package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.*;
import java.util.List;
public record ItemPedidoRequest(@NotNull Long produtoId,@NotNull @Min(1) @Max(99) Integer quantidade,
 @Size(max=60) String variacaoId,@Size(max=30) List<@NotBlank @Size(max=60) String> adicionais,@Size(max=300) String observacao) {
 public ItemPedidoRequest(Long id,Integer q){this(id,q,null,List.of(),null);}
}
