package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.Pedido;
import br.com.uchub.restaurante.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pedido> findById(Long id);
    List<Pedido> findAllByOrderByCriadoEmDesc();
    List<Pedido> findByClienteEmailOrderByCriadoEmDesc(String email);
    List<Pedido> findByStatusOrderByCriadoEmAsc(StatusPedido status);
}
