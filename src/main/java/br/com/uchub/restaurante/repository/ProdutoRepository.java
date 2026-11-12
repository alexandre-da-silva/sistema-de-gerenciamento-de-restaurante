package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByAtivoTrueOrderByNomeAsc();
}
