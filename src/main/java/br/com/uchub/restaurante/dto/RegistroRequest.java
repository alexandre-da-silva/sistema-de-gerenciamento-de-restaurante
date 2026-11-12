package br.com.uchub.restaurante.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegistroRequest(@NotBlank @Size(max=120) String nome,
                              @NotBlank @Email @Size(max=180) String email,
                              @NotBlank @Size(min=10, max=72) String senha,
                              @Size(max=25) String telefone) {}
