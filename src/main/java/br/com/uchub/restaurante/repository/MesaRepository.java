package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Mesa> findById(Long id);}
