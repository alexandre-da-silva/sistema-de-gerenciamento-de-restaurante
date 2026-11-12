package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.RecursoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/admin/recursos")
public class AdminRecursoController {
    private final RecursoService recursos;
    public AdminRecursoController(RecursoService recursos) { this.recursos = recursos; }
    @GetMapping public List<RecursoResponse> listar() { return recursos.listar(); }
    @PutMapping("/{codigo}") public RecursoResponse alterar(@PathVariable String codigo, @Valid @RequestBody AlterarRecursoRequest request) {
        return recursos.alterar(codigo, request.habilitado());
    }
}
