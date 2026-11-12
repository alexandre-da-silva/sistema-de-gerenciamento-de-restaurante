package br.com.uchub.restaurante.dto;
public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds,
                            String nome, String email, String perfil) {}
