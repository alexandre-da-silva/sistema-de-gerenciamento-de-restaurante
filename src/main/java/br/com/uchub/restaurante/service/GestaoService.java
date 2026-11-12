package br.com.uchub.restaurante.service;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.dto.GestaoRequests.*;
import br.com.uchub.restaurante.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;

@Service
public class GestaoService {
    private final GestaoRepository repository;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final RecursoService recursos;
    public GestaoService(GestaoRepository repository, UsuarioRepository usuarios, PasswordEncoder encoder, RecursoService recursos) {
        this.repository=repository; this.usuarios=usuarios; this.encoder=encoder; this.recursos=recursos;
    }
    @Transactional
    public void usuario(br.com.uchub.restaurante.dto.GestaoRequests.Usuario request) {
        usuarios.save(new Usuario(request.nome(),request.email().trim().toLowerCase(java.util.Locale.ROOT),encoder.encode(request.senha()),null,request.perfil()));
    }
    @Transactional
    public void movimentar(long id, Movimento request, String email) {
        recursos.exigirHabilitado("CONTROLE_ESTOQUE");
        var jdbc=repository.jdbc();
        var saldos=jdbc.queryForList("SELECT quantidade_atual FROM ingredientes WHERE id=? FOR UPDATE",BigDecimal.class,id);
        if(saldos.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if(request.quantidade().signum()==0 || saldos.getFirst().add(request.quantidade()).signum()<0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Movimentação inválida ou saldo insuficiente.");
        long usuario=usuarios.findByEmail(email).orElseThrow().getId();
        jdbc.update("UPDATE ingredientes SET quantidade_atual=quantidade_atual+? WHERE id=?",request.quantidade(),id);
        jdbc.update("INSERT INTO movimentacoes_estoque(ingrediente_id,tipo,quantidade,motivo,usuario_id) VALUES (?,?,?,?,?)",
            id,request.quantidade().signum()>0 ? "ENTRADA":"SAIDA",request.quantidade().abs(),request.motivo(),usuario);
    }
    @Transactional
    public void pagar(long id, Pagamento request) {
        recursos.exigirHabilitado("CAIXA_E_PAGAMENTOS");
        var jdbc=repository.jdbc();
        var pedidos=jdbc.queryForList("SELECT total,status,mesa_id FROM pedidos WHERE id=? FOR UPDATE",id);
        if(pedidos.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var pedido=pedidos.getFirst();
        var anteriores=jdbc.queryForList("SELECT referencia_transacao FROM pagamentos WHERE pedido_id=? AND status='CONFIRMADO'",id);
        if(!anteriores.isEmpty()) {
            if(request.referencia().equals(anteriores.getFirst().get("referencia_transacao"))) return;
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Pedido já pago.");
        }
        if(!"ENTREGUE".equals(pedido.get("status"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Finalize a entrega antes de fechar a conta.");
        jdbc.update("INSERT INTO pagamentos(pedido_id,valor,forma_pagamento,status,referencia_transacao) VALUES (?,?,?,'CONFIRMADO',?)",id,pedido.get("total"),request.forma(),request.referencia());
        if(pedido.get("mesa_id")!=null) jdbc.update("UPDATE mesas SET status='LIVRE' WHERE id=?",pedido.get("mesa_id"));
    }
}
