package br.com.uchub.restaurante;

import br.com.uchub.restaurante.config.SecurityConfig;
import br.com.uchub.restaurante.controller.AuthController;
import br.com.uchub.restaurante.repository.UsuarioRepository;
import br.com.uchub.restaurante.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, properties = {
    "app.jwt.secret=test-only-key-with-at-least-32-characters",
    "app.cors.allowed-origins=http://localhost:8080"
})
@Import(SecurityConfig.class)
class AuthAccessTest {
    @Autowired MockMvc mvc;
    @MockitoBean AuthService auth;
    @MockitoBean UsuarioRepository usuarios;

    @Test void loginAcceptsAnonymousRequestEvenWithStaleHeader() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .header("Authorization", "Bearer expired-token")
                .contentType("application/json")
                .content("{\"email\":\"cliente@example.com\",\"senha\":\"password123\"}"))
            .andExpect(status().isOk());
    }
    @Test void registrationAcceptsAnonymousRequest() throws Exception {
        mvc.perform(post("/api/v1/auth/registrar")
                .contentType("application/json")
                .content("{\"nome\":\"Cliente\",\"email\":\"cliente@example.com\",\"senha\":\"password123\"}"))
            .andExpect(status().isOk());
    }
    @Test void adminStillRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/admin/usuarios")).andExpect(status().isUnauthorized());
    }
    @Test void javascriptIsPublic() throws Exception {
        mvc.perform(get("/js/auth.js")).andExpect(status().isOk());
    }
}
