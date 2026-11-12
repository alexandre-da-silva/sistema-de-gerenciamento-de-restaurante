package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}
