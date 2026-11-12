package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.NotNull;
public record AlterarRecursoRequest(@NotNull Boolean habilitado) {}
