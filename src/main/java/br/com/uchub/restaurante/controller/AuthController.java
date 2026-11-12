package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/registrar") public TokenResponse registrar(@Valid @RequestBody RegistroRequest request) { return auth.registrar(request); }
    @PostMapping("/login") public TokenResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PutMapping("/senha") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody AlterarSenhaRequest request, Authentication authentication) {
        auth.alterarSenha(authentication.getName(), request);
    }
}
