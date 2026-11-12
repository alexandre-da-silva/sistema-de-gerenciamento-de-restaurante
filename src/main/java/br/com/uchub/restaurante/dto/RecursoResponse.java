package br.com.uchub.restaurante.dto;
import java.time.LocalDateTime;
public record RecursoResponse(String codigo, String descricao, boolean habilitado, LocalDateTime atualizadoEm) {}
