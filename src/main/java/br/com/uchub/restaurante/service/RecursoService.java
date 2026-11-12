package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.RecursoSistema;
import br.com.uchub.restaurante.repository.RecursoSistemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class RecursoService {
    private final RecursoSistemaRepository repository;
    public RecursoService(RecursoSistemaRepository repository) { this.repository = repository; }
    public void exigirHabilitado(String codigo) {
        RecursoSistema recurso = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Recurso não configurado."));
        if (!recurso.isHabilitado()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Este recurso está desabilitado pelo administrador.");
    }
    public List<RecursoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }
    @Transactional
    public RecursoResponse alterar(String codigo, boolean habilitado) {
        if (habilitado && (codigo.equals("EMISSAO_NFE") || codigo.equals("EMISSAO_NFCE")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Integração fiscal ainda pendente de configuração e homologação.");
        RecursoSistema recurso = repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado."));
        recurso.alterarHabilitado(habilitado);
        return toResponse(recurso);
    }
    private RecursoResponse toResponse(RecursoSistema r) {
        return new RecursoResponse(r.getCodigo(), r.getDescricao(), r.isHabilitado(), r.getAtualizadoEm());
    }
}
