package br.com.uchub.restaurante.dto;
import br.com.uchub.restaurante.model.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoResponse(Long id, String cliente, Integer mesa, StatusPedido status,
                             BigDecimal total, String observacao, LocalDateTime criadoEm,
                             List<Item> itens) {
    public record Item(Long produtoId, String produto, Integer quantidade,
                       BigDecimal precoUnitario, BigDecimal subtotal) {}
}
