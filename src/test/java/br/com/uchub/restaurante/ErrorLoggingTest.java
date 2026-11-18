package br.com.uchub.restaurante;
import br.com.uchub.restaurante.exception.ApiExceptionHandler;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;
class ErrorLoggingTest {
    @Test void unexpectedErrorIsLoggedButNotExposedToClient(){
        Logger logger=(Logger)LoggerFactory.getLogger(ApiExceptionHandler.class);
        var appender=new ListAppender<ILoggingEvent>();appender.start();logger.addAppender(appender);
        try {
            var result=new ApiExceptionHandler().unexpected(new IllegalStateException("diagnostic-only-fixture"));
            assertEquals(500,result.getStatusCode().value());
            assertFalse(result.getBody().toString().contains("diagnostic-only-fixture"));
            assertEquals("diagnostic-only-fixture",appender.list.getFirst().getThrowableProxy().getMessage());
        }finally{logger.detachAppender(appender);appender.stop();}
    }
}
