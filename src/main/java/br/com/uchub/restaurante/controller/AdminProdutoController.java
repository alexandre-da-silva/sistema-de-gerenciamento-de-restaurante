package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/admin/produtos")
public class AdminProdutoController {
    private final ProdutoService produtos;
    public AdminProdutoController(ProdutoService produtos) { this.produtos = produtos; }
    @GetMapping public List<ProdutoResponse> listar() { return produtos.listarTodos(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) { return produtos.criar(request); }
    @PutMapping("/{id}") public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) { return produtos.atualizar(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { produtos.excluir(id); }
}
