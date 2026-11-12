package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.RecursoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RecursoSistemaRepository extends JpaRepository<RecursoSistema, Long> {
    Optional<RecursoSistema> findByCodigo(String codigo);
}
