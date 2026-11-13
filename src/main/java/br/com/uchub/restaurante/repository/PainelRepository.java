package br.com.uchub.restaurante.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class PainelRepository {
    private final JdbcTemplate jdbc;
    public PainelRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> pedidos() {
        var pedidos = jdbc.queryForList("""
            SELECT p.id,p.cliente_id,p.mesa_id,m.numero AS mesa,u.nome AS cliente,
                   p.status,p.total,p.observacao,p.criado_em,
                   EXISTS(SELECT 1 FROM pagamentos pg WHERE pg.pedido_id=p.id AND pg.status='CONFIRMADO') AS pago
            FROM pedidos p JOIN usuarios u ON u.id=p.cliente_id LEFT JOIN mesas m ON m.id=p.mesa_id
            ORDER BY p.id DESC LIMIT 500
            """);
        if (pedidos.isEmpty()) return pedidos;
        String placeholders = String.join(",", Collections.nCopies(pedidos.size(), "?"));
        var itens = jdbc.queryForList("SELECT i.pedido_id,i.produto_id,p.nome AS produto,i.quantidade,i.preco_unitario,i.subtotal FROM itens_pedido i JOIN produtos p ON p.id=i.produto_id WHERE i.pedido_id IN (" + placeholders + ") ORDER BY i.id", pedidos.stream().map(p -> p.get("id")).toArray());
        Map<Long, List<Map<String, Object>>> agrupados = new HashMap<>();
        for (var item : itens) agrupados.computeIfAbsent(((Number)item.get("pedido_id")).longValue(), id -> new ArrayList<>()).add(item);
        for (var pedido : pedidos) pedido.put("itens", agrupados.getOrDefault(((Number)pedido.get("id")).longValue(), List.of()));
        return pedidos;
    }

    public List<Map<String, Object>> clientes() {
        return jdbc.queryForList("""
            SELECT u.id,u.nome,u.email,u.telefone,u.ativo,u.criado_em,
                   COUNT(p.id) AS pedidos,COALESCE(SUM(CASE WHEN p.status<>'CANCELADO' THEN p.total ELSE 0 END),0) AS total,
                   MAX(p.criado_em) AS ultimo_pedido
            FROM usuarios u LEFT JOIN pedidos p ON p.cliente_id=u.id
            WHERE u.perfil='CLIENTE' GROUP BY u.id ORDER BY u.nome LIMIT 500
            """);
    }

    public Map<String, Object> resumo(LocalDate data, boolean caixa, boolean estoque) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("pedidosHoje", contarPedidos(data));
        result.put("pedidosOntem", contarPedidos(data.minusDays(1)));
        result.put("mesasTotal", jdbc.queryForObject("SELECT COUNT(*) FROM mesas", Long.class));
        result.put("mesasOcupadas", jdbc.queryForObject("SELECT COUNT(*) FROM mesas WHERE status='OCUPADA'", Long.class));
        result.put("estoqueHabilitado", estoque);
        result.put("caixaHabilitado", caixa);
        result.put("estoqueBaixo", estoque ? jdbc.queryForObject("SELECT COUNT(*) FROM ingredientes WHERE ativo=TRUE AND quantidade_atual<=quantidade_minima", Long.class) : null);
        result.put("faturamento", caixa ? faturamento(data) : null);
        result.put("faturamentoOntem", caixa ? faturamento(data.minusDays(1)) : null);
        result.put("vendasPorHora", caixa ? jdbc.queryForList("SELECT HOUR(criado_em) AS hora,SUM(valor) AS total FROM pagamentos WHERE status='CONFIRMADO' AND criado_em>=? AND criado_em<? GROUP BY HOUR(criado_em) ORDER BY hora", data.atStartOfDay(), data.plusDays(1).atStartOfDay()) : List.of());
        result.put("maisVendidos", caixa ? jdbc.queryForList("""
            SELECT pr.id,pr.nome,SUM(i.quantidade) AS quantidade,SUM(i.subtotal) AS total
            FROM itens_pedido i JOIN produtos pr ON pr.id=i.produto_id
            WHERE EXISTS (SELECT 1 FROM pagamentos pg WHERE pg.pedido_id=i.pedido_id AND pg.status='CONFIRMADO' AND pg.criado_em>=? AND pg.criado_em<?)
            GROUP BY pr.id,pr.nome ORDER BY quantidade DESC,total DESC LIMIT 3
            """, data.atStartOfDay(), data.plusDays(1).atStartOfDay()) : List.of());
        return result;
    }
    private Long contarPedidos(LocalDate data) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM pedidos WHERE criado_em>=? AND criado_em<? AND status<>'CANCELADO'", Long.class, data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }
    private java.math.BigDecimal faturamento(LocalDate data) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(valor),0) FROM pagamentos WHERE status='CONFIRMADO' AND criado_em>=? AND criado_em<?", java.math.BigDecimal.class, data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }
}
