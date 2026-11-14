package br.com.uchub.restaurante.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record ProdutoResponse(
 Long id,String nome,String descricao,BigDecimal preco,Long categoriaId,String categoria,
 boolean ativo,String imagemUrl,Integer tempoPreparo,String tipo,boolean destaque,BigDecimal precoPromocional,
 String setorProducao,String porcao,int ordemExibicao,String observacoesPermitidas,
 String alergenos,String tags,Integer calorias,boolean disponivelAgora,
 List<Variacao> variacoes,List<Adicional> adicionais,List<Horario> disponibilidade
){
 public record Variacao(Long id,String nome,BigDecimal preco){}
 public record Adicional(Long id,String nome,BigDecimal preco){}
 public record Horario(Integer diaSemana,LocalTime horaInicio,LocalTime horaFim){}
}
