package br.com.uchub.restaurante.config;

import br.com.uchub.restaurante.model.PerfilUsuario;
import br.com.uchub.restaurante.model.Usuario;
import br.com.uchub.restaurante.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.admin.email:}") private String adminEmail;
    @Value("${app.admin.password:}") private String adminPassword;
    public AdminInitializer(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios; this.passwordEncoder = passwordEncoder;
    }
    @Override public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.length() < 12) {
            throw new IllegalStateException("Defina APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD (mínimo 12 caracteres) no .env para criar o primeiro administrador.");
        }
        if (!usuarios.existsByEmail(adminEmail)) {
            usuarios.save(new Usuario("Administrador", adminEmail.toLowerCase(), passwordEncoder.encode(adminPassword), null, PerfilUsuario.ADMIN));
        }
    }
}
