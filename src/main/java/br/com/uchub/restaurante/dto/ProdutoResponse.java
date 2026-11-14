package br.com.uchub.restaurante.dto;
import java.math.BigDecimal;
public record ProdutoResponse(Long id,String nome,String descricao,BigDecimal preco,Long categoriaId,
 String categoria,boolean ativo,String imagemUrl,Integer tempoPreparo,String tipo,boolean destaque,BigDecimal precoPromocional, ProdutoDetalhes detalhes, BigDecimal custo, BigDecimal margem, boolean disponivelAgora, long vendas30Dias, double calor, boolean popular) {}
