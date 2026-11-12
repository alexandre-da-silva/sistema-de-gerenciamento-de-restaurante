package br.com.uchub.restaurante.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
public record PedidoRequest(Long mesaId, @NotEmpty List<@Valid ItemPedidoRequest> itens,
                            @Size(max=1000) String observacao) {}
