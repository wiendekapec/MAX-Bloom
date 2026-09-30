package bloom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import service.MaxInitDataVerifier;
import service.RateLimitService;

import java.io.IOException;
import java.util.List;

/**
 * Фильтр аутентификации запросов по заголовку X-Init-Data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataAuthFilter extends OncePerRequestFilter {

    private final MaxInitDataVerifier initDataVerifier;
    private final RateLimitService rateLimitService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String clientIp = extractClientIp(request);
        if (!rateLimitService.tryConsume(clientIp)) {
            log.warn("Rate limit exceeded for IP={}", clientIp);
            sendError(response, 429,
                    "RATE_LIMIT_EXCEEDED", "Too many requests. Try again later.");
            return;
        }

        String initData = request.getHeader("X-Init-Data");
        if (initData == null || initData.isBlank()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED_INIT_DATA", "Missing X-Init-Data header");
            return;
        }

        String maxUserId;
        try {
            maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        } catch (SecurityException e) {
            log.warn("InitData HMAC verification failed: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED_INIT_DATA", e.getMessage());
            return;
        }

        var auth = new UsernamePasswordAuthenticationToken(
                maxUserId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendError(HttpServletResponse response, int status,
                           String code, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                "{\"status\":" + status + ",\"code\":\"" + code + "\",\"detail\":\"" + detail + "\"}"
        );
    }
}
