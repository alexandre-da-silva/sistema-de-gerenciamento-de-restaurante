package br.com.uchub.restaurante.service;

import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.repository.UsuarioRepository;
import br.com.uchub.restaurante.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usuarios = usuarios; this.encoder = encoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }
    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        validarTamanhoSenha(request.senha());
        String email = request.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        Usuario usuario = usuarios.save(new Usuario(request.nome().trim(), email,
                encoder.encode(request.senha()), request.telefone(), PerfilUsuario.CLIENTE));
        return token(usuario);
    }
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha()));
        Usuario usuario = usuarios.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));
        return token(usuario);
    }
    @Transactional
    public void alterarSenha(String email, AlterarSenhaRequest request) {
        validarTamanhoSenha(request.novaSenha());
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!encoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
        usuario.alterarSenhaHash(encoder.encode(request.novaSenha()));
    }
    private void validarTamanhoSenha(String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter no máximo 72 bytes em UTF-8.");
        }
    }
    private TokenResponse token(Usuario usuario) {
        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpirationSeconds(),
                usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }
}
