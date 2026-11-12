package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AlterarSenhaRequest(@NotBlank String senhaAtual,
                                  @NotBlank @Size(min=12, max=72) String novaSenha) {}
