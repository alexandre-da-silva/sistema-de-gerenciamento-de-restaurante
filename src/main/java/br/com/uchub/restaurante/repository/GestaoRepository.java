package br.com.uchub.restaurante.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class GestaoRepository {
    private final JdbcTemplate jdbc;
    public GestaoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Map<String, Object>> usuarios() {
        return jdbc.queryForList("SELECT id,nome,email,perfil,ativo FROM usuarios ORDER BY id LIMIT 500");
    }
    public List<Map<String, Object>> mesas() { return jdbc.queryForList("SELECT * FROM mesas ORDER BY numero"); }
    public List<Map<String, Object>> categorias() { return jdbc.queryForList("SELECT * FROM categorias ORDER BY nome"); }
    public List<Map<String, Object>> estoque() { return jdbc.queryForList("SELECT * FROM ingredientes ORDER BY nome LIMIT 500"); }
    public List<Map<String, Object>> vendas() {
        return jdbc.queryForList("SELECT DATE(criado_em) dia, SUM(valor) total, COUNT(*) pagamentos FROM pagamentos WHERE status='CONFIRMADO' GROUP BY DATE(criado_em) ORDER BY dia DESC LIMIT 365");
    }
    public List<Map<String, Object>> pedidos() { return jdbc.queryForList("SELECT * FROM pedidos ORDER BY id DESC LIMIT 500"); }
    public void categoria(String nome, String descricao) { jdbc.update("INSERT INTO categorias(nome,descricao) VALUES (?,?)", nome, descricao); }
    public void mesa(int numero, int capacidade) { jdbc.update("INSERT INTO mesas(numero,capacidade_pessoas) VALUES (?,?)",numero,capacidade); }
    public void ingrediente(String nome, String unidade) { jdbc.update("INSERT INTO ingredientes(nome,unidade_medida) VALUES (?,?)",nome,unidade); }
    public JdbcTemplate jdbc() { return jdbc; }
}
