package br.com.uchub.restaurante.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record PedidoRequest(Long mesaId,@NotEmpty @Size(max=50) List<@Valid ItemPedidoRequest> itens,
 @Size(max=1000) String observacao,Boolean taxaServico,
 @NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") String referencia) {}
