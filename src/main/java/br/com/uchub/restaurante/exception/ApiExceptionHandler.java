package br.com.uchub.restaurante.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("timestamp", LocalDateTime.now(), "status", ex.getStatusCode().value(), "message", ex.getReason() == null ? "Requisição não processada." : ex.getReason()));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("timestamp", LocalDateTime.now(), "status", 401, "message", "E-mail ou senha inválidos."));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("timestamp", LocalDateTime.now(), "status", 409, "message", "A operação viola uma regra de integridade dos dados."));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("timestamp", LocalDateTime.now(), "status", 400, "message", errors));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("timestamp", LocalDateTime.now(), "status", 400, "message", "Corpo da requisição inválido."));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex) {
        // Não expõe stack trace, SQL, credenciais ou detalhes internos ao cliente.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("timestamp", LocalDateTime.now(), "status", 500, "message", "Erro interno. Consulte os logs do servidor."));
    }
}
