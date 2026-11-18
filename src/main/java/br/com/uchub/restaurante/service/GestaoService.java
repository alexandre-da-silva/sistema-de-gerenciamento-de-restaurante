package br.com.uchub.restaurante.service;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.dto.GestaoRequests.*;
import br.com.uchub.restaurante.model.Usuario;
import br.com.uchub.restaurante.model.PerfilUsuario;
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
    public void atualizarUsuario(long id, Boolean ativo, PerfilUsuario perfil, String administrador) {
        if(ativo==null && perfil==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Informe os campos a atualizar.");
        Usuario alvo=usuarios.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário não encontrado."));
        if(alvo.getEmail().equals(administrador)
            && (Boolean.FALSE.equals(ativo)||perfil!=null && perfil!=PerfilUsuario.ADMIN))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Não é possível retirar sua própria autorização administrativa.");
        boolean deixaDeSerAdmin=alvo.getPerfil()==PerfilUsuario.ADMIN && alvo.isAtivo()
            && (Boolean.FALSE.equals(ativo) || perfil!=null && perfil!=PerfilUsuario.ADMIN);
        if(deixaDeSerAdmin && usuarios.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)<=1)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"O último administrador ativo não pode ser removido.");
        if(perfil!=null)alvo.setPerfil(perfil);
        if(ativo!=null)alvo.setAtivo(ativo);
        // As mudanças revogam automaticamente os JWTs emitidos antes.
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
    public void atualizarCusto(long id,Custo request){
        int linhas=repository.jdbc().update("UPDATE ingredientes SET custo_unitario=? WHERE id=?",request.custoUnitario(),id);
        if(linhas==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ingrediente não encontrado.");
    }
    @Transactional
    public void atualizarMinimo(long id,Minimo request){
        recursos.exigirHabilitado("CONTROLE_ESTOQUE");
        if(repository.jdbc().update("UPDATE ingredientes SET quantidade_minima=? WHERE id=?",request.quantidadeMinima(),id)==0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ingrediente não encontrado.");
    }
    @Transactional
    public void liberarMesa(long id){
        recursos.exigirHabilitado("MESAS_E_COMANDAS");
        var jdbc=repository.jdbc();
        if(jdbc.queryForList("SELECT id FROM mesas WHERE id=? FOR UPDATE",Long.class,id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Mesa não encontrada.");
        Integer emAndamento=jdbc.queryForObject("SELECT COUNT(*) FROM pedidos WHERE mesa_id=? AND status NOT IN ('ENTREGUE','CANCELADO')",Integer.class,id);
        if(emAndamento!=null && emAndamento>0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Conclua ou cancele os pedidos antes de liberar a mesa.");
        if(recursos.habilitado("CAIXA_E_PAGAMENTOS")){
            Integer semPagamento=jdbc.queryForObject("SELECT COUNT(*) FROM pedidos p WHERE p.mesa_id=? AND p.status='ENTREGUE' AND NOT EXISTS (SELECT 1 FROM pagamentos pg WHERE pg.pedido_id=p.id AND pg.status='CONFIRMADO')",Integer.class,id);
            if(semPagamento!=null && semPagamento>0)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Confirme os pagamentos antes de liberar a mesa.");
        }
        jdbc.update("UPDATE mesas SET status='LIVRE' WHERE id=?",id);
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
        if(pedido.get("mesa_id")!=null) jdbc.update(
            "UPDATE mesas SET status='LIVRE' WHERE id=? AND NOT EXISTS ("
            + "SELECT 1 FROM pedidos p WHERE p.mesa_id=mesas.id AND p.status<>'CANCELADO' "
            + "AND NOT EXISTS (SELECT 1 FROM pagamentos pg WHERE pg.pedido_id=p.id AND pg.status='CONFIRMADO'))",
            pedido.get("mesa_id"));
    }
}
