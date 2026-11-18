package br.com.uchub.restaurante.repository;
import br.com.uchub.restaurante.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    // Serializa o checkout de um mesmo cliente antes da consulta de idempotência.
    // Complementa a constraint única de (cliente_id, referencia) no banco.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> findByEmailForUpdate(@Param("email") String email);
    boolean existsByEmail(String email);
    long countByPerfilAndAtivoTrue(br.com.uchub.restaurante.model.PerfilUsuario perfil);
}
