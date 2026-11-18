package br.com.uchub.restaurante;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.service.*;
import br.com.uchub.restaurante.dto.GestaoRequests;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RepositoryRegressionTest {
    JdbcTemplate jdbc;
    @BeforeEach void setup(){
        var ds=new JdbcDataSource();ds.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");jdbc=new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE usuarios(id BIGINT PRIMARY KEY,nome VARCHAR(120))");
        jdbc.execute("CREATE TABLE mesas(id BIGINT PRIMARY KEY,numero INT,status VARCHAR(20))");
        jdbc.execute("CREATE TABLE pedidos(id BIGINT PRIMARY KEY,cliente_id BIGINT,mesa_id BIGINT,status VARCHAR(20),total DECIMAL(12,2),observacao VARCHAR(1000),criado_em TIMESTAMP)");
        jdbc.execute("CREATE TABLE pagamentos(pedido_id BIGINT,status VARCHAR(20),valor DECIMAL(12,2),forma_pagamento VARCHAR(30),referencia_transacao VARCHAR(120))");
        jdbc.execute("CREATE TABLE produtos(id BIGINT PRIMARY KEY,nome VARCHAR(140))");
        jdbc.execute("CREATE TABLE itens_pedido(id BIGINT,pedido_id BIGINT,produto_id BIGINT,produto_nome VARCHAR(140),quantidade INT,preco_unitario DECIMAL,subtotal DECIMAL,variacao_nome VARCHAR,adicionais_descricao VARCHAR,observacao_item VARCHAR)");
        jdbc.execute("CREATE TABLE ingredientes(id BIGINT AUTO_INCREMENT PRIMARY KEY,nome VARCHAR,unidade_medida VARCHAR,custo_unitario DECIMAL(12,4),quantidade_minima DECIMAL(12,3))");
        jdbc.update("INSERT INTO usuarios VALUES(1,'Cliente')");jdbc.update("INSERT INTO mesas VALUES(1,1,'OCUPADA')");
    }
    @Test void cursorCanReachOldPendingAndUnpaidOrders(){
        for(int id=1;id<=502;id++)jdbc.update("INSERT INTO pedidos VALUES(?,1,1,?,10,NULL,CURRENT_TIMESTAMP)",id,id==1?"RECEBIDO":"ENTREGUE");
        var repo=new PainelRepository(jdbc);var first=repo.pedidos();assertEquals(500,first.size());assertEquals(3L,((Number)first.getLast().get("id")).longValue());
        var older=repo.pedidos(3L);assertEquals(2,older.size());assertEquals("RECEBIDO",older.getLast().get("status"));assertFalse((Boolean)older.getFirst().get("pago"));
    }
    @Test void ingredientMinimumPersistsAndCanBeUpdated(){
        var repo=new GestaoRepository(jdbc);repo.ingrediente("Arroz","kg",new BigDecimal("10"),new BigDecimal("2.5"));
        var recursos=mock(RecursoService.class);var service=new GestaoService(repo,mock(UsuarioRepository.class),mock(PasswordEncoder.class),recursos);
        service.atualizarMinimo(1L,new GestaoRequests.Minimo(new BigDecimal("4.5")));
        assertEquals(0,new BigDecimal("4.5").compareTo(jdbc.queryForObject("SELECT quantidade_minima FROM ingredientes WHERE id=1",BigDecimal.class)));
    }
    @Test void manualReleaseRejectsActiveOrdersAndUnpaidOrdersWithCashier(){
        var recursos=mock(RecursoService.class);var service=new GestaoService(new GestaoRepository(jdbc),mock(UsuarioRepository.class),mock(PasswordEncoder.class),recursos);
        jdbc.update("INSERT INTO pedidos VALUES(1,1,1,'RECEBIDO',10,NULL,CURRENT_TIMESTAMP)");
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.liberarMesa(1L)).getStatusCode().value());
        jdbc.update("UPDATE pedidos SET status='ENTREGUE'");when(recursos.habilitado("CAIXA_E_PAGAMENTOS")).thenReturn(true);
        assertThrows(ResponseStatusException.class,()->service.liberarMesa(1L));
        when(recursos.habilitado("CAIXA_E_PAGAMENTOS")).thenReturn(false);service.liberarMesa(1L);
        assertEquals("LIVRE",jdbc.queryForObject("SELECT status FROM mesas WHERE id=1",String.class));
    }
    @Test void optionRecipeQuantitiesAreAvailableToAdmin(){
        jdbc.execute("CREATE TABLE variacoes_produto(id BIGINT,produto_id BIGINT,nome VARCHAR,preco DECIMAL,ativo BOOLEAN)");
        jdbc.execute("CREATE TABLE variacao_ingredientes(variacao_id BIGINT,ingrediente_id BIGINT,quantidade_necessaria DECIMAL(12,3))");
        jdbc.update("INSERT INTO variacoes_produto VALUES(10,1,'Grande',30,TRUE)");jdbc.update("INSERT INTO variacao_ingredientes VALUES(10,5,0.2)");
        var result=new ProdutoDetalhesRepository(jdbc).opcoesComInsumos(1,true);
        var recipe=(List<Map<String,Object>>)result.getFirst().get("ingredientes");assertEquals(5L,((Number)recipe.getFirst().get("ingredienteId")).longValue());assertEquals(0,new BigDecimal("0.2").compareTo((BigDecimal)recipe.getFirst().get("quantidade")));
    }

    @Test void payingAnOldOrderDoesNotReleaseTableWithAnotherPendingOrder(){
        var service=new GestaoService(new GestaoRepository(jdbc),mock(UsuarioRepository.class),mock(PasswordEncoder.class),mock(RecursoService.class));
        jdbc.update("INSERT INTO pedidos VALUES(1,1,1,'ENTREGUE',10,NULL,CURRENT_TIMESTAMP)");
        jdbc.update("INSERT INTO pedidos VALUES(2,1,1,'RECEBIDO',10,NULL,CURRENT_TIMESTAMP)");
        service.pagar(1L,new GestaoRequests.Pagamento("PIX","payment-1"));
        assertEquals("OCUPADA",jdbc.queryForObject("SELECT status FROM mesas WHERE id=1",String.class));
        jdbc.update("UPDATE pedidos SET status='ENTREGUE' WHERE id=2");
        service.pagar(2L,new GestaoRequests.Pagamento("PIX","payment-2"));
        assertEquals("LIVRE",jdbc.queryForObject("SELECT status FROM mesas WHERE id=1",String.class));
    }
}
