package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.ProdutoResponse;
import br.com.uchub.restaurante.service.ProdutoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/publico")
public class PublicoController {
    private final ProdutoService produtos;
    public PublicoController(ProdutoService produtos) { this.produtos = produtos; }
    @GetMapping("/cardapio") public List<ProdutoResponse> cardapio() { return produtos.listarPublicos(); }
}
