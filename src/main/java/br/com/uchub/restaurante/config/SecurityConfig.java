package br.com.uchub.restaurante.config;

import br.com.uchub.restaurante.repository.UsuarioRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret:}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("APP_JWT_SECRET precisa ter pelo menos 32 bytes.");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getEncoded()));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Valida emissor, expiração, nbf e exige sub e exp presentes.
        OAuth2TokenValidator<Jwt> claimsObrigatorias = jwt -> {
            if (jwt.getExpiresAt() == null || jwt.getSubject() == null
                    || jwt.getSubject().isBlank()) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token", "JWT sem expiração ou identidade válida.", null));
            }
            return OAuth2TokenValidatorResult.success();
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer("uc-hub-restaurante"),
                claimsObrigatorias));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        cors.setAllowedMethods(List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmail(email)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getSenhaHash())
                        .roles(u.getPerfil().name())
                        .disabled(!u.isAtivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(UsuarioRepository usuarios) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // O JWT identifica o usuário; o banco define o estado e o perfil atuais.
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String email = jwt.getSubject();
            if (email == null || email.isBlank()) {
                throw tokenRevogado();
            }

            var usuario = usuarios.findByEmail(email)
                    .orElseThrow(SecurityConfig::tokenRevogado);

            Object idClaim = jwt.getClaim("userId");
            Object versionClaim = jwt.getClaim("tokenVersion");
            Number idAtual = usuario.getId();
            Number versaoAtual = usuario.getTokenVersion();

            // Valida o tipo numérico das claims e compara os valores com o banco.
            if (!usuario.isAtivo()
                    || !(idClaim instanceof Integer || idClaim instanceof Long)
                    || !(versionClaim instanceof Integer || versionClaim instanceof Long)
                    || idAtual == null || versaoAtual == null
                    || ((Number) idClaim).longValue() != idAtual.longValue()
                    || ((Number) versionClaim).longValue() != versaoAtual.longValue()) {
                throw tokenRevogado();
            }

            return List.of(new SimpleGrantedAuthority(
                    "ROLE_" + usuario.getPerfil().name()));
        });

        return converter;
    }

    private static OAuth2AuthenticationException tokenRevogado() {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token", "Sessão inválida ou revogada.", null));
    }

    @Bean
    @Order(1)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(
                        "/", "/index.html", "/css/**", "/js/**", "/images/**",
                        "/media/**", "/assets/**", "/client/**",
                        "/favicon.ico", "/manifest.json",
                        "/api/v1/auth/login", "/api/v1/auth/registrar", "/api/v1/publico/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Somente leitura de arquivos do frontend: não libera escrita em /client/**.
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/css/**", "/js/**", "/images/**",
                                "/media/**", "/assets/**", "/client/**",
                                "/favicon.ico", "/manifest.json").permitAll()
                        .requestMatchers(HttpMethod.HEAD,
                                "/", "/index.html", "/css/**", "/js/**", "/images/**",
                                "/media/**", "/assets/**", "/client/**",
                                "/favicon.ico", "/manifest.json").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login", "/api/v1/auth/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/publico/**").permitAll()
                        .anyRequest().denyAll())
                .requestCache(cache -> cache.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter)
            throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/app.js", "/app.css", "/actuator/health").permitAll()
                        // Documentação pública apenas por GET; restrinja/desative em produção.
                        .requestMatchers(HttpMethod.GET,
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        .requestMatchers(
                                "/api/v1/admin/**", "/api/v1/estoque/**",
                                "/api/v1/financeiro/**", "/api/v1/relatorios/**")
                                .hasRole("ADMIN")
                        .requestMatchers("/api/v1/cozinha/**")
                                .hasAnyRole("ADMIN", "COZINHA")
                        .requestMatchers("/api/v1/cliente/**")
                                .hasRole("CLIENTE")
                        // Endpoint novo de API não recebe autorização automaticamente.
                        .requestMatchers("/api/**").denyAll()
                        // Preserva a exigência anterior de autenticação fora de /api.
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }
}
