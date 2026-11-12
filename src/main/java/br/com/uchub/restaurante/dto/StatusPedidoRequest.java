package br.com.uchub.restaurante.dto;
import br.com.uchub.restaurante.model.StatusPedido;
import jakarta.validation.constraints.NotNull;
public record StatusPedidoRequest(@NotNull StatusPedido status) {}
