package br.com.uchub.restaurante.controller;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.service.AuthService;
import br.com.uchub.restaurante.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    private final UsuarioRepository usuarios;
    public AuthController(AuthService auth,UsuarioRepository usuarios) { this.auth = auth;this.usuarios=usuarios; }
    @GetMapping("/me") public Map<String,String> me(Authentication authentication){
        var u=usuarios.findByEmail(authentication.getName()).filter(x->x.isAtivo())
          .orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,"Conta inativa."));
        return Map.of("nome",u.getNome(),"email",u.getEmail(),"perfil",u.getPerfil().name());
    }
    @PostMapping("/registrar") public TokenResponse registrar(@Valid @RequestBody RegistroRequest request) { return auth.registrar(request); }
    @PostMapping("/login") public TokenResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) { auth.logout(authentication.getName()); }
    @PutMapping("/senha") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody AlterarSenhaRequest request, Authentication authentication) {
        auth.alterarSenha(authentication.getName(), request);
    }
}
