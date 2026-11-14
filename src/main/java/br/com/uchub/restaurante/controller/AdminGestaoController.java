package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.repository.GestaoRepository;
import br.com.uchub.restaurante.service.*;
import br.com.uchub.restaurante.dto.GestaoRequests.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminGestaoController {
    private final GestaoRepository repository;
    private final GestaoService service;
    private final RecursoService recursos;
    public AdminGestaoController(GestaoRepository repository, GestaoService service, RecursoService recursos) {
        this.repository=repository; this.service=service; this.recursos=recursos;
    }
    public record NovoInsumo(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=120) String nome,@jakarta.validation.constraints.Pattern(regexp="kg|g|L|ml|un") @jakarta.validation.constraints.NotNull String unidade,@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0") @jakarta.validation.constraints.Digits(integer=8,fraction=4) java.math.BigDecimal custoUnitario) {}
    @PostMapping("/insumos") public void novoInsumo(@Valid @RequestBody NovoInsumo i){repository.jdbc().update("INSERT INTO ingredientes(nome,unidade_medida,custo_unitario) VALUES(?,?,?)",i.nome().trim(),i.unidade(),i.custoUnitario());}
    @GetMapping("/insumos") public List<Map<String,Object>> insumos(){return repository.estoque();}
    public record Custo(@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0") @jakarta.validation.constraints.Digits(integer=8,fraction=4) java.math.BigDecimal custoUnitario) {}
    @PutMapping("/insumos/{id}/custo") public void custo(@PathVariable long id,@Valid @RequestBody Custo c){if(repository.jdbc().update("UPDATE ingredientes SET custo_unitario=? WHERE id=?",c.custoUnitario(),id)==0)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);}
    @GetMapping("/usuarios") public List<Map<String,Object>> usuarios() { return repository.usuarios(); }
    @PostMapping("/usuarios") public void usuario(@Valid @RequestBody Usuario request) { service.usuario(request); }
    @GetMapping("/categorias") public List<Map<String,Object>> categorias() { return repository.categorias(); }
    @PostMapping("/categorias") public void categoria(@Valid @RequestBody Categoria request) { repository.categoria(request.nome(),request.descricao()); }
    @GetMapping("/mesas") public List<Map<String,Object>> mesas() { recursos.exigirHabilitado("MESAS_E_COMANDAS"); return repository.mesas(); }
    @PostMapping("/mesas") public void mesa(@Valid @RequestBody Mesa request) { recursos.exigirHabilitado("MESAS_E_COMANDAS"); repository.mesa(request.numero(),request.capacidade()); }
    @GetMapping("/estoque") public List<Map<String,Object>> estoque() { recursos.exigirHabilitado("CONTROLE_ESTOQUE"); return repository.estoque(); }
    @PostMapping("/estoque") public void ingrediente(@Valid @RequestBody Ingrediente request) { recursos.exigirHabilitado("CONTROLE_ESTOQUE"); repository.ingrediente(request.nome(),request.unidade()); }
    @PostMapping("/estoque/{id}/movimentos") public void movimento(@PathVariable long id, @Valid @RequestBody Movimento request, Principal principal) { service.movimentar(id,request,principal.getName()); }
    @GetMapping("/pedidos") public List<Map<String,Object>> pedidos() { return repository.pedidos(); }
    @PostMapping("/pedidos/{id}/pagamentos") public void pagamento(@PathVariable long id,@Valid @RequestBody Pagamento request) { service.pagar(id,request); }
    @GetMapping("/relatorios/vendas") public List<Map<String,Object>> vendas() { recursos.exigirHabilitado("RELATORIOS"); return repository.vendas(); }
}
