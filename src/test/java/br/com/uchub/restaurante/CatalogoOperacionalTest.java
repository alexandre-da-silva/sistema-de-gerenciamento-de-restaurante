package br.com.uchub.restaurante;
import br.com.uchub.restaurante.dto.*;
import br.com.uchub.restaurante.model.*;
import br.com.uchub.restaurante.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class CatalogoOperacionalTest {
 final ObjectMapper mapper=new ObjectMapper();
 final CatalogoOperacionalService service=new CatalogoOperacionalService(mapper,mock(JdbcTemplate.class));
 ProdutoDetalhes details(List<Integer> days,String start,String end){return new ProdutoDetalhes("SKU","Bar","Individual",days,start,end,true,false,0,List.of(new ProdutoDetalhes.Insumo(1L,new BigDecimal("0.250"))),List.of(new ProdutoDetalhes.Variacao("large","Grande",new BigDecimal("50.00"),new BigDecimal("2"))),List.of(new ProdutoDetalhes.Adicional("extra","Extra",new BigDecimal("5.00"),List.of(new ProdutoDetalhes.Insumo(2L,new BigDecimal("0.100"))))),List.of("lactose"),List.of(),null,null,null);}
 @Test void availabilityHonorsServiceDayAcrossMidnight(){var d=details(List.of(6),"20:00","02:00");assertTrue(CatalogoOperacionalService.disponivel(d,ZonedDateTime.parse("2026-11-15T01:00:00-03:00[America/Bahia]")));assertFalse(CatalogoOperacionalService.disponivel(d,ZonedDateTime.parse("2026-11-15T02:00:00-03:00[America/Bahia]")));assertFalse(CatalogoOperacionalService.disponivel(d,ZonedDateTime.parse("2026-11-16T01:00:00-03:00[America/Bahia]")));}
 @Test void recommendationDoesNotMarkColdStartOrTinySalesAsPopular(){assertFalse(CatalogoOperacionalService.popular(0,0));assertFalse(CatalogoOperacionalService.popular(4,4));assertTrue(CatalogoOperacionalService.popular(35,100));assertFalse(CatalogoOperacionalService.popular(34,100));assertEquals(0,CatalogoOperacionalService.calor(0,0));assertEquals(1,CatalogoOperacionalService.calor(100,100));assertTrue(CatalogoOperacionalService.calor(20,100)>CatalogoOperacionalService.calor(1,100));}
 @Test void optionsUseServerPricesAndScaleRecipe() throws Exception {Produto p=new Produto("Prato","",new BigDecimal("20"),null);p.definirDetalhes("SKU",mapper.writeValueAsString(details(List.of(),null,null)));var selected=service.escolher(p,new ItemPedidoRequest(1L,2,"large",List.of("extra"),null));assertEquals(new BigDecimal("55.00"),selected.preco());assertEquals(new BigDecimal("0.500"),selected.consumo().get(1L));assertEquals(new BigDecimal("0.100"),selected.consumo().get(2L));assertEquals("Bar",selected.setor());assertThrows(ResponseStatusException.class,()->service.escolher(p,new ItemPedidoRequest(1L,1,"large",List.of("extra","extra"),null)));assertThrows(ResponseStatusException.class,()->service.escolher(p,new ItemPedidoRequest(1L,1,"large",List.of(),"não permitido")));}
 @Test void publicDetailsHideRecipeAndTaxData(){var d=details(List.of(),null,null).publico();assertTrue(d.fichaTecnica().isEmpty());assertTrue(d.adicionais().getFirst().insumos().isEmpty());assertNull(d.fiscal());assertEquals(List.of("lactose"),d.alergenos());}
}
