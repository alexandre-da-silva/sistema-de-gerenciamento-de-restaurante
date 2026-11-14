package br.com.uchub.restaurante.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.*;

@RestController
public class ExperienciaClienteController {
 private final JdbcTemplate jdbc;
 public ExperienciaClienteController(JdbcTemplate jdbc) {this.jdbc=jdbc;}
 private long usuario(String email) {return jdbc.queryForObject("SELECT id FROM usuarios WHERE email=? AND ativo=TRUE",Long.class,email);}
 private void dono(long pedido,String email) {
  Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM pedidos p JOIN usuarios u ON u.id=p.cliente_id WHERE p.id=? AND u.email=?",Integer.class,pedido,email);
  if(count==null||count==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Pedido não encontrado.");
 }
 @GetMapping("/api/v1/cliente/mesas") public List<Map<String,Object>> mesas() {
  Boolean ativo=jdbc.queryForObject("SELECT habilitado FROM recursos_sistema WHERE codigo='MESAS_E_COMANDAS'",Boolean.class);
  return Boolean.TRUE.equals(ativo)?jdbc.queryForList("SELECT id,numero,capacidade_pessoas FROM mesas WHERE status='LIVRE' ORDER BY numero"):List.of();
 }
 @GetMapping("/api/v1/publico/avaliacoes") public Map<String,Object> avaliacoes(@RequestParam(defaultValue="0") @Min(0) int pagina,@RequestParam(defaultValue="0") int nota) {
  if(pagina<0||pagina>10000||nota<0||nota>5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  var filtro=nota==0?"":" WHERE a.nota=?";
  Object[] args=nota==0?new Object[]{pagina*6}:new Object[]{nota,pagina*6};
  var lista=jdbc.queryForList("SELECT a.id,a.pedido_id,a.produto_id,a.nota,a.comentario,a.criado_em,SUBSTRING_INDEX(u.nome,' ',1) AS cliente,p.nome AS produto,p.imagem_url FROM avaliacoes a JOIN usuarios u ON u.id=a.cliente_id JOIN produtos p ON p.id=a.produto_id"+filtro+" ORDER BY a.id DESC LIMIT 6 OFFSET ?",args);
  var resumo=jdbc.queryForMap("SELECT COUNT(*) total,COALESCE(AVG(nota),0) media,COALESCE(SUM(nota>=4),0) positivas,COALESCE(SUM(nota=3),0) neutras,COALESCE(SUM(nota<=2),0) negativas FROM avaliacoes");
  var distribuicao=jdbc.queryForList("SELECT nota,COUNT(*) quantidade FROM avaliacoes GROUP BY nota");
  Long filtrado=nota==0?jdbc.queryForObject("SELECT COUNT(*) FROM avaliacoes",Long.class):jdbc.queryForObject("SELECT COUNT(*) FROM avaliacoes WHERE nota=?",Long.class,nota);
  var destaque=jdbc.queryForList("SELECT p.id,p.nome,p.imagem_url,AVG(a.nota) media,COUNT(*) quantidade FROM avaliacoes a JOIN produtos p ON p.id=a.produto_id GROUP BY p.id,p.nome,p.imagem_url ORDER BY quantidade DESC,media DESC LIMIT 1");
  return Map.of("itens",lista,"resumo",resumo,"distribuicao",distribuicao,"totalFiltrado",filtrado,"destaque",destaque);
 }
 public record Avaliacao(@NotNull Long pedidoId,@NotNull Long produtoId,@Min(1) @Max(5) int nota,@NotBlank @Size(max=1000) String comentario) {}
 @PostMapping("/api/v1/cliente/avaliacoes") @ResponseStatus(HttpStatus.CREATED)
 public void avaliar(@Valid @RequestBody Avaliacao a,Principal principal) {
  dono(a.pedidoId(),principal.getName());
  Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM pedidos p JOIN itens_pedido i ON i.pedido_id=p.id WHERE p.id=? AND p.status='ENTREGUE' AND i.produto_id=?",Integer.class,a.pedidoId(),a.produtoId());
  if(count==null||count==0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Avalie um prato de um pedido já finalizado.");
  jdbc.update("INSERT INTO avaliacoes(pedido_id,produto_id,cliente_id,nota,comentario) VALUES(?,?,?,?,?)",a.pedidoId(),a.produtoId(),usuario(principal.getName()),a.nota(),a.comentario().trim());
 }
 @GetMapping("/api/v1/cliente/perfil") public Map<String,Object> perfil(Principal p) {return jdbc.queryForMap("SELECT nome,email,telefone FROM usuarios WHERE id=?",usuario(p.getName()));}
 public record Perfil(@NotBlank @Size(max=120) String nome,@Size(max=25) String telefone) {}
 @PutMapping("/api/v1/cliente/perfil") public void perfil(@Valid @RequestBody Perfil perfil,Principal p) {jdbc.update("UPDATE usuarios SET nome=?,telefone=? WHERE id=?",perfil.nome().trim(),perfil.telefone(),usuario(p.getName()));}
 public record Mensagem(@NotBlank @Size(max=1000) String texto) {}
 @GetMapping("/api/v1/cliente/pedidos/{id}/mensagens") public List<Map<String,Object>> mensagens(@PathVariable long id,Principal p) {dono(id,p.getName());return listar(id);}
 @PostMapping("/api/v1/cliente/pedidos/{id}/mensagens") public void mensagem(@PathVariable long id,@Valid @RequestBody Mensagem m,Principal p) {dono(id,p.getName());enviar(id,m,p);}
 @GetMapping("/api/v1/admin/pedidos/{id}/mensagens") public List<Map<String,Object>> adminMensagens(@PathVariable long id) {return listar(id);}
 @PostMapping("/api/v1/admin/pedidos/{id}/mensagens") public void adminMensagem(@PathVariable long id,@Valid @RequestBody Mensagem m,Principal p) {enviar(id,m,p);}
 private List<Map<String,Object>> listar(long id) {return jdbc.queryForList("SELECT m.id,m.texto,m.criado_em,u.perfil FROM mensagens_pedido m JOIN usuarios u ON u.id=m.usuario_id WHERE pedido_id=? ORDER BY m.id DESC LIMIT 100",id);}
 private void enviar(long id,Mensagem m,Principal p) {jdbc.update("INSERT INTO mensagens_pedido(pedido_id,usuario_id,texto) VALUES(?,?,?)",id,usuario(p.getName()),m.texto().trim());}
}
