package br.com.uchub.restaurante.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Limite conservador por IP, em memória. Em cluster, usar Redis/gateway. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MS=60_000;
    private final ConcurrentHashMap<String, Window> windows=new ConcurrentHashMap<>();
    private record Window(long started, int attempts) {}
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path=request.getServletPath();
        return !path.equals("/api/v1/auth/login") && !path.equals("/api/v1/auth/registrar");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
      throws ServletException,IOException {
        String path=request.getServletPath();
        // O endereço vem da conexão; não confia em X-Forwarded-For arbitrário.
        String key=path+":"+request.getRemoteAddr();
        long now=Instant.now().toEpochMilli();
        Window window=windows.compute(key,(k,w)->w==null||now-w.started>=WINDOW_MS
            ?new Window(now,1):new Window(w.started,w.attempts+1));
        // Evita crescimento ilimitado no caso de varreduras por IPs distintos.
        if(windows.size()>10000)windows.entrySet().removeIf(e->now-e.getValue().started>WINDOW_MS);
        int limit=path.endsWith("/login")?12:5;
        if(window.attempts>limit){
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After",String.valueOf(Math.max(1,(WINDOW_MS-(now-window.started)+999)/1000)));
            response.getWriter().write("{\"message\":\"Muitas tentativas. Tente novamente mais tarde.\"}");
            return;
        }
        chain.doFilter(request,response);
    }
}
