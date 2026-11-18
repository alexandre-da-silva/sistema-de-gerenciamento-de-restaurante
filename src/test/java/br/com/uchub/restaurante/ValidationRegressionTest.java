package br.com.uchub.restaurante;
import br.com.uchub.restaurante.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ValidationRegressionTest {
    @Test void nullOrderLinesAndNullAddonsAreRejectedBeforeService() throws Exception {
        try(var factory=Validation.buildDefaultValidatorFactory()){
            var validator=factory.getValidator();var mapper=new ObjectMapper();
            for(String itens:new String[]{"[null]","[{\"produtoId\":1,\"quantidade\":1,\"adicionaisIds\":[null]}]"}){
                var dto=mapper.readValue("{\"referencia\":\"12345678-1234-1234-1234-123456789abc\",\"itens\":"+itens+"}",PedidoRequest.class);
                assertFalse(validator.validate(dto).isEmpty());
            }
        }
    }
    @Test void nullRecipesOptionsAndSchedulesAreRejected() throws Exception {
        try(var factory=Validation.buildDefaultValidatorFactory()){
            var validator=factory.getValidator();var mapper=new ObjectMapper();
            for(String field:new String[]{"variacoes","adicionais","fichaTecnica","disponibilidade"}){
                var dto=mapper.readValue("{\"nome\":\"Prato\",\"preco\":20,\""+field+"\":[null]}",ProdutoRequest.class);
                assertFalse(validator.validate(dto).isEmpty(),field);
            }
            var nested=mapper.readValue("{\"nome\":\"Prato\",\"preco\":20,\"variacoes\":[{\"nome\":\"Grande\",\"preco\":30,\"ingredientes\":[null]}]}",ProdutoRequest.class);
            assertFalse(validator.validate(nested).isEmpty());
        }
    }
    @Test void minimumStockCannotBeNegative() {
        try(var factory=Validation.buildDefaultValidatorFactory()){
            assertFalse(factory.getValidator().validate(new GestaoRequests.Minimo(new java.math.BigDecimal("-1"))).isEmpty());
            assertTrue(factory.getValidator().validate(new GestaoRequests.Minimo(new java.math.BigDecimal("2.500"))).isEmpty());
        }
    }
}
