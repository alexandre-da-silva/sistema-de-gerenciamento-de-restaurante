package br.com.uchub.restaurante.security;

import br.com.uchub.restaurante.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final long expirationMinutes;
    public JwtService(JwtEncoder encoder,
                      @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.encoder = encoder; this.expirationMinutes = expirationMinutes;
    }
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("uc-hub-restaurante")
                .issuedAt(agora)
                .expiresAt(agora.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil().name())
                .claim("authorities", List.of("ROLE_" + usuario.getPerfil().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
    public long getExpirationSeconds() { return expirationMinutes * 60; }
}
