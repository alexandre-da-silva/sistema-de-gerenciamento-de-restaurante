package br.com.uchub.restaurante;

import br.com.uchub.restaurante.config.SecurityConfig;
import br.com.uchub.restaurante.controller.PainelController;
import br.com.uchub.restaurante.repository.*;
import br.com.uchub.restaurante.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(controllers = PainelController.class, properties = {
    "app.jwt.secret=test-only-key-with-at-least-32-characters",
    "app.cors.allowed-origins=http://localhost:8080"
})
@Import(SecurityConfig.class)
class PainelAccessTest {
    @Autowired MockMvc mvc;
    @MockitoBean PainelRepository painel;
    @MockitoBean RecursoSistemaRepository recursos;
    @MockitoBean UsuarioRepository usuarios;
    @MockitoBean PedidoService pedidos;

    @Test void anonymousCannotReadDashboard() throws Exception {
        mvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isUnauthorized());
        verifyNoInteractions(painel);
    }
    @Test @WithMockUser(roles="CLIENTE") void clientCannotReadCustomerList() throws Exception {
        mvc.perform(get("/api/v1/admin/clientes")).andExpect(status().isForbidden());
        verifyNoInteractions(painel);
    }
    @Test @WithMockUser(roles="ADMIN") void adminCanReadOrders() throws Exception {
        mvc.perform(get("/api/v1/admin/pedidos/detalhes")).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles="ADMIN") void emptyOrderIsRejectedBeforeService() throws Exception {
        mvc.perform(post("/api/v1/admin/pedidos").contentType("application/json")
            .content("{\"clienteId\":1,\"itens\":[]}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(pedidos);
    }
}
